package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ParameterNotValidException;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {

        String email = userDto.getEmail();

        if (email == null || email.isBlank()) {
            throw new ParameterNotValidException("Email cannot be empty");
        }

        if (!email.contains("@")) {
            throw new ParameterNotValidException("Email is invalid");
        }

        boolean existsEmail = userRepository.findAllUsers().stream()
                .anyMatch(user -> user.getEmail().equals(userDto.getEmail()));

        if (existsEmail) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.create(user));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {

        User existingUser = userRepository.getById(userId);

        if (existingUser == null) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {

            boolean exists = userRepository.findAllUsers().stream()
                    .anyMatch(user ->
                            user.getUserId() != userId &&
                                    user.getEmail().equals(userDto.getEmail()));

            if (exists) {
                throw new DuplicateEmailException("Email already exists");
            }

            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.update(existingUser);

        if (updatedUser == null) {
            throw new NotFoundException("Не удалось обвновить пользователя");
        }

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(long userId) {
        User user = userRepository.getById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }

        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(long userId) {
        userRepository.delete(userId);
    }

}
