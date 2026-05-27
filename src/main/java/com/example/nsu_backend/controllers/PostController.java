package com.example.nsu_backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.dto.GetPostRequest;
import com.example.nsu_backend.dto.MessageResponse;
import com.example.nsu_backend.dto.PageOfPosts;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.dto.UpdatePostRequest;
import com.example.nsu_backend.dto.VerbosePostDetails;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.services.CommentService;
import com.example.nsu_backend.services.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {
    public final CommentService commentService;
    private final PostService postService;

    /**
     * Retrieves all posts that match the provided filters, sorts them and then returns them in page format
     * The page, size and sort query params are converted to pageable by spring automatically
     * Only the first "sort" variable will be considered. The default sort is by "created_at" in descending order.
     * By default, no category filters are applied, with page = 0 and size = 20.
     * Example usage: /posts?category=HOUSING&page=0&size=20&sort=createdAt,desc
     *
     * @param category    used to filter posts by category
     * @param searchInput used to filer posts by search input
     * @param authorId    used to show only posts by that author if provided
     * @param pageable    pagination and sorting information
     * @return a list of posts that is filtered and sorted according to the inputs
     */
    @GetMapping("posts")
    public PageOfPosts getPosts(@RequestParam(required = false) Category category,
                                @RequestParam(required = false) String searchInput,
                                @RequestParam(required = false) String authorId,
                                Pageable pageable) {
        return postService.getPosts(new GetPostRequest(category, searchInput, authorId, pageable));
    }

    @GetMapping("post/{postId}")
    public VerbosePostDetails getPost(@PathVariable UUID postId) {
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
    public MessageResponse deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return new MessageResponse("The post has been deleted successfully!.");
    }

    @GetMapping("post/{id}/comments")
    public List<CommentDetails> getPostComments(@PathVariable UUID id) {
        return commentService.getCommentsByPost(id);
    }
}
