package com.gtbr.gtbrpg.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_player")
public class GroupPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Integer id;

    @OneToOne
    @JoinColumn
    private Group group;

    @OneToOne
    @JoinColumn
    private Player player;

    @Column
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime leaveAt;
}
