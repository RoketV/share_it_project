package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequestInputDto {

    private Long id;
    @NotBlank
    private String description;
    private User owner;
}
