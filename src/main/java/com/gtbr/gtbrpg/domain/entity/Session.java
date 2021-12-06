package com.gtbr.gtbrpg.domain.entity;

import com.gtbr.gtbrpg.domain.enums.SessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Integer sessionId;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime scheduledTo;

    @Column
    private LocalDateTime started;

    @Column
    private LocalDateTime finished;

    @Column(length = 20000)
    private String notebook;

    @Column
    private String thumbnail;

    @Column
    @Enumerated
    private SessionType sessionType;

    @OneToOne
    @JoinColumn
    private Status sessionStatus;

    @OneToOne
    @JoinColumn
    private Player player;

    @OneToOne
    @JoinColumn
    private Group group;

}
