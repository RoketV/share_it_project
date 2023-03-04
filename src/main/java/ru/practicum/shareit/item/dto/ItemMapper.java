package ru.practicum.shareit.item.dto;

import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.model.Item;


public interface ItemMapper {

    ItemMapper ITEM_MAPPER = Mappers.getMapper(ItemMapper.class);

    ItemOutputDto toDto(Item item);

    Item toItem(ItemInputDto dto);
}
