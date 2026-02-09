package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.CreatePostRequest;
import com.example.nsu_backend.dto.DeletePostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.mappers.PostMapper;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    public PostDetails createPost(CreatePostRequest request) {
        Post post = postMapper.postDtoToNewPost(request, userRepository.getReferenceById(request.authorId()));
        Post newPost = postRepository.save(post);
        return postMapper.postToPostDto(newPost);
    }

    public void deletePost(DeletePostRequest request) {
        postRepository.deleteById(request.postId());
    }

    public List<PostDetails> getPosts() {
        return postRepository.findAll().stream().map(postMapper::postToPostDto).toList();
    }
}
