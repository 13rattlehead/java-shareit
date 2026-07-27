package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;


public interface UserRepository {

    Collection<User> findAllUsers();

    User create(User user);

    User update(User user);

    User getById(long id);

    void delete(long userId);
}
