package ru.practicum.shareit.item.dto;

import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.model.Item;


public interface ItemMapper {

    ItemMapper ITEM_MAPPER = Mappers.getMapper(ItemMapper.class);

    ItemResponseDto toDto(Item item);

    Item toItem(ItemRequestDto dto);
}
