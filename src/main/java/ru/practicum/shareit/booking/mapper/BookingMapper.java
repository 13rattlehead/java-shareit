package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;


public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getBookingId())
                .start(booking.getBookingStart())
                .end(booking.getBookingEnd())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getUserId())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setBookingId(bookingDto.getId());
        booking.setBookingStart(bookingDto.getStart());
        booking.setBookingEnd(bookingDto.getEnd());
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }
}
