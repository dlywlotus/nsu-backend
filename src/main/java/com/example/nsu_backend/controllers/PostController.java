package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.CreatePostRequest;
import com.example.nsu_backend.dto.DeletePostRequest;
import com.example.nsu_backend.dto.GetPostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.exceptions.InvalidCategoryException;
import com.example.nsu_backend.services.PostService;
import com.example.nsu_backend.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final AuthUtils authUtils;


    /**
     * Retrieves all posts that match the provided filters, sorts them and then returns them in page format
     * The page, size and sort query params are converted to pageable by spring automatically
     * Note: Only the first "sort" variable will be considered
     * Example usage: /posts?category=HOUSING&page=0&size=20&sort=recent,desc
     *
     * @param category    used to filter posts by category
     * @param searchInput used to filer posts by search input
     * @param authorId    used to show only posts by that author if provided
     * @param pageable    pagination and sorting information
     * @return a list of posts that is filtered and sorted according to the inputs
     */
    @GetMapping
    public List<PostDetails> getPosts(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) String searchInput,
                                      @RequestParam(required = false) UUID authorId,
                                      Pageable pageable) {

        if (!Objects.isNull(category) && Arrays.stream(Category.values()).noneMatch(c -> c.toString().equals(category))) {
            throw new InvalidCategoryException("The provided category must be one of: " + Arrays.toString(Category.values()));
        }

        return postService.getPosts(new GetPostRequest(category, searchInput, authorId, pageable));
    }

    @PostMapping
    public PostDetails createPost(@Valid @RequestBody CreatePostRequest request) {
        if (!authUtils.getCurrentUserId().equals(request.authorId().toString())) {
            throw new AuthorizationDeniedException("Can't create a post on behalf of another user.");
        }

        if (Arrays.stream(Category.values()).noneMatch(c -> c.toString().equals(request.category()))) {
            throw new InvalidCategoryException("The provided category must be one of: " + Arrays.toString(Category.values()));
        }

        return postService.createPost(request);
    }

    @DeleteMapping
    public Map<String, String> deletePost(@Valid @RequestBody DeletePostRequest request) {
        postService.deletePost(request);
        return Map.of("message", "Post " + request.postId() + " has been deleted.");
    }
}
