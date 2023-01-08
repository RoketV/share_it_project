package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@Valid @RequestBody ItemDto dto,
                                           @RequestHeader("X-Sharer-User-Id") @Valid @NotNull long userId) {
        return ResponseEntity.ok(itemService.addItem(dto, userId));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestBody ItemDto dto,
                                              @RequestHeader("X-Sharer-User-Id") @Valid @NotNull long userId,
                                              @PathVariable long itemId) {
        return ResponseEntity.ok(itemService.updateItem(dto, userId, itemId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable long itemId) {
        return ResponseEntity.ok(itemService.getItem(itemId));
    }

    @GetMapping
    public ResponseEntity<Set<ItemDto>> getItems(@RequestHeader("X-Sharer-User-Id") @Valid @NotNull long userId) {
        return ResponseEntity.ok(itemService.getItems(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Set<ItemDto>> searchItem(@RequestParam(required = false) String text) {
        return ResponseEntity.ok(itemService.searchItem(text));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ItemDto> deleteItem(@PathVariable long itemId) {
        return ResponseEntity.ok(itemService.deleteItem(itemId));
    }
}
