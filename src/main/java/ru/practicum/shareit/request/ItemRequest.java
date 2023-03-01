package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TODO Sprint add-item-requests.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "requests", schema = "public")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "description")
    @NotBlank
    private String description;
    @Column(name = "created")
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User user;

    public ItemRequest(Long id, String description, LocalDateTime created, User user) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.user = user;
    }

    public ItemRequest() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemRequest)) return false;
        ItemRequest request = (ItemRequest) o;
        return Objects.equals(getId(), request.getId()) && Objects.equals(getDescription(), request.getDescription()) && Objects.equals(getCreated(), request.getCreated()) && Objects.equals(getUser(), request.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDescription(), getCreated(), getUser());
    }
}
