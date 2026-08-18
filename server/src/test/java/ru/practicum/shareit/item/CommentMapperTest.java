package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toDto_shouldMapComment() {
        User author = new User();
        author.setId(10L);
        author.setName("Denis");

        LocalDateTime created =
                LocalDateTime.of(2026, 8, 18, 15, 30);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная вещь!");
        comment.setAuthor(author);
        comment.setCreated(created);

        CommentDto result = CommentMapper.toDto(comment);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Отличная вещь!");
        assertThat(result.getAuthorName()).isEqualTo("Denis");
        assertThat(result.getCreated()).isEqualTo(created);
    }
}