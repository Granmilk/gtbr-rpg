package com.gtbr.gtbrpg.domain.dto;

import com.gtbr.gtbrpg.domain.entity.Session;

import java.util.List;

public record SessionRecord(List<Session> registeredSessions, List<Session> canRegister) {
}
