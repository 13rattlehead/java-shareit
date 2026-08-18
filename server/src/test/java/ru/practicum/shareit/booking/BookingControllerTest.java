package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnOk() throws Exception {
        when(bookingService.create(any(), any()))
                .thenReturn(new BookingDto());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("""
                                {
                                  "itemId": 1,
                                  "start": "2026-08-18T16:00:00",
                                  "end": "2026-08-18T17:00:00"
                                }
                                """))
                .andExpect(status().isOk());

        verify(bookingService).create(any(), any());
    }

    @Test
    void approve_shouldReturnOk() throws Exception {
        when(bookingService.approve(1L, 2L, true))
                .thenReturn(new BookingDto());

        mockMvc.perform(patch("/bookings/2")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingService).approve(1L, 2L, true);
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(bookingService.getById(1L, 2L))
                .thenReturn(new BookingDto());

        mockMvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(bookingService).getById(1L, 2L);
    }

    @Test
    void getBookingsByBooker_shouldReturnOk() throws Exception {
        when(bookingService.getBookingsByBooker(1L, BookingState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingService)
                .getBookingsByBooker(1L, BookingState.ALL);
    }

    @Test
    void getBookingsByOwner_shouldReturnOk() throws Exception {
        when(bookingService.getBookingsByOwner(1L, BookingState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingService)
                .getBookingsByOwner(1L, BookingState.ALL);
    }
}