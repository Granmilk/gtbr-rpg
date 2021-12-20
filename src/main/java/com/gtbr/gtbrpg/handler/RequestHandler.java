package com.gtbr.gtbrpg.handler;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import static com.gtbr.gtbrpg.util.Constants.ACEITAR_REQUISICAO;
import static com.gtbr.gtbrpg.util.Constants.DONE_EMOJI_CODE;
import static com.gtbr.gtbrpg.util.Constants.REJEITAR_REQUISICAO;
import static com.gtbr.gtbrpg.util.Constants.RELOADING_EMOJI_CODE;
import static com.gtbr.gtbrpg.util.MessageUtil.buildEmbedGroupMessage;
import static com.gtbr.gtbrpg.util.MessageUtil.replaceEmote;

import com.gtbr.gtbrpg.domain.configurations.requests.InviteRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.SubscribeRequestParameters;
import com.gtbr.gtbrpg.domain.dto.GroupPlayerDto;
import com.gtbr.gtbrpg.domain.entity.Request;
import com.gtbr.gtbrpg.domain.enums.RequestStatus;
import com.gtbr.gtbrpg.service.GroupService;
import com.gtbr.gtbrpg.service.MessageService;
import com.gtbr.gtbrpg.service.RequestService;
import com.gtbr.gtbrpg.util.MessageUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestHandler implements CommandTypeHandler{

    public final RequestService requestService;
    public final GroupService groupService;

    @Override
    public void handle(String command, Message message) {
        switch (command){
            case ACEITAR_REQUISICAO -> {
                handleAcceptGroupRequest(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
            case REJEITAR_REQUISICAO -> {
                handleRejectGroupRequest(message);
                replaceEmote(message, RELOADING_EMOJI_CODE, DONE_EMOJI_CODE);
            }
        }
    }

    private void handleAcceptGroupRequest(Message message) {
        String requestId = MessageUtil.removePrefixAndCommand(message).trim().replace("#", "");
        Request request = requestService.update(Integer.valueOf(requestId), RequestStatus.ACEITA);
        GroupPlayerDto groupPlayerDto = groupService.acceptGroupInvite(request);
        EmbedBuilder embedBuilder = buildEmbedGroupMessage(groupPlayerDto, message);
        MessageService.sendEmbbedMessage(message.getChannel(), embedBuilder);
    }

    private void handleRejectGroupRequest(Message message) {
        String requestId = MessageUtil.removePrefixAndCommand(message).replace("#", "").trim();
        Request request = requestService.update(requestId.contains(" ") ? Integer.parseInt(requestId.split(" ")[0]) : Integer.parseInt(requestId), RequestStatus.RECUSADA);
        switch (request.getRequestType()) {
            case INVITE -> {
                InviteRequestParameters inviteRequestParameters = InviteRequestParameters.of(request.getRequestParameter());
                message.getJDA()
                        .getUserById(inviteRequestParameters.getInvitedBy().getDiscordId())
                        .openPrivateChannel()
                        .queue(privateChannel ->
                                privateChannel.sendMessage(String.format("Requisicao #%s `recusada por %s` em `%s` `motivo: %s`",
                                        request.getRequestId(),
                                        inviteRequestParameters.getInvitedPlayer().getTag(),
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")),
                                        Objects.isNull(request.getReviewerObservation()) ? "" : request.getReviewerObservation())).queue());
            }
            case SUBSCRIBE -> {
                SubscribeRequestParameters subscribeRequestParameters = SubscribeRequestParameters.of(request.getRequestParameter());
                message.getJDA()
                        .getUserById(subscribeRequestParameters.getIssuer().getDiscordId())
                        .openPrivateChannel()
                        .queue(privateChannel -> privateChannel.sendMessage(String.format("Requisicao #%s `recusada por %s` em `%s` motivo: %s",
                                request.getRequestId(),
                                subscribeRequestParameters.getSubscribedGroup().getLeader().getTag(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")),
                                Objects.isNull(request.getReviewerObservation()) ? "" : request.getReviewerObservation())).queue());
            }
        }
    }
}
