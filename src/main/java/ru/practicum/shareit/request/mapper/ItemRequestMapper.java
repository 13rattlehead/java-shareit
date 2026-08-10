package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;


public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                request.getItems()
                        .stream()
                        .map(ItemMapper::toItemDto)
                        .toList()
        );

    }
}
