package com.example.nsu_backend.dto;

import java.util.List;

public record PageOfPosts(int curPage, int pageCount, List<VerbosePostDetails> posts) {
}
