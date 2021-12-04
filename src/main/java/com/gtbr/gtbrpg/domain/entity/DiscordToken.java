package com.gtbr.gtbrpg.domain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
@Data
public class DiscordToken {

    @Id
    @Column
    private Long idDiscordToken;

    @Column
    private String discordToken;
}
