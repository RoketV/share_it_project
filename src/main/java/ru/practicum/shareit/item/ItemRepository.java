package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Optional;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByUser_Id(Long id);
}
