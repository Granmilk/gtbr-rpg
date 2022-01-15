package com.gtbr.gtbrpg.service;

import com.gtbr.gtbrpg.domain.entity.Request;
import com.gtbr.gtbrpg.domain.enums.RequestStatus;
import com.gtbr.gtbrpg.domain.enums.RequestType;
import com.gtbr.gtbrpg.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    public Request register(Request request) {
        request.setRequestedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    public List<Request> findAllToProcess() {
        return requestRepository.findAllToProcess();
    }

    public List<Request> findAllByType(RequestType type) {
        return requestRepository.findAllByType(type);
    }


    public Request process(Request request) {
        request.setProcessed(true);
        request.setProcessedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    public Request update(Integer requestId, RequestStatus status) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> {
            throw new RuntimeException("Request nao encontrada");
        });
        if (!Objects.equals(request.getRequestStatus(), RequestStatus.SEM_RESPOSTA))
            throw new RuntimeException("Esta requisicao ja foi processada");

        request.setRequestStatus(status);
        return requestRepository.save(request);
    }

    public void processAllByType(RequestType requestType) {
        findAllByType(requestType).forEach(this::process);
    }

    public Request findMessageByCode(String code) {
        return requestRepository.findByReviewerObservation(code).orElseThrow(() -> {
            throw new RuntimeException("Mensagem não encontrada por codigo");
        });
    }
}
