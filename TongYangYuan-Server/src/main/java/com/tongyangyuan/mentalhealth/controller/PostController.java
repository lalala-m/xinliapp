package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.CreatePostRequest;
import com.tongyangyuan.mentalhealth.dto.PostDto;
import com.tongyangyuan.mentalhealth.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> listPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.listPosts(pageable));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest request, @RequestParam(required = false, defaultValue = "4") Long authorId) {
        // For now, we'll use a request parameter for authorId. 
        // In a production app, this would come from the authentication token.
        PostDto createdPost = postService.createPost(request, authorId);
        return ResponseEntity.created(URI.create("/api/posts/" + createdPost.getId())).body(createdPost);
    }
}
