package com.kargox.dto.rating;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RatingResponse {

    private Long id;
    private Long bookingId;
    private String ratedByName;
    private String ratedByRole;
    private Integer score;
    private String comment;
    private Instant createdAt;
}
