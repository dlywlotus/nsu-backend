package com.example.nsu_backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.entities.Post;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {
    @Mapping(target = "authorId", source = "post.author.id")
    PostDetails postToPostDto(Post post);
}
