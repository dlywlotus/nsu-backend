package com.example.nsu_backend.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.entities.Comment;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(target = "postId", source = "comment.post.id")
    @Mapping(target = "authorId", source = "comment.author.id")
    @Mapping(target = "parentCommentId", source = "comment.parentComment.id")
    @Mapping(target = "username", source = "comment.author.username")
    @Mapping(target = "profileIconImageKey", source = "comment.author.profileIconImageKey")
    @Mapping(target = "nestedComments", ignore = true)
    CommentDetails commentToCommentDto(Comment comment);

    @Mapping(target = "nestedComments", source = "nestedComments")
    CommentDetails commentDtoToParentCommentDto(CommentDetails comment, List<CommentDetails> nestedComments);

}