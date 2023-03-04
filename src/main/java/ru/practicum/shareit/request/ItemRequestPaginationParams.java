package ru.practicum.shareit.request;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class ItemRequestPaginationParams {
    @Min(0)
    private final Integer from;
    @Min(1)
    @Max(20)
    private final Integer size;

    public ItemRequestPaginationParams(@RequestParam(value = "from", defaultValue = "0")
                                       Integer from,
                                       @RequestParam(value = "size", defaultValue = "20")
                                       Integer size) {
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

