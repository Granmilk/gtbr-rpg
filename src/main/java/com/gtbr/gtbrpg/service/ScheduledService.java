package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.GtbrRpgApplication;
import com.gtbr.gtbrpg.domain.configurations.requests.InviteRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.MessageRequestParameter;
import com.gtbr.gtbrpg.domain.configurations.requests.SubscribeRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.utils.RequestBuildParameterUtil;
import com.gtbr.gtbrpg.domain.entity.Request;
import com.gtbr.gtbrpg.domain.enums.RequestStatus;
import com.gtbr.gtbrpg.domain.enums.RequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final RequestService requestService;
    private final SessionService sessionService;

    private static final Integer PROCESS_REQUEST_FIXED_DELAY = 30000;
    private static final Integer NOTIFICATIONS_FIXED_DELAY = 60000;

    @Scheduled(fixedDelay = PROCESS_REQUEST_FIXED_DELAY)
    public void processRequests() {
        List<Request> requestList = requestService.findAllToProcess();
        log.info("Processing requests. total: {}", requestList.size());
        JDA jda = GtbrRpgApplication.getJda();
        try {
            requestList.forEach(request -> {
                if (Objects.isNull(request.getProcessedAt()) || LocalDateTime.now().isAfter(request.getProcessedAt().plus(1, ChronoUnit.HOURS))) {
                    switch (request.getRequestType()) {
                        case SUBSCRIBE -> {
                            SubscribeRequestParameters subscribeRequestParameters = RequestBuildParameterUtil.of(request.getRequestParameter(), SubscribeRequestParameters.class);
                            Objects.requireNonNull(jda
                                            .getUserById(subscribeRequestParameters.getSubscribedGroup().getLeader().getDiscordId()), "Discord returning a null user! cannot open private channel with the user")
                                    .openPrivateChannel().queue(privateChannel -> {
                                        privateChannel.sendMessage(String.format("%s pediu para juntar-se ao grupo %s id #%s, para aceitar responda `*aceitarRequisicao #%s` ou se deseja rejeitar `*rejeitarRequisicao #%s <motivo>`",
                                                        subscribeRequestParameters.getIssuer().getTag(),
                                                        subscribeRequestParameters.getSubscribedGroup().getName(),
                                                        subscribeRequestParameters.getSubscribedGroup().getGroupId(),
                                                        request.getRequestId(),
                                                        request.getRequestId()))
                                                .queue();
                                    });
                            requestService.process(request);
                            log.info("Request processed, awaiting for response: {}", new JSONObject(request).toString());
                        }
                        case INVITE -> {
                            InviteRequestParameters inviteRequestParameters = RequestBuildParameterUtil.of(request.getRequestParameter(), InviteRequestParameters.class);
                            Objects.requireNonNull(jda.getGuildById(inviteRequestParameters.getInvitedPlayer().getGuildId())
                                            .getMemberById(inviteRequestParameters.getInvitedPlayer().getDiscordId())
                                            .getUser(), "Discord returning a null user! cannot open private channel with the user")
                                    .openPrivateChannel().queue(privateChannel -> {
                                        privateChannel.sendMessage(String.format("%s te convidou para o grupo %s id #%s, para aceitar responda `*aceitarRequisicao #%s` ou se deseja rejeitar `*rejeitarRequisicao #%s <motivo>`",
                                                        inviteRequestParameters.getInvitedBy().getTag(),
                                                        inviteRequestParameters.getInvitedTo().getName(),
                                                        inviteRequestParameters.getInvitedTo().getGroupId(),
                                                        request.getRequestId(),
                                                        request.getRequestId()))
                                                .queue();
                                    });
                            requestService.process(request);
                            log.info("Request processed, awaiting for response: {}", new JSONObject(request).toString());
                        }
                        case MESSAGE -> {
                            MessageRequestParameter messageRequestParameter = RequestBuildParameterUtil.of(request.getRequestParameter(), MessageRequestParameter.class);
                            if (LocalDateTime.now().isAfter(messageRequestParameter.getSendAt())) {
                                jda
                                        .getUserById(messageRequestParameter.getPlayer().getDiscordId())
                                        .openPrivateChannel().queue(privateChannel -> {
                                            privateChannel.sendMessage(messageRequestParameter.getMessage()).queue();
                                        });

                                requestService.process(request);
                                log.info("Message sent, request has been processed: {}", new JSONObject(request).toString());
                            }
                        }
                    }
                }
            });
        } catch (NullPointerException e) {
            log.error("{}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = NOTIFICATIONS_FIXED_DELAY)
    public void createNotifications() {
        log.info("Starting creation of notifications of sessions");
        sessionService.findAllSessionsToNotifications().forEach(session -> {
            switch (session.getSessionType()) {
                case SOLO -> {
                    MessageRequestParameter.MessageRequestParameterBuilder parameterBuilder = MessageRequestParameter.builder()
                            .scheduledAt(LocalDateTime.now())
                            .sendAt(LocalDateTime.now())
                            .player(session.getPlayer());
                    if (LocalDateTime.now().isBefore(session.getScheduledTo().minusDays(1))
                            && LocalDateTime.now().isAfter(session.getScheduledTo().minusHours(23)))
                        requestService.register(Request.builder()
                                .requestParameter(new JSONObject(parameterBuilder.message(String.format("Faltam `24 horas` para sua sessão `%s` começar, confirme sua presença respondendo `*confirmarPresença #%s`", session.getTitle(), session.getSessionId())).build()).toString())
                                .requestedAt(LocalDateTime.now())
                                .requestType(RequestType.MESSAGE)
                                .requestStatus(RequestStatus.SEM_RESPOSTA)
                                .processIfStatus(RequestStatus.SEM_RESPOSTA)
                                .reviewerObservation(session.getPlayer().getPlayerId() + "-" + session.getSessionId())
                                .build());
                    if (LocalDateTime.now().isBefore(session.getScheduledTo().minusHours(1))
                            && LocalDateTime.now().isAfter(session.getScheduledTo().minusMinutes(50))) {
                        parameterBuilder.message(String.format("Falta `1 hora` para sua sessão `%s` começar, prepare-se!", session.getTitle()));
                        requestService.register(Request.builder()
                                .requestParameter(new JSONObject(parameterBuilder.build()).toString())
                                .requestedAt(LocalDateTime.now())
                                .requestType(RequestType.MESSAGE)
                                .requestStatus(RequestStatus.SEM_RESPOSTA)
                                .processIfStatus(RequestStatus.SEM_RESPOSTA)
                                .reviewerObservation(session.getPlayer().getPlayerId() + "-" + session.getSessionId())
                                .build());
                    }
                }
                case GROUP -> {
                    MessageRequestParameter.MessageRequestParameterBuilder parameterBuilder = MessageRequestParameter.builder()
                            .scheduledAt(LocalDateTime.now())
                            .sendAt(LocalDateTime.now())
                            .player(session.getPlayer());
                    if (LocalDateTime.now().isBefore(session.getScheduledTo().minusDays(1))
                            && LocalDateTime.now().isAfter(session.getScheduledTo().minusHours(23)))
                        requestService.register(Request.builder()
                                .requestParameter(new JSONObject(parameterBuilder.message(String.format("Faltam `24 horas` para sua sessão `%s` começar, confirme sua presença respondendo `*confirmarPresença #%s`", session.getTitle(), session.getSessionId())).build()).toString())
                                .requestedAt(LocalDateTime.now())
                                .requestType(RequestType.MESSAGE)
                                .requestStatus(RequestStatus.SEM_RESPOSTA)
                                .processIfStatus(RequestStatus.SEM_RESPOSTA)
                                .reviewerObservation(session.getPlayer().getPlayerId() + "-" + session.getSessionId())
                                .build());
                    if (LocalDateTime.now().isBefore(session.getScheduledTo().minusHours(1))
                            && LocalDateTime.now().isAfter(session.getScheduledTo().minusMinutes(50))) {
                        parameterBuilder.message(String.format("Falta `1 hora` para sua sessão `%s` começar, prepare-se!", session.getTitle()));
                        requestService.register(Request.builder()
                                .requestParameter(new JSONObject(parameterBuilder.build()).toString())
                                .requestedAt(LocalDateTime.now())
                                .requestType(RequestType.MESSAGE)
                                .requestStatus(RequestStatus.SEM_RESPOSTA)
                                .processIfStatus(RequestStatus.SEM_RESPOSTA)
                                .reviewerObservation(session.getPlayer().getPlayerId() + "-" + session.getSessionId())
                                .build());
                    }
                }
            }
        });


    }
}
