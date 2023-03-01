package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByUser_Id(Long userId);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.user.id <> ?1 ORDER BY ir.created DESC")
    Page<ItemRequest> findAllExceptUser(Long userId, Pageable pageable);
}
