package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.addUser(dto));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> patchUser(@RequestBody UserDto dto, @PathVariable Long userId) {
        return ResponseEntity.ok(userService.updateUser(dto, userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping
    public ResponseEntity<Set<UserDto>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
