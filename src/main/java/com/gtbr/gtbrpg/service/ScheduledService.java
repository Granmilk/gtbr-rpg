package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.GtbrRpgApplication;
import com.gtbr.gtbrpg.domain.configurations.requests.InviteRequestParameters;
import com.gtbr.gtbrpg.domain.configurations.requests.SubscribeRequestParameters;
import com.gtbr.gtbrpg.domain.entity.Request;
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

    public final RequestService requestService;

    @Scheduled(fixedDelay = 30000)
    public void processRequests() {
        List<Request> requestList = requestService.findAllToProcess();
        log.info("Processing requests. total: {}", requestList.size());
        JDA jda = GtbrRpgApplication.getJda();
        try {
            requestList.forEach(request -> {
                if (Objects.isNull(request.getProcessedAt()) || LocalDateTime.now().isAfter(request.getProcessedAt().plus(1, ChronoUnit.HOURS))) {
                    switch (request.getRequestType()) {
                        case SUBSCRIBE -> {
                            SubscribeRequestParameters subscribeRequestParameters = SubscribeRequestParameters.of(request.getRequestParameter());
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
                            InviteRequestParameters inviteRequestParameters = InviteRequestParameters.of(request.getRequestParameter());
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
                    }
                }
            });
        } catch (NullPointerException e) {
            log.error("{}", e.getMessage());
        }
    }
}
