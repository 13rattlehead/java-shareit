package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    private long bookingId;

    private LocalDateTime bookingStart;

    private LocalDateTime bookingEnd;

    private Item item;

    private User booker;

    private BookingStatus status;

}
