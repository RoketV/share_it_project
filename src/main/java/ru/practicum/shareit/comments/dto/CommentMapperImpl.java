package ru.practicum.shareit.comments.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.comments.Comment;

@Component
public class CommentMapperImpl implements CommentMapper {


    @Override
    public Comment toComment(CommentInputDto dto) {
        if (dto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setText(dto.getText());
        return comment;
    }

    @Override
    public CommentOutputDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentOutputDto dto = new CommentOutputDto();
        dto.setId(comment.getId());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}
