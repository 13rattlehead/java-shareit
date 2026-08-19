package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.*;

import static org.assertj.core.api.Assertions.assertThat;


class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handleNotFoundException_shouldReturnMessage() {
        ErrorResponse result =
                handler.handleNotFoundException(
                        new NotFoundException("Не найден"));

        assertThat(result.getError())
                .isEqualTo("Не найден");
    }

    @Test
    void handleAccessException_shouldReturnMessage() {
        ErrorResponse result =
                handler.handleAccessException(
                        new AccessException("Нет доступа"));

        assertThat(result.getError())
                .isEqualTo("Нет доступа");
    }

    @Test
    void handleParameterNotValidException_shouldReturnMessage() {
        ErrorResponse result =
                handler.handleParameterNotValidException(
                        new ParameterNotValidException("Некорректные данные"));

        assertThat(result.getError())
                .isEqualTo("Некорректные данные");
    }

    @Test
    void handleDuplicateException_shouldReturnMessage() {
        ErrorResponse result =
                handler.handleDuplicateException(
                        new DuplicateEmailException("Email занят"));

        assertThat(result.getError())
                .isEqualTo("Email занят");
    }

    @Test
    void handleValidationException_shouldReturnValidationMessage() {
        ErrorResponse result =
                handler.handleValidationException(
                        new MethodArgumentNotValidException("Ошибка"));

        assertThat(result.getError())
                .isEqualTo("Ошибка валидации данных");
    }

    @Test
    void handleThrowable_shouldReturnMessage() {
        ErrorResponse result =
                handler.handleThrowable(
                        new RuntimeException("Внутренняя ошибка"));

        assertThat(result.getError())
                .isEqualTo("Внутренняя ошибка");
    }
}