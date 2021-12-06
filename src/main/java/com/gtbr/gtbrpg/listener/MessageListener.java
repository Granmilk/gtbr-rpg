package com.gtbr.gtbrpg.listener;

import com.gtbr.gtbrpg.domain.enums.HandleType;
import com.gtbr.gtbrpg.handler.CommandHandler;
import com.gtbr.gtbrpg.handler.MemberHandler;
import com.gtbr.gtbrpg.service.MessageService;
import com.gtbr.gtbrpg.util.Constants;
import com.gtbr.gtbrpg.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

@Slf4j
public class MessageListener extends ListenerAdapter {

    private final CommandHandler commandHandler;
    private final MemberHandler memberHandler;

    public MessageListener() {
        this.memberHandler = new MemberHandler();
        this.commandHandler = new CommandHandler();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (!messageReceivedEvent.getAuthor().isBot() && MessageUtil.hasPrefix(messageReceivedEvent.getMessage().getContentRaw())) {
            commandHandler.handle(messageReceivedEvent.getMessage());
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().getGuilds().forEach(guild -> {
            log.info("Guild: {}", guild.getName());
            Role role = guild.getRoleById(Constants.RPG_ROLE_ID);
            guild.getMembersWithRoles(role).forEach(member -> {
                memberHandler.handle(member, HandleType.REGISTER_ON_READY);
            });
        });
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent guildMemberRoleAddEvent) {
        Member member = guildMemberRoleAddEvent.getMember();
        Role role = guildMemberRoleAddEvent.getGuild().getRoleById(Constants.RPG_ROLE_ID);
        if (guildMemberRoleAddEvent.getRoles().contains(role))
            memberHandler.handle(guildMemberRoleAddEvent.getMember(), HandleType.REGISTER_ON_UPDATE);

        MessageService.sendEmbbedMessage(guildMemberRoleAddEvent.getGuild().getTextChannelById("913504160752746596"),
                new EmbedBuilder().setColor(Color.GREEN).setTitle("Novo jogador registrado")
                        .setDescription("Seja bem vindo "+member.getUser().getAsMention()));
    }
}
