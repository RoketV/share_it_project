package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemOutputDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestOutputDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemOutputDto> items;

    public ItemRequestOutputDto(Long id, String description, LocalDateTime created, List<ItemOutputDto> items) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = items;
    }

    public ItemRequestOutputDto() {
    }
}
