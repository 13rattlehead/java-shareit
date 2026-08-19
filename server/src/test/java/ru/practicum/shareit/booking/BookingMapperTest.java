package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toBookingDto_shouldMapAllFields() {
        User user = new User();
        user.setId(10L);
        user.setName("Denis");

        Item item = new Item();
        item.setId(20L);
        item.setName("Дрель");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2026, 8, 18, 10, 0));
        booking.setEnd(LocalDateTime.of(2026, 8, 18, 12, 0));
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(user);

        BookingDto result = BookingMapper.toBookingDto(booking);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart())
                .isEqualTo(LocalDateTime.of(2026, 8, 18, 10, 0));
        assertThat(result.getEnd())
                .isEqualTo(LocalDateTime.of(2026, 8, 18, 12, 0));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(20L);
        assertThat(result.getItem().getName()).isEqualTo("Дрель");
        assertThat(result.getBooker().getId()).isEqualTo(10L);
    }

    @Test
    void toBookingDtoList_shouldMapList() {
        Booking booking = new Booking();
        booking.setId(1L);

        User user = new User();
        user.setId(10L);

        Item item = new Item();
        item.setId(20L);
        item.setName("Дрель");

        booking.setBooker(user);
        booking.setItem(item);

        List<BookingDto> result =
                BookingMapper.toBookingDtoList(List.of(booking));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
}