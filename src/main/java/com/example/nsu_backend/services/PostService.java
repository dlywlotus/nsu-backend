package com.example.nsu_backend.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.DeletePostRequest;
import com.example.nsu_backend.dto.GetPostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.dto.UpdatePostRequest;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.exceptions.InvalidCategoryException;
import com.example.nsu_backend.mappers.PostMapper;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;
import com.example.nsu_backend.utils.AuthUtils;

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
    private final AuthUtils authUtils;

    public PostDetails createPost(AddPostRequest request) {
        if (Arrays.stream(Category.values()).noneMatch(c -> c.toString().equals(request.getCategory()))) {
            throw new InvalidCategoryException("The provided category must be one of: " + Arrays.toString(Category.values()));
        }

        Post post = postMapper.addPostDtoToNewPost(request, userRepository.getReferenceById(authUtils.getCurrentUserId()));
        Post newPost = postRepository.save(post);
        return postMapper.postToPostDto(newPost);
    }

    public PostDetails updatePost(UpdatePostRequest request) {
        if (Objects.nonNull(request.getCategory()) &&
                Arrays.stream(Category.values()).noneMatch(c -> c.toString().equals(request.getCategory()))) {
            throw new InvalidCategoryException("The provided category must be one of: " + Arrays.toString(Category.values()));
        }

        Post oldPost = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("The post does not exist"));
        Post post = Post.builder()
                .id(request.getPostId())
                .title(Optional.ofNullable(request.getTitle()).orElse(oldPost.getTitle()))
                .body(Optional.ofNullable(request.getBody()).orElse(oldPost.getBody()))
                .category(Optional.ofNullable(request.getCategory()).orElse(oldPost.getCategory()))
                .author(userRepository.getReferenceById(authUtils.getCurrentUserId()))
                .createdAt(oldPost.getCreatedAt())
                .build();
        Post newPost = postRepository.save(post);
        return postMapper.postToPostDto(newPost);
    }

    public List<PostDetails> getPosts(GetPostRequest request) {
        HashMap<String, Object> paramMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM posts p WHERE p.id IS NOT NULL");

        if (!Objects.isNull(request.category())) {
            paramMap.put("category", request.category());
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

        String[] sortInputs = request.pageable().getSort().toString().split(",")[0].split(":");
        String sortBy = sortInputs[0].strip();
        String sortDirection = sortInputs[1].strip();

        if (sortBy.equals("recent")) {
            sqlBuilder.append(" ORDER BY p.created_at ");
        } else {
            sqlBuilder.append(" ORDER BY p.likes ");
        }

        paramMap.put("limit", request.pageable().getPageSize());
        paramMap.put("offset", request.pageable().getOffset());

        sqlBuilder.append(sortDirection).append(" LIMIT :limit OFFSET :offset");
        String sql = sqlBuilder.toString();

        return jdbcClient.sql(sql)
                .params(paramMap)
                .query(PostDetails.class).list();
    }

    public void deletePost(DeletePostRequest request) {
        postRepository.findByPostAndAuthorId(request.postId(), authUtils.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("The post does not exist"));
        postRepository.deleteById(request.postId());
    }
}
