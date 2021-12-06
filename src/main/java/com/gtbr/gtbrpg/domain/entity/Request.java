package com.gtbr.gtbrpg.domain.entity;

import com.gtbr.gtbrpg.domain.enums.RequestStatus;
import com.gtbr.gtbrpg.domain.enums.RequestType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Integer requestId;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(length = 500)
    private String requestParameter;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column
    private String playerObservation;

    @Column
    private String reviewerObservation;

    @Column
    private LocalDateTime requestedAt;

    @Column
    private boolean processed;

    @Column
    private LocalDateTime processedAt;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestStatus processIfStatus;

}
