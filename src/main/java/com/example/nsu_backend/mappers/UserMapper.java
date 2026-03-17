package com.example.nsu_backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.entities.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDetails userToUserDto(User user);
}