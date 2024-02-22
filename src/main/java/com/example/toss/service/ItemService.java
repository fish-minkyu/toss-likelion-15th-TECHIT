package com.example.toss.service;

import com.example.toss.dto.ItemDto;
import com.example.toss.entity.Item;
import com.example.toss.repo.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        // itemRepository가 비어있을 때 4개의 상품 생성
        if(this.itemRepository.count() == 0) {
            this.itemRepository.saveAll(List.of(
                    Item.builder()
                            .name("mouse")
                            .price(40000)
                            .imageUrl("/static/mouse.png")
                            .build(),
                    Item.builder()
                            .name("keyboard")
                            .price(50000)
                            .imageUrl("/static/keyboard.png")
                            .build(),
                    Item.builder()
                            .name("monitor")
                            .price(100000)
                            .imageUrl("/static/monitor.png")
                            .build(),
                    Item.builder()
                            .name("speaker")
                            .price(30000)
                            .imageUrl("/static/speaker.png")
                            .build()
            ));
        }
    }

    public List<ItemDto> readAll() {
        return itemRepository.findAll().stream()
                .map(ItemDto::fromEntity)
                .toList();
    }

    public ItemDto readOne(Long id) {
        return itemRepository.findById(id)
                .map(ItemDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
