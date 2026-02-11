package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.LikeDetails;
import com.example.nsu_backend.dto.LikeRequest;
import com.example.nsu_backend.entities.Like;
import com.example.nsu_backend.mappers.LikeMapper;
import com.example.nsu_backend.repositories.LikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final LikeMapper likeMapper;

    public LikeDetails addLike(LikeRequest request) {
        Like like = likeMapper.likeRequestToLike(request);
        Like newLike = likeRepository.save(like);
        return likeMapper.likeToLikeDto(newLike);
    }

    @Transactional
    public void removeLike(LikeRequest request) {
        likeRepository.deleteByUserIdAndPostId(request.userId(), request.postId());
    }
}
