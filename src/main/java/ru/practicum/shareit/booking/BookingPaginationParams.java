package ru.practicum.shareit.booking;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class BookingPaginationParams {
    @Min(0)
    private final Integer from;
    @Max(20)
    @Min(1)
    private final Integer size;

    public BookingPaginationParams(@RequestParam(value = "from", defaultValue = "0") Integer from,
                                   @RequestParam(value = "size", defaultValue = "20") Integer size) {
        if (from == null) {
            from = 0;
        }
        if (size == null) {
            size = 20;
        }
        this.from = from;
        this.size = size;
    }
}
