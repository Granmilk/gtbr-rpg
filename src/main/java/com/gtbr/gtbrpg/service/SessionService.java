package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.domain.configurations.requests.MessageRequestParameter;
import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.dto.SessionRecord;
import com.gtbr.gtbrpg.domain.entity.*;
import com.gtbr.gtbrpg.domain.enums.RequestStatus;
import com.gtbr.gtbrpg.domain.enums.SessionType;
import com.gtbr.gtbrpg.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

import static com.gtbr.gtbrpg.domain.enums.RequestType.MESSAGE;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PlayerService playerService;
    private final GroupService groupService;
    private final RequestService requestService;

    public Session createSession(Map<String, Object> parameterMap, String discordId) {
        Player creator = playerService.getPlayerByDiscordId(discordId);

        if (!creator.isAdmin()) throw new RuntimeException("Você não tem permissão para criar uma sessão");

        validateCreateSessionParameters(parameterMap);

        Session session = buildSessionByParameters(parameterMap);

        return sessionRepository.save(session);
    }

    public Session updateSession(Map<String, Object> paramatersMap, String discordId, Integer sessionId) {
        Player creator = playerService.getPlayerByDiscordId(discordId);

        if (!creator.isAdmin()) throw new RuntimeException("Você não tem permissão para criar uma sessão");

        validateUpdateSessionParameters(paramatersMap);

        Session session = mergeSession(buildSessionByParameters(paramatersMap), sessionRepository.findById(sessionId).orElseThrow());

        return sessionRepository.save(session);
    }

    @SneakyThrows
    private Session mergeSession(Session sessionFromParams, Session session) {
        for (Field declaredField : session.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (!declaredField.isSynthetic() && Objects.nonNull(declaredField.get(sessionFromParams))){
                declaredField.set(session, declaredField.get(sessionFromParams));
            }
            declaredField.setAccessible(false);
        }
        return session;
    }

    private Session buildSessionByParameters(Map<String, Object> parameterMap) {
        return Session.builder()
                .title((String) parameterMap.get("titulo"))
                .description((String) parameterMap.get("descricao"))
                .scheduledTo(parameterMap.containsKey("data") ? parseToLocalDateTime((String) parameterMap.get("data")) : null)
                .sessionType(parameterMap.containsKey("tipo") ? SessionType.valueOf((String) parameterMap.get("tipo")) : null)
                .sessionStatus(Status.builder().id(1).build())
                .thumbnail((String) parameterMap.get("thumbnail"))
                .canList(parameterMap.containsKey("podeConsultar")
                        ?  Boolean.valueOf(parameterMap.get("podeConsultar").toString().toLowerCase()) : Boolean.TRUE)
                .build();
    }

    private LocalDateTime parseToLocalDateTime(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm");
        TemporalAccessor temporalAccessor = dateTimeFormatter.parse(date);
        return LocalDateTime.from(temporalAccessor);
    }

    private void validateUpdateSessionParameters(Map<String, Object> parameterMap) {
        if (parameterMap.containsKey("sessionId")) throw new RuntimeException("O campo `sessionId` nao pode ser alterado");

        if (parameterMap.containsKey("data")) {
            try {
                String date = (String) parameterMap.get("data");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm");
                TemporalAccessor temporalAccessor = dateTimeFormatter.parse(date);
                LocalDateTime.from(temporalAccessor);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao formatar a data, o padrão correto é: dd/MM/yyyy-HH:mm");
            }
        }
    }

    private void validateCreateSessionParameters(Map<String, Object> parameterMap) {
        if (!parameterMap.containsKey("titulo")) throw new RuntimeException("O campo `titulo` é obrigatório");
        if (!parameterMap.containsKey("descricao")) throw new RuntimeException("O campo `descricao` é obrigatório");
        if (!parameterMap.containsKey("data")) throw new RuntimeException("O campo `data` é obrigatório");
        if (!parameterMap.containsKey("tipo")) throw new RuntimeException("O campo `tipo` é obrigatório");

        try {
            String date = (String) parameterMap.get("data");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm");
            TemporalAccessor temporalAccessor = dateTimeFormatter.parse(date);
            LocalDateTime.from(temporalAccessor);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao formatar a data, o padrão correto é: dd/MM/yyyy-HH:mm");
        }
    }

    public SessionRecord findMySession(String discordId) {
        Optional<Session> soloSession;
        Optional<Session> groupSession;
        try {
            soloSession = sessionRepository.findSessionByPlayerId(playerService.getPlayerByDiscordId(discordId).getPlayerId());
        } catch (RuntimeException e) {
            soloSession = Optional.empty();
        }
        try {
            groupSession = sessionRepository.findSessionByGroupId(groupService.findGroupByPlayer(discordId).group().getGroupId());
        } catch (RuntimeException e) {
            groupSession = Optional.empty();
        }

        List<Session> availableSessions = new ArrayList<>();
        List<Session> registeredSessions = new ArrayList<>();

        if (soloSession.isEmpty()) {
            Session availableSessionsBySessionType = sessionRepository.findAvailableSessionsBySessionType(SessionType.SOLO);
            if (Objects.nonNull(availableSessionsBySessionType))
                availableSessions.add(availableSessionsBySessionType);
        } else
            registeredSessions.add(soloSession.get());

        if (groupSession.isEmpty()) {
            Session availableSessionsBySessionType = sessionRepository.findAvailableSessionsBySessionType(SessionType.GROUP);
            if (Objects.nonNull(availableSessionsBySessionType))
                availableSessions.add(availableSessionsBySessionType);
        } else
            registeredSessions.add(groupSession.get());

        return new SessionRecord(registeredSessions, availableSessions);
    }

    public Session joinSession(Integer sessionId, String discordId) {
        Player player = playerService.getPlayerByDiscordId(discordId);
        SessionRecord sessionRecord = findMySession(discordId);
        List<Session> sessionList = new ArrayList<>();
        sessionRepository.findAvailableSessionById(sessionId)
                .ifPresentOrElse(session -> {
                            switch (session.getSessionType()) {
                                case SOLO -> {
                                    if (sessionRecord.registeredSessions()
                                            .stream().anyMatch(registeredSession -> registeredSession.getSessionType().equals(SessionType.SOLO)))
                                        throw new RuntimeException("Você já está registrado em uma sessão solo, não é possivel se registrar em duas sessões do mesmo tipo.");
                                    if (Objects.nonNull(session.getPlayer()))
                                        throw new RuntimeException(String.format("O jogador %s já se registrou para esta sessão.", session.getPlayer().getName()));

                                    session.setPlayer(player);

                                }
                                case GROUP -> {
                                    GroupPlayerDto groupByPlayer = groupService.findGroupByPlayer(discordId);
                                    if (sessionRecord.registeredSessions()
                                            .stream().anyMatch(registeredSession -> registeredSession.getSessionType().equals(SessionType.GROUP)))
                                        throw new RuntimeException("Você já está registrado em uma sessão de grupo, não é possivel se registrar em duas sessões do mesmo tipo.");
                                    if (Objects.nonNull(session.getPlayer()))
                                        throw new RuntimeException(String.format("O grupo %s já se registrou para esta sessão.", session.getGroup().getName()));

                                    session.setGroup(groupByPlayer.group());
                                }
                            }
                            sessionList.add(sessionRepository.save(session));
                        },
                        () -> {
                            throw new RuntimeException("Sessão não disponível ou inexistente");
                        });

        return sessionList.get(0);
    }

    public Session leaveSession(Integer sessionId, String discordId) {
        SessionRecord sessionRecord = findMySession(discordId);
        List<Session> sessionCollected = sessionRecord.registeredSessions().stream().filter(session -> session.getSessionId().equals(sessionId)).collect(Collectors.toList());

        if (sessionCollected.isEmpty())
            throw new RuntimeException("Você não está neste grupo.");

        Session session = sessionCollected.get(0);

        if (session.getSessionType().equals(SessionType.SOLO))
            session.setPlayer(null);
        else {
            Group group = session.getGroup();
            session.setGroup(null);
            GroupPlayerDto groupById = groupService.findGroupById(group.getGroupId());
            groupById.playerList().forEach(player ->
                    requestService.register(Request.builder()
                            .requestStatus(RequestStatus.MESSAGE)
                            .requestType(MESSAGE)
                            .requestedAt(LocalDateTime.now())
                            .reviewerObservation("Message sent by system.")
                            .requestParameter(new JSONObject(new MessageRequestParameter(player,
                                    String.format("O líder do seu grupo `%s` cancelou o registro para a sessão `%s` que aconteceria em: `%s`",
                                            group.getName(), session.getTitle(), session.getScheduledTo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))),
                                    LocalDateTime.now(), LocalDateTime.now())).toString())
                            .build()));

        }

        return sessionRepository.save(session);
    }

    public List<Session> findAllSessionsToNotifications() {
        return sessionRepository.findAvailableSessionsForNextDays(LocalDateTime.now(), LocalDateTime.now().plusDays(1L));
    }

    public Session start(Integer sessionId, String userId) {
        Player player = playerService.getPlayerByDiscordId(userId);
        if (!player.isAdmin()) throw new RuntimeException("Somente o mestre pode iniciar sessoes");

        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> {
            throw new RuntimeException("Sessao inexistente");
        });

        if (Objects.nonNull(session.getStarted())) throw new RuntimeException("Sessao ja iniciada");

        session.setStarted(LocalDateTime.now());
        sessionRepository.save(session);

        return session;
    }

    public void saveThreadId(String threadId, Integer sessionId) {
        Optional<Session> sessionOptional = sessionRepository.findById(sessionId);

        sessionOptional.ifPresent(session -> {
            session.setThreadId(threadId);
            sessionRepository.save(session);
        });

    }

    public Session finish(Integer sessionId, String userId) {
        Player player = playerService.getPlayerByDiscordId(userId);
        if (!player.isAdmin()) throw new RuntimeException("Somente o mestre pode finalizar sessoes");

        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> {
            throw new RuntimeException("Sessao inexistente");
        });

        if (Objects.nonNull(session.getFinished())) throw new RuntimeException("Sessao ja finalizada");

        session.setFinished(LocalDateTime.now());
        sessionRepository.save(session);

        return session;
    }

    public Session findSessionById(Integer sessionId, String userId) {
        Player player = playerService.getPlayerByDiscordId(userId);
        if (!player.isAdmin()) throw new RuntimeException("Somente o mestre pode consultar sessoes");

        return sessionRepository.findById(sessionId).orElseThrow(() -> {
            throw new RuntimeException("Sessao inexistente");
        });
    }
}
