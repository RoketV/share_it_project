package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "items", schema = "public")
@Data
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotBlank
    private String name;
    @Column(name = "description")
    @NotBlank
    private String description;
    @Column(name = "is_available")
    @NotNull
    private Boolean available;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User user;
}
