package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.id = ?1 and (b.item.user.id = ?2 or b.user.id = ?2)")
    Optional<Booking> findByIdAndItem_User_IdOrUser_Id(Long bookingId, Long userId);

    @Query("select b from Booking b where b.user.id = ?1 order by b.start desc")
    List<Booking> findAllByUser_Id(Long bookerId);

    List<Booking> findAllByItem_Id(Long itemId);

    List<Booking> findAllByItem_User_Id(Long ownerId);

    @Query("select b from Booking b where b.item.user.id = ?1 order by b.start desc")
    Page<Booking> findAllByOwner(Long ownerId, Pageable pageable);

    @Query("select b from Booking b where b.start > current_timestamp and b.user.id = ?1 order by b.start desc")
    Page<Booking> findFutureBookingsByBooker(Long bookerId, Pageable pageable);

    @Query("select b from Booking b where b.end < current_timestamp and b.user.id = ?1 order by b.start desc")
    Page<Booking> findPastBookingsByBooker(Long bookerId, Pageable pageable);

    @Query("select b from Booking b where b.end < current_timestamp and b.user.id = ?1 and b.item.id = ?2")
    List<Booking> findPastBookingsByBookerAndItem(Long bookerId, Long itemId);

    @Query("select b from Booking b where (b.start < current_timestamp and b.end > current_timestamp ) " +
            "and b.user.id = ?1 order by b.start desc")
    Page<Booking> findCurrentBookingsByBooker(Long bookerId, Pageable pageable);

    @Query("select b from Booking b where b.status = ?1 and b.user.id = ?2 order by b.start desc")
    Page<Booking> findByStatusByBooker(Status status, Long bookerId, Pageable pageable);

    @Query("select b from Booking b where b.start > CURRENT_TIMESTAMP and b.item.user.id = ?1 order by b.start desc")
    Page<Booking> findFutureBookingsByOwner(Long bookerId, Pageable pageable);

    @Query("select b from Booking b where b.end < current_timestamp and b.item.user.id = ?1 order by b.start desc")
    Page<Booking> findPastBookingsByOwner(Long bookerId, Pageable pageable);

    @Query("select b from Booking b where (b.start < current_timestamp and b.end > current_timestamp ) " +
            "and b.item.user.id = ?1 order by b.start desc")
    Page<Booking> findCurrentBookingsByOwner(Long bookerId, Pageable pageable);

    @Query("select b from Booking b where b.status = ?1 and b.item.user.id = ?2 order by b.start desc")
    Page<Booking> findByStatusByOwner(Status status, Long bookerId, Pageable pageable);

    @Modifying
    @Query("update Booking b set b.status = ?1 where b.id = ?2")
    void updateBookingStatus(Status status, Long bookerId);
}
