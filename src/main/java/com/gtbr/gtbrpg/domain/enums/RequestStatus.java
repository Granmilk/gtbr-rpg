package com.gtbr.gtbrpg.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestStatus {
    ACEITA,
    RECUSADA,
    SEM_RESPOSTA,
    MESSAGE
}
