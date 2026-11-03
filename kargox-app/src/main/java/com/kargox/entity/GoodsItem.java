package com.kargox.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "goods_items")
public class GoodsItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String name;

    @Enumerated(EnumType.STRING)
    private GoodsCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoodsSizeTier sizeTier = GoodsSizeTier.SMALL;

    private Integer quantity = 1;
    private boolean fragile = false;
    private String photoUrl;
}
