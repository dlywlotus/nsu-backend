package com.example.nsu_backend.services;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.GetPostRequest;
import com.example.nsu_backend.dto.PageOfPosts;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.dto.UpdatePostRequest;
import com.example.nsu_backend.dto.VerbosePostDetails;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.exceptions.RateLimitException;
import com.example.nsu_backend.mappers.PostMapper;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final JdbcClient jdbcClient;
    private final UserService userService;
    private final RateLimitingService rateLimitingService;

    public VerbosePostDetails getPost(UUID postId) {
        String sql = """
                SELECT p.*, u.username, u.profile_icon_image_key,
                EXISTS (
                    SELECT 1
                    FROM likes
                    WHERE user_id = :userId
                    AND post_id = p.id
                ) as user_liked
                FROM posts p
                JOIN users u ON p.author_id = u.id
                WHERE p.id = :postId
                """;

        return jdbcClient.sql(sql)
                .param("postId", postId)
                .param("userId", userService.getCurrentUserId())
                .query(VerbosePostDetails.class)
                .optional()
                .orElseThrow(() -> new ApiException("Post not found"));
    }

    public PostDetails createPost(AddPostRequest request) {
        String userId = userService.getCurrentUserId().toString();
        if (!rateLimitingService.resolveCreatePostBucket(userId).tryConsume(1)) {
            throw new RateLimitException("Too many requests");
        }

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

    public PageOfPosts getPosts(GetPostRequest request) {
        StringBuilder postsQuery = new StringBuilder("""
                SELECT p.*, u.username, u.profile_icon_image_key,
                EXISTS (
                    SELECT 1
                    FROM likes
                    WHERE user_id = :userId
                    AND post_id = p.id
                ) as user_liked
                FROM posts p
                JOIN users u ON p.author_id = u.id
                WHERE p.id IS NOT NULL
                """);

        StringBuilder countQuery = new StringBuilder("""
                SELECT COUNT(*)
                FROM posts p
                JOIN users u ON p.author_id = u.id
                WHERE p.id IS NOT NULL
                """);

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userService.getCurrentUserId());


        if (!Objects.isNull(request.category())) {
            paramMap.put("category", request.category().name());
            postsQuery.append(" AND p.category = :category");
            countQuery.append(" AND p.category = :category");
        }

        if (!Objects.isNull(request.searchInput())) {
            paramMap.put("searchInput", request.searchInput());
            postsQuery.append(" AND p.search_vector @@ plainto_tsquery('english', :searchInput)");
            countQuery.append(" AND p.search_vector @@ plainto_tsquery('english', :searchInput)");
        }

        if (!Objects.isNull(request.authorId())) {
            paramMap.put("authorId", request.authorId());
            postsQuery.append(" AND p.author_id = :authorId");
            countQuery.append(" AND p.author_id = :authorId");
        }

        Sort.Order order = request.pageable().getSort().stream().findFirst()
                .orElse(new Sort.Order(Sort.Direction.DESC, "createdAt"));

        if (order.getProperty().equals("createdAt")) {
            postsQuery.append(" ORDER BY p.created_at ");
        } else {
            postsQuery.append(" ORDER BY p.like_count ");
        }

        paramMap.put("limit", request.pageable().getPageSize());
        paramMap.put("offset", request.pageable().getOffset());
        postsQuery.append(order.getDirection()).append(" LIMIT :limit OFFSET :offset");


        List<VerbosePostDetails> postDetails = jdbcClient
                .sql(postsQuery.toString())
                .params(paramMap)
                .query(VerbosePostDetails.class).list();

        long numPosts = jdbcClient
                .sql(countQuery.toString())
                .params(paramMap)
                .query(Long.class).single();

        return new PageOfPosts(
                request.pageable().getPageNumber(),
                (int) Math.ceil((double) numPosts / request.pageable().getPageSize()),
                postDetails);
    }

    public void deletePost(UUID postId) {
        postRepository.findByPostAndAuthorId(postId, userService.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("The post does not exist"));
        postRepository.deleteById(postId);
    }

    public void updateLikeCount(UUID postId, int change) {
        postRepository.updateLikeCount(postId, change);
    }

    public void updateCommentCount(UUID postId, int change) {
        postRepository.updateCommentCount(postId, change);
    }
}
