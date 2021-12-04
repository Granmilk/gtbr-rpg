package com.gtbr.gtbrpg.domain.entity;

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
@Table(name = "\"group\"")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Integer groupId;

    @Column
    private String name;

    @Column
    private String thumbnail;

    @Column
    private String description;

    @OneToOne
    @JoinColumn
    private Player leader;

    @OneToOne
    @JoinColumn
    private Player creator;

    @Column
    private Integer size;

    @Column
    private LocalDateTime createdAt;

}
