package ru.practicum.shareit.comments.dto;

import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.comments.Comment;

public interface CommentMapper {

    CommentMapper COMMENT_MAPPER = Mappers.getMapper(CommentMapper.class);

    Comment toComment(CommentRequestDto dto);

    CommentResponseDto toDto(Comment comment);

}
