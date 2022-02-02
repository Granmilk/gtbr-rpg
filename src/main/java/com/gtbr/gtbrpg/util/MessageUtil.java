package com.gtbr.gtbrpg.util;

import com.gtbr.gtbrpg.GtbrRpgApplication;
import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.Player;
import com.gtbr.gtbrpg.domain.entity.Session;
import com.gtbr.gtbrpg.domain.enums.CommandDetails;
import com.gtbr.gtbrpg.domain.enums.CommandTypeHelp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.gtbr.gtbrpg.util.Constants.*;
import static com.gtbr.gtbrpg.util.GeneralUtils.in;

@Component
public class MessageUtil {

    private static String prefix;

    private MessageUtil(@Value("${configuration.prefix}") String prefix) {
        this.prefix = prefix;
    }

    public static boolean hasPrefix(String contentRaw) {
        return contentRaw.startsWith(prefix);
    }

    public static String getCommandOfMessage(Message message) {
        return message.getContentRaw()
                .trim()
                .replace(prefix, "")
                .split(" ")[0];
    }

    public static String removePrefixAndCommand(Message message) {
        return message.getContentRaw()
                .trim()
                .replace(prefix + getCommandOfMessage(message), "");
    }

    public static void hasPermission(Player userRequest, List<Player> authorizedUsers, boolean masterByPass) {
        if (!(masterByPass && userRequest.isAdmin()) && !in(userRequest.getDiscordId(), authorizedUsers.stream().map(Player::getDiscordId).collect(Collectors.toList())))
            throw new RuntimeException("Voce nao tem autorizacao para convidar pessoas para este grupo");
    }

    public static Map<String, Object> getParamatersMap(Message message, String command) {
        String allParametersInline = message.getContentRaw().trim().replace(prefix + command, "").split(" ")[1].trim();
        String[] parameters = allParametersInline.contains(",") ? allParametersInline.split(",") : new String[]{allParametersInline};
        Map<String, Object> mapParameter = new HashMap<>();

        for (String parameter : parameters) {
            if (parameter.startsWith("lider")) parameter = "lider=" + message.getMentionedUsers().get(0).getId();
            mapParameter.put(parameter.split("=")[0].trim().toLowerCase(Locale.ROOT), parameter.split("=")[1].trim());
        }

        return mapParameter;
    }

    public static boolean hasRequestObservation(String command) {
        return command.contains(" ");
    }


    public static EmbedBuilder buildEmbedGroupMessage(GroupPlayerDto groupPlayerDto, Session session, JDA jda) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(groupPlayerDto.group().getName())
                .setDescription(groupPlayerDto.group().getDescription())
                .setColor(Color.MAGENTA)
                .setThumbnail(groupPlayerDto.group().getThumbnail())
                .setFooter("Preenchimento: " + String.format("(%s/%s)", groupPlayerDto.playerList().size(), groupPlayerDto.group().getSize()));

        if (Objects.nonNull(session))
            embedBuilder.addField("Sessao", String.format("```Titulo: %s %nId: #%s%nData: %s%nHorario: %s```",
                    session.getTitle(),
                    session.getSessionId().toString(),
                    session.getScheduledTo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    session.getScheduledTo().format(DateTimeFormatter.ofPattern("HH:mm"))), true);

        embedBuilder.addBlankField(true)
                .addField("Instrucoes:", "Para solicitar sua entrada no grupo digite `*EntrarGrupo #" + groupPlayerDto.group().getGroupId() + "`", true)
                .addBlankField(false);

        groupPlayerDto.playerList().forEach(player -> {
            embedBuilder.addField("Membro: ", jda.getUserById(player.getDiscordId()).getAsMention() +
                            (Objects.equals(groupPlayerDto.group().getLeader().getDiscordId(), player.getDiscordId()) ? " " + CROWN_EMOJI_CODE : ""),
                    true);
        });

        return embedBuilder;
    }

    public static void replaceEmote(Message message, String oldEmoji, String newEmoji) {
        message.removeReaction(oldEmoji).queue();
        message.addReaction(newEmoji).queue();
    }

    public static void addDefaultReaction(Message message) {
        replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
    }

    public static EmbedBuilder buildEmbedSessionMessage(Session session) {
        JDA jda = GtbrRpgApplication.getJda();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle(session.getTitle())
                .setDescription(session.getDescription())
                .setFooter(session.getSessionStatus().getName() + " - " + (session.getSessionStatus().getId().equals(1) ? "\uD83D\uDFE2" : "\uD83D\uDD34"))
                .setImage(session.getThumbnail());
        switch (session.getSessionType()) {
            case SOLO -> {
                embedBuilder
                        .addField("Sessão para", "Um jogador", true)
                        .addField("Jogador", Objects.nonNull(session.getPlayer())
                                ? jda.getGuildById(session.getPlayer().getGuildId()).getMemberById(session.getPlayer().getDiscordId()).getAsMention()
                                : String.format("Para se registrar digite `*entrarSessao #%s`", session.getSessionId()), true);

            }
            case GROUP -> {
                embedBuilder
                        .addField("Sessão para", "Um grupo", true)
                        .addField("Grupo", Objects.nonNull(session.getGroup())
                                ? jda.getRoleById(session.getGroup().getRoleId()).getAsMention()
                                : String.format("Para registrar seu grupo digite `*entrarSessao #%s`", session.getSessionId()), true);
            }
        }

        if (Objects.nonNull(session.getStarted())) {
            embedBuilder.addField("Esta sessaão foi iniciada em",
                    session.getStarted().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")), true);
            if (Objects.nonNull(session.getFinished()))
                embedBuilder.addField("Esta sessaão foi finalizada em",
                        session.getFinished().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")), true);
        }

        embedBuilder
                .addBlankField(false)
                .addField("Agendada para", session.getScheduledTo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                .addBlankField(true)
                .addField("Horário", session.getScheduledTo().format(DateTimeFormatter.ofPattern("**HH:mm**")), true);

        return embedBuilder;
    }

    public static Integer getDeafaultIdNumberFromMessage(Message message) {
        return Integer.valueOf(removePrefixAndCommand(message).replace("#", "").trim().split(" ")[0].trim());
    }

    public static String getCommandWithId(Message message) {
        String[] strings = message.getContentRaw().split(" ");
        return strings[0] + " " + (strings[1].contains(prefix) ? strings[1].replace(prefix, "") : strings[1]);
    }

    public static List<MessageEmbed> buildEmbedHelpMessage() {
        EmbedBuilder embedBuilderGroup = new EmbedBuilder();
        EmbedBuilder embedBuilderSession = new EmbedBuilder();

        embedBuilderGroup.setTitle("Comandos de grupo").setDescription("Todos os comandos devem conter o prefixo `*`").setColor(Color.ORANGE);
        embedBuilderSession.setTitle("Comandos de sessao").setDescription("Todos os comandos devem conter o prefixo `*`").setColor(Color.BLUE);

        for (CommandDetails commandDetail : CommandTypeHelp.GROUP.getCommandDetails()) {
            embedBuilderGroup.addField(commandDetail.getCommand(), commandDetail.getDescription(), true);
        }
        for (CommandDetails commandDetail : CommandTypeHelp.SESSION.getCommandDetails()) {
            embedBuilderSession.addField(commandDetail.getCommand(), commandDetail.getDescription(), true);
        }
        return List.of(embedBuilderGroup.build(), embedBuilderSession.build());
    }
}
