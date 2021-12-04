package com.gtbr.gtbrpg.domain.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Integer playerId;

    @Column
    private String name;

    @Column
    private String tag;

    @Column
    private String discordId;

    @OneToOne
    @JoinColumn
    private Status status;
}
