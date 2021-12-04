package com.gtbr.gtbrpg.handler;


import com.gtbr.gtbrpg.service.GroupService;
import com.gtbr.gtbrpg.service.MessageService;
import com.gtbr.gtbrpg.util.MessageUtil;
import com.gtbr.gtbrpg.util.SpringContext;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.gtbr.gtbrpg.util.Constants.*;

@Slf4j
public class CommandHandler {

    private String randomUrl = "https://www.random.org/integers/?num={roll}&min=1&max={dice}&col=1&base=10&format=plain&rnd=new";
    private GroupService groupService;

    public CommandHandler() {
        this.groupService = SpringContext.getBean(GroupService.class);
    }

    public void handle(Message message) {
        try {
            JDA jda = message.getJDA();

            String command = MessageUtil.getCommandOfMessage(message).toUpperCase();
            log.info("[INITIALIZING COMMAND HANDLER] - [COMMAND:{}]", command);
            switch (command) {
                case ORDEM_DE_INICIATIVA -> {

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
                case DELETAR_GRUPO -> {
                    handleDeleteGroup(message);
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
                default -> {
                    replaceEmote(message, RELOADING_EMOJI_CODE, WHAT_EMOJI_CODE);
                    MessageService.sendMessage(message.getChannel(), "Comando nao reconhecido!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.removeReaction("\uD83D\uDD04").queue();
            message.addReaction(ERROR_EMOJI_CODE).queue();
            MessageService.sendEmbbedMessage(message.getChannel(),
                    new EmbedBuilder()
                            .setTitle("Erro")
                            .setDescription(e.getMessage())
                            .setColor(Color.RED));
        }

    }

    private void handleInviteGroup(Message message) {

    }

    private void handleQuitGroup(Message message) {

    }

    private void handleJoinGroup(Message message) {

    }

    private void handleCloseGroup(Message message) {

    }

    private void handleDeleteGroup(Message message) {

    }

    private void handleUpdateGroup(Message message) {

    }

    private void handleFindGroup(Message message) {

    }

    private void handleCreateGroup(Message message) {
        groupService.createGroup(MessageUtil.getParamatersMap(message, MessageUtil.getCommandOfMessage(message)), message.getAuthor().getId());
    }

    private void replaceEmote(Message message, String oldEmoji, String newEmoji) {
        message.removeReaction(oldEmoji).queue();
        message.addReaction(newEmoji).queue();
    }

    /*public void handlePrivateMessage(PrivateMessageReceivedEvent privateMessageReceivedEvent) throws IOException, InterruptedException {

        String modelo = "10d10+1d6+3d20-4";
        List<String> dices = getDices(modelo);
        List<String> subtracts = dices.stream().filter(dice -> dice.contains("-")).collect(Collectors.toList());
        List<String> divides = dices.stream().filter(dice -> dice.contains("\\")).collect(Collectors.toList());
        List<String> multiply = dices.stream().filter(dice -> dice.contains("*")).collect(Collectors.toList());

        if ()

        List<Integer> dicesRolled = rola(rolls, dice);

        Collections.sort(dicesRolled);

        privateMessageReceivedEvent.getJDA()
                .getTextChannelById(863201818614956042L)
                .sendMessage("teste true random -> "+  dicesRolled)
                .queue();
    }

    private List<String> getDices(String modelo) {
        String[] dices = modelo.split("\\+");
        return Arrays.asList(dices.clone());
    }


    public List<Integer> rola(Integer roll, Integer dice) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.of(1, ChronoUnit.MINUTES))
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(randomUrl.replace("{roll}", roll.toString()).replace("{dice}", dice.toString())))
                .build();

        HttpResponse<String> stringHttpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        String[] strings = stringHttpResponse.body().trim().split("\n");

        return Arrays.stream(strings)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }*/

}