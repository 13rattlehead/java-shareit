package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import lombok.*;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long userId;

    @Email(message = "Email должен содержать @")
    private String email;

    private String name;

}
