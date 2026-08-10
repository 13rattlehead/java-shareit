package ru.practicum.shareit.item.service;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingShortMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(Long ownerId, @Valid ItemRequestDto itemDto) {
        User owner = userRepository.findById(ownerId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден")
        );

        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);

        return ItemMapper.toItemDto(itemRepository.save(item));

    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {

        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Вещь не найдена")
        );

        if (!(existingItem.getOwner().getId().equals(ownerId))) {
            throw new AccessException("Только владелец может обновлять свои вещи");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Вещь не найдена"));

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(itemId)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {

            Booking last = bookingRepository
                    .findFirstByItemIdAndStartBeforeOrderByStartDesc(
                            itemId,
                            LocalDateTime.now())
                    .orElse(null);

            Booking next = bookingRepository
                    .findFirstByItemIdAndStartAfterOrderByStartAsc(
                            itemId,
                            LocalDateTime.now())
                    .orElse(null);

            lastBooking = BookingShortMapper.toDto(last);
            nextBooking = BookingShortMapper.toDto(next);
        }

        return ItemMapper.toItemDto(
                item,
                lastBooking,
                nextBooking,
                comments
        );
    }

    @Override
    public Collection<ItemDto> getItemsByOwner(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> search(String text) {

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Вещь не найдена")
        );

        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId,
                itemId,
                BookingStatus.APPROVED,
                LocalDateTime.now())) {

            throw new ValidationException(
                    "Оставить комментарий может только пользователь, который арендовал вещь");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
