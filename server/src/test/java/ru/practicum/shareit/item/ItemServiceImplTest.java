package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import ru.practicum.shareit.exception.AccessException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void create_withRequestId_shouldSaveItemWithRequest() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");
        user = userRepository.save(user);

        ItemRequestCreateDto requestCreateDto = new ItemRequestCreateDto();
        requestCreateDto.setDescription("Нужна дрель");

        ItemRequestDto request =
                itemRequestService.create(user.getId(), requestCreateDto);

        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Дрель Bosch");
        itemCreateDto.setDescription("Аккумуляторная дрель");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(request.getId());

        ItemDto result =
                itemService.create(user.getId(), itemCreateDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Дрель Bosch");

        Item savedItem = itemRepository.findById(result.getId())
                .orElseThrow();

        assertThat(savedItem.getRequest()).isNotNull();
        assertThat(savedItem.getRequest().getId())
                .isEqualTo(request.getId());
    }

    @Test
    void create_withoutRequestId_shouldSaveItemWithoutRequest() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");
        user = userRepository.save(user);

        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Молоток");
        itemCreateDto.setDescription("Обычный молоток");
        itemCreateDto.setAvailable(true);

        ItemDto result =
                itemService.create(user.getId(), itemCreateDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Молоток");

        Item savedItem = itemRepository.findById(result.getId())
                .orElseThrow();

        assertThat(savedItem.getRequest()).isNull();
    }

    @Test
    void update_shouldUpdateItemForOwner() {
        User owner = new User();
        owner.setName("Denis");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");

        User savedOwner = userRepository.save(owner);

        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Старая дрель");
        createDto.setDescription("Старое описание");
        createDto.setAvailable(true);

        ItemDto created =
                itemService.create(savedOwner.getId(), createDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Новая дрель")
                .description("Новое описание")
                .available(false)
                .build();

        ItemDto updated =
                itemService.update(
                        savedOwner.getId(),
                        created.getId(),
                        updateDto
                );

        assertThat(updated.getName()).isEqualTo("Новая дрель");
        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getAvailable()).isFalse();

        Item savedItem = itemRepository.findById(created.getId())
                .orElseThrow();

        assertThat(savedItem.getName()).isEqualTo("Новая дрель");
        assertThat(savedItem.getDescription()).isEqualTo("Новое описание");
        assertThat(savedItem.getAvailable()).isFalse();
    }

    @Test
    void update_shouldThrowAccessExceptionForAnotherUser() {
        User owner = new User();
        owner.setName("Denis");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");

        User savedOwner = userRepository.save(owner);

        User anotherUser = new User();
        anotherUser.setName("Ivan");
        anotherUser.setEmail("another" + System.nanoTime() + "@test.ru");

        User savedAnotherUser = userRepository.save(anotherUser);

        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Дрель");
        createDto.setDescription("Описание");
        createDto.setAvailable(true);

        ItemDto created =
                itemService.create(savedOwner.getId(), createDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Чужая дрель")
                .description("Попытка изменить")
                .available(false)
                .build();

        assertThatThrownBy(() ->
                itemService.update(
                        savedAnotherUser.getId(),
                        created.getId(),
                        updateDto
                ))
                .isInstanceOf(AccessException.class)
                .hasMessage("Только владелец может обновлять свои вещи");
    }

    @Test
    void getById_shouldReturnItemWithComments() {
        User owner = new User();
        owner.setName("Denis");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");
        User savedOwner = userRepository.save(owner);

        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Дрель");
        createDto.setDescription("Аккумуляторная дрель");
        createDto.setAvailable(true);

        ItemDto created =
                itemService.create(savedOwner.getId(), createDto);

        ItemDto result =
                itemService.getById(created.getId(), savedOwner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription())
                .isEqualTo("Аккумуляторная дрель");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void getItemsByOwner_shouldReturnOwnerItems() {
        User owner = new User();
        owner.setName("Denis");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");
        User savedOwner = userRepository.save(owner);

        ItemCreateDto firstItem = new ItemCreateDto();
        firstItem.setName("Дрель");
        firstItem.setDescription("Аккумуляторная дрель");
        firstItem.setAvailable(true);

        ItemCreateDto secondItem = new ItemCreateDto();
        secondItem.setName("Молоток");
        secondItem.setDescription("Обычный молоток");
        secondItem.setAvailable(true);

        itemService.create(savedOwner.getId(), firstItem);
        itemService.create(savedOwner.getId(), secondItem);

        Collection<ItemDto> result =
                itemService.getItemsByOwner(savedOwner.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void search_shouldReturnAvailableMatchingItems() {
        User owner = new User();
        owner.setName("Denis");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");
        User savedOwner = userRepository.save(owner);

        ItemCreateDto availableItem = new ItemCreateDto();
        availableItem.setName("Дрель Bosch");
        availableItem.setDescription("Аккумуляторная дрель");
        availableItem.setAvailable(true);

        ItemCreateDto unavailableItem = new ItemCreateDto();
        unavailableItem.setName("Дрель Makita");
        unavailableItem.setDescription("Другая дрель");
        unavailableItem.setAvailable(false);

        itemService.create(savedOwner.getId(), availableItem);
        itemService.create(savedOwner.getId(), unavailableItem);

        Collection<ItemDto> result =
                itemService.search("дрель");

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName())
                .isEqualTo("Дрель Bosch");
    }

    @Test
    void addComment_shouldSaveCommentForUserWhoRentedItem() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker" + System.nanoTime() + "@test.ru");
        User savedBooker = userRepository.save(booker);

        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Дрель");
        itemCreateDto.setDescription("Аккумуляторная дрель");
        itemCreateDto.setAvailable(true);

        ItemDto createdItem =
                itemService.create(savedOwner.getId(), itemCreateDto);

        Booking booking = new Booking();
        booking.setItem(itemRepository.findById(createdItem.getId()).orElseThrow());
        booking.setBooker(savedBooker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(booking);

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Отличная дрель!");

        CommentDto result = itemService.addComment(
                savedBooker.getId(),
                createdItem.getId(),
                commentRequestDto
        );

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Отличная дрель!");

        List<Comment> comments =
                commentRepository.findByItemIdOrderByCreatedAsc(
                        createdItem.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getText())
                .isEqualTo("Отличная дрель!");
        assertThat(comments.get(0).getAuthor().getId())
                .isEqualTo(savedBooker.getId());
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);

        assertThatThrownBy(() ->
                itemService.create(999999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrowWhenRequestNotFound() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);
        dto.setRequestId(999999L);

        assertThatThrownBy(() ->
                itemService.create(savedUser.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenItemNotFound() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(true)
                .build();

        assertThatThrownBy(() ->
                itemService.update(savedUser.getId(), 999999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldThrowWhenItemNotFound() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        assertThatThrownBy(() ->
                itemService.getById(999999L, savedUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void search_shouldReturnEmptyForBlankText() {
        assertThat(itemService.search("")).isEmpty();
        assertThat(itemService.search("   ")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
    }

    @Test
    void getById_shouldReturnLastAndNextBookingForOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");

        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker" + System.nanoTime() + "@test.ru");

        User savedBooker = userRepository.save(booker);

        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);

        ItemDto created = itemService.create(savedOwner.getId(), dto);

        Item item = itemRepository.findById(created.getId()).orElseThrow();

        Booking past = new Booking();
        past.setItem(item);
        past.setBooker(savedBooker);
        past.setStart(LocalDateTime.now().minusDays(2));
        past.setEnd(LocalDateTime.now().minusDays(1));
        past.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(past);

        Booking future = new Booking();
        future.setItem(item);
        future.setBooker(savedBooker);
        future.setStart(LocalDateTime.now().plusDays(1));
        future.setEnd(LocalDateTime.now().plusDays(2));
        future.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(future);

        ItemDto result =
                itemService.getById(created.getId(), savedOwner.getId());

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    void addComment_shouldThrowWhenUserNotFound() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Комментарий");

        assertThatThrownBy(() ->
                itemService.addComment(999999L, 999999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_shouldThrowWhenItemNotFound() {
        User user = new User();
        user.setName("Booker");
        user.setEmail("booker" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Комментарий");

        assertThatThrownBy(() ->
                itemService.addComment(savedUser.getId(), 999999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_shouldThrowWhenUserDidNotRentItem() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner" + System.nanoTime() + "@test.ru");

        User savedOwner = userRepository.save(owner);

        User user = new User();
        user.setName("User");
        user.setEmail("user" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(true);

        ItemDto item = itemService.create(savedOwner.getId(), itemDto);

        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Комментарий");

        assertThatThrownBy(() ->
                itemService.addComment(
                        savedUser.getId(),
                        item.getId(),
                        commentDto))
                .isInstanceOf(jakarta.validation.ValidationException.class);
    }
}