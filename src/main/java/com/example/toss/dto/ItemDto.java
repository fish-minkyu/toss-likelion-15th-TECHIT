package com.example.toss.dto;

import com.example.toss.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private Integer price;
    private String imageUrl;

    public static ItemDto fromEntity(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
