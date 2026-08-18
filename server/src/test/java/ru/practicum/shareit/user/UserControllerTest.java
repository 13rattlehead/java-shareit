package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnOk() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userService).getAllUsers();
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(userService.getById(1L))
                .thenReturn(new UserDto(1L, "Denis", "denis@test.ru"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        verify(userService).getById(1L);
    }

    @Test
    void create_shouldReturnOk() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenReturn(new UserDto(1L, "Denis", "denis@test.ru"));

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Denis",
                                  "email": "denis@test.ru"
                                }
                                """))
                .andExpect(status().isOk());

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        when(userService.update(
                any(Long.class),
                any(UserDto.class)))
                .thenReturn(new UserDto(1L, "New Denis", "new@test.ru"));

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "New Denis",
                                  "email": "new@test.ru"
                                }
                                """))
                .andExpect(status().isOk());

        verify(userService).update(
                any(Long.class),
                any(UserDto.class));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }
}