package com.example.nsu_backend.mappers;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {
    @Mapping(target = "authorId", source = "post.author.id")
    PostDetails postToPostDto(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "likeCount", constant = "0")
    Post addPostDtoToNewPost(AddPostRequest dto, User author);
}
