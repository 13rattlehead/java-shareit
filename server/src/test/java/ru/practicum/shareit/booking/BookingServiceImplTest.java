package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ParameterNotValidException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemService itemService;

    private User createUser(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name.toLowerCase() + System.nanoTime() + "@test.ru");
        return userRepository.save(user);
    }

    private Item createItem(User owner, boolean available) {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Хорошая дрель");
        dto.setAvailable(available);

        ItemDto result = itemService.create(owner.getId(), dto);

        return itemRepository.findById(result.getId()).orElseThrow();
    }

    private BookingRequestDto createRequest(Long itemId) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));
        return dto;
    }

    @Test
    void create_shouldCreateWaitingBooking() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        BookingDto result =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        User owner = createUser("Owner");
        Item item = createItem(owner, true);

        assertThatThrownBy(() ->
                bookingService.create(999999L, createRequest(item.getId())))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrowWhenItemNotFound() {
        User booker = createUser("Booker");

        assertThatThrownBy(() ->
                bookingService.create(booker.getId(), createRequest(999999L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrowWhenItemUnavailable() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, false);

        assertThatThrownBy(() ->
                bookingService.create(booker.getId(), createRequest(item.getId())))
                .isInstanceOf(ParameterNotValidException.class)
                .hasMessage("Вещь недоступна для бронирования");
    }

    @Test
    void create_shouldThrowWhenBookerIsOwner() {
        User owner = createUser("Owner");
        Item item = createItem(owner, true);

        assertThatThrownBy(() ->
                bookingService.create(owner.getId(), createRequest(item.getId())))
                .isInstanceOf(AccessException.class)
                .hasMessage("Нельзя бронировать собственную вещь");
    }

    @Test
    void approve_shouldApproveBooking() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        BookingDto result =
                bookingService.approve(owner.getId(), created.getId(), true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_shouldRejectBooking() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        BookingDto result =
                bookingService.approve(owner.getId(), created.getId(), false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_shouldThrowForAnotherUser() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        User another = createUser("Another");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        assertThatThrownBy(() ->
                bookingService.approve(another.getId(), created.getId(), true))
                .isInstanceOf(AccessException.class);
    }

    @Test
    void approve_shouldThrowWhenBookingAlreadyProcessed() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        bookingService.approve(owner.getId(), created.getId(), true);

        assertThatThrownBy(() ->
                bookingService.approve(owner.getId(), created.getId(), false))
                .isInstanceOf(ParameterNotValidException.class);
    }

    @Test
    void approve_shouldThrowWhenBookingNotFound() {
        User owner = createUser("Owner");

        assertThatThrownBy(() ->
                bookingService.approve(owner.getId(), 999999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldAllowBooker() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        BookingDto result =
                bookingService.getById(booker.getId(), created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
    }

    @Test
    void getById_shouldAllowOwner() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        BookingDto result =
                bookingService.getById(owner.getId(), created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
    }

    @Test
    void getById_shouldThrowForAnotherUser() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        User another = createUser("Another");
        Item item = createItem(owner, true);

        BookingDto created =
                bookingService.create(booker.getId(), createRequest(item.getId()));

        assertThatThrownBy(() ->
                bookingService.getById(another.getId(), created.getId()))
                .isInstanceOf(AccessException.class);
    }

    @Test
    void getById_shouldThrowWhenBookingNotFound() {
        User user = createUser("User");

        assertThatThrownBy(() ->
                bookingService.getById(user.getId(), 999999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingsByBooker_shouldReturnAll() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        bookingService.create(booker.getId(), createRequest(item.getId()));

        List<BookingDto> result =
                bookingService.getBookingsByBooker(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnWaiting() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        bookingService.create(booker.getId(), createRequest(item.getId()));

        List<BookingDto> result =
                bookingService.getBookingsByBooker(
                        booker.getId(),
                        BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus())
                .isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByBooker_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() ->
                bookingService.getBookingsByBooker(999999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingsByOwner_shouldReturnAll() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        bookingService.create(booker.getId(), createRequest(item.getId()));

        List<BookingDto> result =
                bookingService.getBookingsByOwner(owner.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnWaiting() {
        User owner = createUser("Owner");
        User booker = createUser("Booker");
        Item item = createItem(owner, true);

        bookingService.create(booker.getId(), createRequest(item.getId()));

        List<BookingDto> result =
                bookingService.getBookingsByOwner(
                        owner.getId(),
                        BookingState.WAITING);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() ->
                bookingService.getBookingsByOwner(999999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }
}