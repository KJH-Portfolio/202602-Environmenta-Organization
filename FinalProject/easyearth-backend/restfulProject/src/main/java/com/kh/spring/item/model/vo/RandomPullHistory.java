package com.kh.spring.item.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RandomPullHistory {

    private int itemId;
    private String itemName;
    private String description;
    private int price;
    private String rarity;
    private String isOnSale;
    private String category;

    private int memberId;
}
