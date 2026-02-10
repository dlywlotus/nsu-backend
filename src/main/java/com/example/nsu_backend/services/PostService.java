package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.CreatePostRequest;
import com.example.nsu_backend.dto.DeletePostRequest;
import com.example.nsu_backend.dto.GetPostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.mappers.PostMapper;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final JdbcClient jdbcClient;

    public PostDetails createPost(CreatePostRequest request) {
        Post post = postMapper.postDtoToNewPost(request, userRepository.getReferenceById(request.authorId()));
        Post newPost = postRepository.save(post);
        return postMapper.postToPostDto(newPost);
    }

    public void deletePost(DeletePostRequest request) {
        postRepository.deleteById(request.postId());
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
}
