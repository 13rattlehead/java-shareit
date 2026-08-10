package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Repository
public class ItemRepositoryImpl implements ItemReposiroty {

    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1L;

    @Override
    public Item create(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {

        if (!items.containsKey(item.getId())) {
            return null;
        }

        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getById(Long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> getAll() {
        return items.values();
    }

    @Override
    public Collection<Item> getItemsByOwner(Long ownerId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwner().getUserId().equals(ownerId))
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        String searchText = text.toLowerCase();

        return items.values()
                .stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(searchText)
                        || item.getDescription().toLowerCase().contains(searchText))
                .toList();

    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }
}
