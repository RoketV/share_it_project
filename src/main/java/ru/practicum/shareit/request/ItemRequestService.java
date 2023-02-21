package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestOutputDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestOutputDto addRequest(ItemRequestInputDto dto, Long userId) {
        if (!userExists(userId)) {
            throw new EntityNotFoundException(String.format("there is no user with id %d to make a itemRequest", userId));
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("there is no user with id %d to make a itemRequest", userId)));
        dto.setOwner(owner);
        ItemRequest request = ItemRequestMapper.ITEM_REQUEST_MAPPER.toItemRequest(dto);
        request.setCreated(LocalDateTime.now());
        return ItemRequestMapper.ITEM_REQUEST_MAPPER.toDto(itemRequestRepository.save(request));
    }

    public List<ItemRequestOutputDto> getItemRequestWithItemsResponse(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(String.format("there is no user with id %d", userId));
        }
        List<ItemRequestOutputDto> requests = itemRequestRepository.findAllByUser_Id(userId)
                .stream()
                .map(ItemRequestMapper.ITEM_REQUEST_MAPPER::toDto)
                .collect(Collectors.toList());
        setItemsForItemRequestsDto(requests);
        return requests;
    }

    public ItemRequestOutputDto getItemRequest(Long itemId, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(String.format("there is no user with id %d", userId));
        }
        ItemRequest request = itemRequestRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("there is no item with id %d", itemId)));
        ItemRequestOutputDto dto = ItemRequestMapper.ITEM_REQUEST_MAPPER.toDto(request);
        List<ItemOutputDto> items = itemRepository.findItemsByRequestId(itemId)
                .stream()
                .map(ItemMapper.ITEM_MAPPER::toDto)
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }

    public List<ItemRequestOutputDto> getItemRequestWithPagination(Long userId, ItemRequestPaginationParams params) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(String.format("there is no user with id %d", userId));
        }
        Page<ItemRequest> page = itemRequestRepository.findAllExceptUser(
                userId, PageRequest.of(params.getFrom(), params.getSize()));
        List<ItemRequestOutputDto> requests = page.getContent().stream()
                .map(ItemRequestMapper.ITEM_REQUEST_MAPPER::toDto)
                .collect(Collectors.toList());
        setItemsForItemRequestsDto(requests);
        return requests;
    }

    private boolean userExists(Long userId) {
        return userRepository.findById(userId).isPresent();
    }

    private void setItemsForItemRequestsDto(List<ItemRequestOutputDto> requests) {
        List<ItemOutputDto> items = itemRepository.findItemsWithRequest()
                .stream()
                .map(ItemMapper.ITEM_MAPPER::toDto)
                .collect(Collectors.toList());
        for (ItemRequestOutputDto request : requests) {
            request.setItems(items
                    .stream()
                    .filter(item -> item.getRequestId().equals(request.getId()))
                    .collect(Collectors.toList()));
        }
    }
}
