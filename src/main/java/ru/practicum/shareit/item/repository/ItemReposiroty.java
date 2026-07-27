package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;


public interface ItemReposiroty {

    Item create(Item item);

    Item update(Item item);

    Item getById(Long id);

    Collection<Item> getAll();

    Collection<Item> search(String text);

    Collection<Item> getItemsByOwner(Long ownerId);

    void delete(Long id);
}
