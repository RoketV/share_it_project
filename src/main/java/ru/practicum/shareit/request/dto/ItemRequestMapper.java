package ru.practicum.shareit.request.dto;

import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.ItemRequest;

public interface ItemRequestMapper {

    ItemRequestMapper ITEM_REQUEST_MAPPER = Mappers.getMapper(ItemRequestMapper.class);

    ItemRequestOutputDto toDto(ItemRequest request);

    ItemRequest toItemRequest(ItemRequestInputDto dto);
}
