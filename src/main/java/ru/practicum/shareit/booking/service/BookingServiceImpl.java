package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ParameterNotValidException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, BookingRequestDto requestDto) {

        User booker = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() ->
                        new NotFoundException("Вещь с id = " + requestDto.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new ParameterNotValidException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new AccessException("Нельзя бронировать собственную вещь");
        }

        Booking booking = BookingMapper.toBooking(requestDto);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingDto(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, Boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new AccessException("Подтверждать бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ParameterNotValidException("Бронирование уже обработано");
        }

        booking.setStatus(
                approved
                        ? BookingStatus.APPROVED
                        : BookingStatus.REJECTED
        );

        return BookingMapper.toBookingDto(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException("Бронирование не найдено"));

        boolean isBooker =
                booking.getBooker().getId().equals(userId);

        boolean isOwner =
                booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new AccessException("Недостаточно прав");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long userId, BookingState state) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id = " + userId + " не найден"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);

            case CURRENT -> bookingRepository.findCurrentByBooker(userId, now);

            case PAST -> bookingRepository.findPastByBooker(userId, now);

            case FUTURE -> bookingRepository.findFutureByBooker(userId, now);

            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    BookingStatus.WAITING);

            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId,
                    BookingStatus.REJECTED);
        };

        return BookingMapper.toBookingDtoList(bookings);
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long ownerId, BookingState state) {

        userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id = " + ownerId + " не найден"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {

            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);

            case CURRENT -> bookingRepository.findCurrentByOwner(ownerId, now);

            case PAST -> bookingRepository.findPastByOwner(ownerId, now);

            case FUTURE -> bookingRepository.findFutureByOwner(ownerId, now);

            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId,
                    BookingStatus.WAITING);

            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId,
                    BookingStatus.REJECTED);
        };

        return BookingMapper.toBookingDtoList(bookings);
    }


}