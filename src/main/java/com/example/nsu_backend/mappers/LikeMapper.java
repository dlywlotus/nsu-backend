package com.example.nsu_backend.mappers;

import com.example.nsu_backend.dto.LikeDetails;
import com.example.nsu_backend.dto.LikeRequest;
import com.example.nsu_backend.entities.Like;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LikeMapper {
    LikeDetails likeToLikeDto(Like like);

    @Mapping(target = "id", ignore = true)
    Like likeRequestToLike(LikeRequest request);
}
