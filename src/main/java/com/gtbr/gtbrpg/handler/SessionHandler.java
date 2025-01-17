package com.gtbr.gtbrpg.handler;

import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.dto.SessionRecord;
import com.gtbr.gtbrpg.domain.entity.Session;
import com.gtbr.gtbrpg.domain.enums.SessionType;
import com.gtbr.gtbrpg.service.GroupService;
import com.gtbr.gtbrpg.service.MessageService;
import com.gtbr.gtbrpg.service.SessionService;
import com.gtbr.gtbrpg.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.gtbr.gtbrpg.util.Constants.*;
import static com.gtbr.gtbrpg.util.MessageUtil.addDefaultReaction;

@Service
@RequiredArgsConstructor
public class SessionHandler implements CommandTypeHandler {

    private final SessionService sessionService;
    private final GroupService groupService;

    @Override
    public void handle(String command, Message message) {
        switch (command) {
            case MINHA_SESSAO -> {
                handleMySession(message);
                addDefaultReaction(message);
            }
            case LISTAR_SESSOES -> {
                handleFindAvailableSessions(message);
                addDefaultReaction(message);
            }
            case CRIAR_SESSAO -> {
                handleCreateSession(message);
                addDefaultReaction(message);
            }
            case ENTRAR_SESSAO -> {
                handleJoin(message);
                addDefaultReaction(message);
            }
            case SAIR_SESSAO -> {
                handleLeave(message);
                addDefaultReaction(message);
            }
            case EDITAR_SESSAO -> {
                handleUpdate(message);
                addDefaultReaction(message);
            }
            case INICIAR_SESSAO -> {
                handleStart(message);
                addDefaultReaction(message);
            }
            case FINALIZAR_SESSAO -> {
                handleFinish(message);
                addDefaultReaction(message);
            }
            case CONSULTAR_SESSAO -> {
                handleFind(message);
                addDefaultReaction(message);
            }
        }
    }

    private void handleFindAvailableSessions(Message message) {
        sessionService.findAvailableSessions().forEach(session -> {
            message.getChannel().sendMessageEmbeds(MessageUtil.buildEmbedSessionMessage(session).build()).queue();
        });
    }

    private void handleFind(Message message) {
        MessageUtil.buildEmbedSessionMessage(sessionService.findSessionById(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId()));
    }

    private void handleFinish(Message message) {
        Session session = sessionService.finish(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId());

        if(session.getSessionType().equals(SessionType.GROUP)) {
            GroupPlayerDto groupPlayerDto = groupService.closeGroup(message.getAuthor().getId());
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Grupo [" + groupPlayerDto.group().getName() + "] fechado")
                    .setDescription("Este grupo agora encontra-se fechado e todos seus jogadores foram liberados!")
                    .setColor(Color.green)
                    .setThumbnail(groupPlayerDto.group().getThumbnail())
                    .setFooter("Grupo fechado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm")))
                    .addBlankField(false);

            groupPlayerDto.playerList().forEach(player -> {
                embedBuilder.addField("Membro: ", message.getJDA().getUserById(player.getDiscordId()).getAsMention() +
                                (Objects.equals(groupPlayerDto.group().getLeader().getDiscordId(), player.getDiscordId()) ? " " + CROWN_EMOJI_CODE : ""),
                        true);
                message.getJDA().getUserById(player.getDiscordId()).openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(String.format("O Grupo `%s` que você fazia parte foi fechado e você está livre para ingressar em um novo grupo!", groupPlayerDto.group().getName())).queue();
                });
            });

            message.getGuild().getRoleById(groupPlayerDto.group().getRoleId()).delete().queue();
            MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);
        }

        message.getJDA().getGuilds().get(0).getThreadChannelById(session.getThreadId()).sendMessage("Sessao finalizada!").queue();
        message.getJDA().getGuilds().get(0).getThreadChannelById(session.getThreadId()).getManager().setArchived(true).queue();
    }

    private void handleStart(Message message) {
        Session session = sessionService.start(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId());
        message.getJDA()
                .getGuilds()
                .get(0)
                .getTextChannelById("858532801859026974")
                .createThreadChannel("Topico da sessao [" + (session.getSessionType().equals(SessionType.GROUP) ? session.getTitle() : session.getPlayer().getName()) + "] [" + session.getStarted().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "]")
                .queue(threadChannel -> {
                    if (session.getSessionType().equals(SessionType.GROUP)) {
                        GroupPlayerDto group = groupService.findGroupById(session.getGroup().getGroupId());
                        group.playerList().forEach(player -> {
                            threadChannel.addThreadMemberById("178560394527244288").queue();
                            threadChannel.addThreadMemberById(player.getDiscordId()).queue();
                        });
                        threadChannel.sendMessage("Sejam bem vindos a sessao MUAHAHAHAHA").queue();
                        EmbedBuilder sessionEmbbed = MessageUtil.buildEmbedSessionMessage(session);
                        EmbedBuilder groupEmbbed = MessageUtil.buildEmbedGroupMessage(group, session, threadChannel.getJDA());

                        threadChannel.sendMessageEmbeds(sessionEmbbed.build(), groupEmbbed.build()).queue();
                    } else {

                        threadChannel.addThreadMemberById("178560394527244288").queue();
                        threadChannel.addThreadMemberById(session.getPlayer().getDiscordId()).queue();

                        threadChannel.sendMessage("Seja bem vindo a sessao MUAHAHAHAHA").queue();
                        EmbedBuilder sessionEmbbed = MessageUtil.buildEmbedSessionMessage(session);

                        threadChannel.sendMessageEmbeds(sessionEmbbed.build()).queue();
                        threadChannel.sendMessage(threadChannel.getJDA().getUserById(session.getPlayer().getDiscordId()).getAsMention()).queue();
                    }

                    session.setThreadId(threadChannel.getId());
                    sessionService.saveThreadId(threadChannel.getId(), session.getSessionId());
                });
    }

    private void handleUpdate(Message message) {
        Session session = sessionService.updateSession(
                MessageUtil.getParamatersMap(message, MessageUtil.getCommandWithId(message)),
                message.getAuthor().getId(),
                MessageUtil.getDeafaultIdNumberFromMessage(message));
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    private void handleLeave(Message message) {
        Session session = sessionService.leaveSession(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId());
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    private void handleJoin(Message message) {
        Session session = sessionService.joinSession(MessageUtil.getDeafaultIdNumberFromMessage(message), message.getAuthor().getId());
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    private void handleMySession(Message message) {
        SessionRecord sessionRecord = sessionService.findMySession(message.getAuthor().getId());
        if (!sessionRecord.registeredSessions().isEmpty()) {
            message.getChannel().sendMessage("Você está registrado nessas sessões").queue();
            sessionRecord.registeredSessions().forEach(session -> {
                message.getChannel().sendMessageEmbeds(MessageUtil.buildEmbedSessionMessage(session).build()).queue();
            });
        }
        if (!sessionRecord.canRegister().isEmpty()) {
            message.getChannel().sendMessage("Você pode se registrar nas seguintes sessões").queue();
            sessionRecord.canRegister().forEach(session -> {
                message.getChannel().sendMessageEmbeds(MessageUtil.buildEmbedSessionMessage(session).build()).queue();
            });
        }

        if (sessionRecord.registeredSessions().isEmpty() && sessionRecord.canRegister().isEmpty()) {
            message.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle("Sem sessões disponiveis").build()).queue();
        }
    }

    private void handleCreateSession(Message message) {
        Session session = sessionService.createSession(MessageUtil.getParamatersMap(message, MessageUtil.getCommandOfMessageWithPrefix(message)), message.getAuthor().getId());
        EmbedBuilder embedBuilder = MessageUtil.buildEmbedSessionMessage(session);
        message.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
