package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ParameterNotValidException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldCreateUser() {
        UserDto dto = new UserDto();
        dto.setName("Denis");
        dto.setEmail("denis" + System.nanoTime() + "@test.ru");

        UserDto result = userService.create(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Denis");
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void create_shouldThrowWhenEmailIsNull() {
        UserDto dto = new UserDto();
        dto.setName("Denis");
        dto.setEmail(null);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ParameterNotValidException.class)
                .hasMessage("Ваш email не может быть пустым");
    }

    @Test
    void create_shouldThrowWhenEmailIsBlank() {
        UserDto dto = new UserDto();
        dto.setName("Denis");
        dto.setEmail("   ");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ParameterNotValidException.class)
                .hasMessage("Ваш email не может быть пустым");
    }

    @Test
    void create_shouldThrowWhenEmailDoesNotContainAt() {
        UserDto dto = new UserDto();
        dto.setName("Denis");
        dto.setEmail("denis-test.ru");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ParameterNotValidException.class)
                .hasMessage("Неправильный ввод почты, она должна содрежать знак @");
    }

    @Test
    void create_shouldThrowWhenEmailAlreadyExists() {
        String email = "duplicate" + System.nanoTime() + "@test.ru";

        User user = new User();
        user.setName("Existing");
        user.setEmail(email);
        userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setName("Another");
        dto.setEmail(email);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Данный email: " + email + " уже зарегистрирован");
    }

    @Test
    void update_shouldUpdateNameAndEmail() {
        User user = new User();
        user.setName("Old name");
        user.setEmail("old" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        UserDto updateDto = new UserDto();
        updateDto.setName("New name");
        updateDto.setEmail("new" + System.nanoTime() + "@test.ru");

        UserDto result =
                userService.update(savedUser.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getEmail()).isEqualTo(updateDto.getEmail());
    }

    @Test
    void update_shouldKeepNameWhenNameIsBlank() {
        User user = new User();
        user.setName("Original");
        user.setEmail("original" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        UserDto updateDto = new UserDto();
        updateDto.setName("   ");

        UserDto result =
                userService.update(savedUser.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Original");
        assertThat(result.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        UserDto dto = new UserDto();
        dto.setName("Denis");

        assertThatThrownBy(() ->
                userService.update(999999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void update_shouldThrowWhenEmailBelongsToAnotherUser() {
        User first = new User();
        first.setName("First");
        first.setEmail("first" + System.nanoTime() + "@test.ru");

        User second = new User();
        second.setName("Second");
        second.setEmail("second" + System.nanoTime() + "@test.ru");

        User savedFirst = userRepository.save(first);
        userRepository.save(second);

        UserDto updateDto = new UserDto();
        updateDto.setEmail(second.getEmail());

        assertThatThrownBy(() ->
                userService.update(savedFirst.getId(), updateDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Данный email уже зарегестрирован");
    }

    @Test
    void update_shouldAllowKeepingOwnEmail() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        UserDto updateDto = new UserDto();
        updateDto.setEmail(savedUser.getEmail());

        UserDto result =
                userService.update(savedUser.getId(), updateDto);

        assertThat(result.getEmail())
                .isEqualTo(savedUser.getEmail());
    }

    @Test
    void getById_shouldReturnUser() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        UserDto result =
                userService.getById(savedUser.getId());

        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getName()).isEqualTo("Denis");
        assertThat(result.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void getById_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() ->
                userService.getById(999999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnUsers() {
        User first = new User();
        first.setName("First");
        first.setEmail("first" + System.nanoTime() + "@test.ru");

        User second = new User();
        second.setName("Second");
        second.setEmail("second" + System.nanoTime() + "@test.ru");

        userRepository.save(first);
        userRepository.save(second);

        Collection<UserDto> result =
                userService.getAllUsers();

        assertThat(result)
                .extracting(UserDto::getName)
                .contains("First", "Second");
    }

    @Test
    void delete_shouldDeleteUser() {
        User user = new User();
        user.setName("Denis");
        user.setEmail("denis" + System.nanoTime() + "@test.ru");

        User savedUser = userRepository.save(user);

        userService.delete(savedUser.getId());

        assertThat(userRepository.existsById(savedUser.getId()))
                .isFalse();
    }

    @Test
    void delete_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() ->
                userService.delete(999999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }
}