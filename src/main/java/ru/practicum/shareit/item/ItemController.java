package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comments.dto.CommentRequestDto;
import ru.practicum.shareit.comments.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> addItem(@Valid @RequestBody ItemRequestDto dto,
                                                   @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        return ResponseEntity.ok(itemService.addItem(dto, userId));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(@RequestBody ItemRequestDto dto,
                                                      @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
                                                      @PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.updateItem(dto, userId, itemId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@RequestHeader("X-Sharer-User-Id") @NotNull Long ownerId,
                                                   @PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItem(ownerId, itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getItems(@RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        return ResponseEntity.ok(itemService.getItems(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> searchItem(@RequestParam(required = false) String text) {
        return ResponseEntity.ok(itemService.searchItem(text));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> deleteItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.deleteItem(itemId));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> postComment(@Valid @RequestBody CommentRequestDto dto,
                                                          @PathVariable Long itemId,
                                                          @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        return ResponseEntity.ok(itemService.addComment(dto, itemId, userId));
    }
}
