package com.gtbr.gtbrpg.handler;

import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.GroupPlayer;
import com.gtbr.gtbrpg.service.GroupService;
import com.gtbr.gtbrpg.service.MessageService;
import com.gtbr.gtbrpg.service.SessionService;
import com.gtbr.gtbrpg.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.gtbr.gtbrpg.util.Constants.*;
import static com.gtbr.gtbrpg.util.MessageUtil.buildEmbedGroupMessage;
import static com.gtbr.gtbrpg.util.MessageUtil.replaceEmote;

@Service
@RequiredArgsConstructor
public class GroupHandler implements CommandTypeHandler{

    public final GroupService groupService;
    public final SessionService sessionService;

    @Override
    public void handle(String command, Message message){
        switch (command){
            case MEU_GRUPO -> {
                handleFindMyGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case VER_GRUPO -> {
                handleFindGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case CRIAR_GRUPO -> {
                handleCreateGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case EDITAR_GRUPO -> {
                handleUpdateGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case FECHAR_GRUPO -> {
                handleCloseGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case ENTRAR_GRUPO -> {
                handleJoinGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case SAIR_GRUPO -> {
                handleQuitGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case CONVIDAR_GRUPO -> {
                handleInviteGroup(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
        }
    }

    private void handleFindMyGroup(Message message) {
        GroupPlayerDto groupPlayerDto = groupService.findGroupByPlayer(message.getAuthor().getId());
        EmbedBuilder embedBuilder = buildEmbedGroupMessage(groupPlayerDto, sessionService.findSessionByGroup(groupPlayerDto.group().getGroupId()), message.getJDA());
        MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);
    }

    private void handleInviteGroup(Message message) {
        List<User> mentionedUsers = message.getMentionedUsers();
        groupService.sendInvite(mentionedUsers, message.getAuthor());
    }

    private void handleQuitGroup(Message message) {
        GroupPlayer groupPlayer = groupService.leaveGroup(message.getAuthor().getId());
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Você deixou o grupo: " + groupPlayer.getGroup().getName())
                .setDescription("Agora você pode se juntar a outros grupos.")
                .setColor(Color.green)
                .addBlankField(false);
        MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);

        message.getJDA()
                .getUserById(groupPlayer.getGroup().getLeader().getDiscordId())
                .openPrivateChannel()
                .queue(privateChannel -> {
                    privateChannel.sendMessage(String.format("O jogador `%s` deixou seu grupo `%s`!",
                                    groupPlayer.getPlayer().getName(),
                                    groupPlayer.getGroup().getName()))
                            .queue();
                });
    }

    private void handleJoinGroup(Message message) {
        String command = MessageUtil.removePrefixAndCommand(message);
        groupService.requestJoinGroup(command, message.getAuthor().getId());
    }

    private void handleCloseGroup(Message message) {
        GroupPlayerDto groupPlayerDto = groupService.closeGroup(message.getAuthor().getId());
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Grupo fechado " + groupPlayerDto.group().getName())
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

    private void handleUpdateGroup(Message message) {
        GroupPlayerDto groupPlayerDto = groupService.updateGroup(message.getAuthor().getId(), MessageUtil.getParamatersMap(message, MessageUtil.getCommandOfMessage(message)));
        EmbedBuilder embedBuilder = buildEmbedGroupMessage(groupPlayerDto, sessionService.findSessionByGroup(groupPlayerDto.group().getGroupId()), message.getJDA());
        MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);
    }

    private void handleFindGroup(Message message) {
        String groupId = message.getContentRaw().trim().replace("*" + MessageUtil.getCommandOfMessage(message), "").split(" ")[1];
        GroupPlayerDto groupPlayerDto = groupService.findGroupById(Integer.valueOf(groupId));
        EmbedBuilder embedBuilder = buildEmbedGroupMessage(groupPlayerDto, sessionService.findSessionByGroup(groupPlayerDto.group().getGroupId()), message.getJDA());
        MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);
    }

    private void handleCreateGroup(Message message) {
        GroupPlayerDto groupPlayerDto = groupService.createGroup(MessageUtil.getParamatersMap(message, MessageUtil.getCommandOfMessage(message)), message.getAuthor().getId());
        message.getGuild()
                .createRole()
                .setMentionable(true)
                .setName(groupPlayerDto.group().getName())
                .setColor(Color.red)
                .setPermissions(List.of(Permission.EMPTY_PERMISSIONS)).queue(role -> {
                    message.getGuild().addRoleToMember(message.getMember().getId(), role).queue();
                    EmbedBuilder embedBuilder = buildEmbedGroupMessage(groupPlayerDto, sessionService.findSessionByGroup(groupPlayerDto.group().getGroupId()), message.getJDA());
                    MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);
                    groupService.addRoleId(groupPlayerDto.group().getGroupId(), role.getId());
                });
    }
}
