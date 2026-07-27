package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ParameterNotValidException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public User create(User user) {
        user.setUserId(nextId++);
        users.put(user.getUserId(), user);
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
    }

    @Override
    public User getById(long id) {

        if (!users.containsKey(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        return users.get(id);
    }

    @Override
    public User update(User user) {

        if (!users.containsKey(user.getUserId())) {
            throw new NotFoundException("Пользователь не найден");
        }

        users.put(user.getUserId(), user);
        return user;
    }

}
