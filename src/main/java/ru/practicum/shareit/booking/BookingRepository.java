package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.id = ?1 and (b.item.user.id = ?2 or b.user.id = ?2)")
    Optional<Booking> findByIdAndItem_User_IdOrUser_Id(Long bookingId, Long userId);

    List<Booking> findAllByUser_Id(Long bookerId);

    List<Booking> findAllByItem_Id(Long itemId);

    List<Booking> findAllByItem_User_Id(Long ownerId);

    @Query("select b from Booking b where b.item.user.id = ?1")
    List<Booking> findAllByOwner(Long ownerId);

    @Query("select b from Booking b where b.start > ?1 and b.user.id = ?2")
    List<Booking> findFutureBookingsByBooker(LocalDateTime now, Long bookerId);

    @Query("select b from Booking b where b.end < ?1 and b.user.id = ?2")
    List<Booking> findPastBookingsByBooker(LocalDateTime now, Long bookerId);

    @Query("select b from Booking b where b.end < ?1 and b.user.id = ?2 and b.item.id = ?3")
    List<Booking> findPastBookingsByBookerAndItem(LocalDateTime now, Long bookerId, Long itemId);

    @Query("select b from Booking b where (b.start < ?1 and b.end > ?1) and b.user.id = ?2")
    List<Booking> findCurrentBookingsByBooker(LocalDateTime now, Long bookerId);

    @Query("select b from Booking b where b.status = ?1 and b.user.id = ?2")
    List<Booking> findByStatusByBooker(Status status, Long bookerId);

    @Query("select b from Booking b where b.start > ?1 and b.item.user.id = ?2")
    List<Booking> findFutureBookingsByOwner(LocalDateTime now, Long bookerId);

    @Query("select b from Booking b where b.end < ?1 and b.item.user.id = ?2")
    List<Booking> findPastBookingsByOwner(LocalDateTime now, Long bookerId);

    @Query("select b from Booking b where (b.start < ?1 and b.end > ?1) and b.item.user.id = ?2")
    List<Booking> findCurrentBookingsByOwner(LocalDateTime now, Long bookerId);

    @Query("select b from Booking b where b.status = ?1 and b.item.user.id = ?2")
    List<Booking> findByStatusByOwner(Status status, Long bookerId);

    @Modifying
    @Query("update Booking b set b.status = ?1 where b.id = ?2")
    void updateBookingStatus(Status status, Long bookerId);
}
