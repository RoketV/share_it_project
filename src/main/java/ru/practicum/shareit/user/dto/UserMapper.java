package ru.practicum.shareit.user.dto;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.model.User;

@Mapper
public interface UserMapper {


    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    User toUser(UserDto dto);

}
