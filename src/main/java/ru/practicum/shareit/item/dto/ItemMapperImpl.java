package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapperImpl implements ItemMapper {
    @Override
    public ItemResponseDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setName(item.getName());
        dto.setUser(item.getUser());
        return dto;
    }

    @Override
    public Item toItem(ItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Item item = new Item();
        item.setName(dto.getName());
        item.setUser(dto.getUser());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        return item;
    }
}
