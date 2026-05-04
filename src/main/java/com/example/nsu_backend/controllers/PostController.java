package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.*;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.exceptions.InvalidCategoryException;
import com.example.nsu_backend.services.CommentService;
import com.example.nsu_backend.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    public final CommentService commentService;

    /**
     * Retrieves all posts that match the provided filters, sorts them and then returns them in page format
     * The page, size and sort query params are converted to pageable by spring automatically
     * Only the first "sort" variable will be considered. The default sort is by "created_at" in descending order.
     * By default, no category filters are applied, with page = 0 and size = 20.
     * Example usage: /posts?category=HOUSING&page=0&size=20&sort=created_at,desc
     *
     * @param category    used to filter posts by category
     * @param searchInput used to filer posts by search input
     * @param authorId    used to show only posts by that author if provided
     * @param pageable    pagination and sorting information
     * @return a list of posts that is filtered and sorted according to the inputs
     */
    @GetMapping("posts")
    public List<PostDetails> getPosts(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) String searchInput,
                                      @RequestParam(required = false) UUID authorId,
                                      Pageable pageable) {

        if (!Objects.isNull(category) && Arrays.stream(Category.values()).noneMatch(c -> c.toString().equals(category))) {
            throw new InvalidCategoryException("The provided category must be one of: " + Arrays.toString(Category.values()));
        }

        return postService.getPosts(new GetPostRequest(category, searchInput, authorId, pageable));
    }

    @GetMapping("post/{postId}")
    public PostDetails getPost(@PathVariable UUID postId) {
        return postService.getPost(postId);
    }

    @PostMapping("post")
    public PostDetails createPost(@Valid @RequestBody AddPostRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("post")
    public PostDetails updatePost(@Valid @RequestBody UpdatePostRequest request) {
        return postService.updatePost(request);
    }

    @DeleteMapping("post/{postId}")
    public Map<String, String> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return Map.of("message", "The post has been deleted successfully!.");
    }

    @GetMapping("post/{id}/comments")
    public List<CommentDetails> getPostComments(@PathVariable UUID id) {
        return commentService.getCommentsByPost(id);
    }
}
