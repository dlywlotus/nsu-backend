package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.GetPostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.dto.UpdatePostRequest;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.mappers.PostMapper;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final JdbcClient jdbcClient;
    private final UserService userService;

    public PostDetails getPost(UUID postId) {
        return postRepository.findById(postId).map(postMapper::postToPostDto).orElseThrow(() -> new ApiException("Post not found"));
    }

    public PostDetails createPost(AddPostRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .category(request.getCategory())
                .likeCount(0)
                .author(userRepository.getReferenceById(userService.getCurrentUserId())).build();
        Post newPost = postRepository.save(post);
        return postMapper.postToPostDto(newPost);
    }

    public PostDetails updatePost(UpdatePostRequest request) {
        Post oldPost = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("The post does not exist"));
        Post post = Post.builder()
                .id(request.getPostId())
                .title(Optional.ofNullable(request.getTitle()).orElse(oldPost.getTitle()))
                .body(Optional.ofNullable(request.getBody()).orElse(oldPost.getBody()))
                .category(Optional.ofNullable(request.getCategory()).orElse(oldPost.getCategory()))
                .author(userRepository.getReferenceById(userService.getCurrentUserId()))
                .createdAt(oldPost.getCreatedAt())
                .build();
        Post newPost = postRepository.save(post);
        return postMapper.postToPostDto(newPost);
    }

    public List<PostDetails> getPosts(GetPostRequest request) {
        HashMap<String, Object> paramMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM posts p WHERE p.id IS NOT NULL");

        if (!Objects.isNull(request.category())) {
            paramMap.put("category", request.category().name());
            sqlBuilder.append(" AND p.category = :category");
        }

        if (!Objects.isNull(request.searchInput())) {
            paramMap.put("searchInput", request.searchInput());
            sqlBuilder.append(" AND p.search_vector @@ to_tsquery('english', :searchInput)");
        }

        if (!Objects.isNull(request.authorId())) {
            paramMap.put("authorId", request.authorId());
            sqlBuilder.append(" AND p.author_id = :authorId");
        }

        Sort.Order order = request.pageable().getSort().stream().findFirst()
                .orElse(new Sort.Order(Sort.Direction.DESC, "createdAt"));

        if (order.getProperty().equals("createdAt")) {
            sqlBuilder.append(" ORDER BY p.created_at ");
        } else {
            sqlBuilder.append(" ORDER BY p.like_count ");
        }

        paramMap.put("limit", request.pageable().getPageSize());
        paramMap.put("offset", request.pageable().getOffset());
        sqlBuilder.append(order.getDirection()).append(" LIMIT :limit OFFSET :offset");
        String sql = sqlBuilder.toString();

        return jdbcClient.sql(sql)
                .params(paramMap)
                .query(PostDetails.class).list();
    }

    public void deletePost(UUID postId) {
        postRepository.findByPostAndAuthorId(postId, userService.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("The post does not exist"));
        postRepository.deleteById(postId);
    }

    public void updateLikeCount(UUID postId, boolean isAdd) {
        if (isAdd) {
            postRepository.incrementLikeCount(postId);
        } else {
            postRepository.decrementLikeCount(postId);
        }
    }
}
