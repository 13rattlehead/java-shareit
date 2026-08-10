package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemReposiroty;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemReposiroty itemReposiroty;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.getById(ownerId);

        if (owner == null) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не найден");
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        return ItemMapper.toItemDto(itemReposiroty.create(item));

    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemReposiroty.getById(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена");
        }

        if (!(existingItem.getOwner().getUserId().equals(ownerId))) {
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

        return ItemMapper.toItemDto(itemReposiroty.update(existingItem));
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemReposiroty.getById(id);

        if (item == null) {
            throw new NotFoundException("Вещь с id = " + id + " не найдена");
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> getItemsByOwner(Long ownerId) {
        return itemReposiroty.getItemsByOwner(ownerId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return java.util.Collections.emptyList();
        }

        return itemReposiroty.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
