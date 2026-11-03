package com.kargox.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ratings")
public class Rating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rated_by_user_id")
    private User ratedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role ratedByRole; // who is giving the rating

    @Column(nullable = false)
    private Integer score; // 1-5 score

    private String comment;
}
