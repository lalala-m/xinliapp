package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.dto.CreatePostRequest;
import com.tongyangyuan.mentalhealth.dto.PostDto;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.Post;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.PostRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, ConsultantRepository consultantRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.consultantRepository = consultantRepository;
    }

    @Transactional(readOnly = true)
    public Page<PostDto> listPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::convertToDto);
    }

    @Transactional
    public PostDto createPost(CreatePostRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + authorId));

        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());

        if (request.getGuidedByConsultantId() != null) {
            Consultant consultant = consultantRepository.findByUserId(request.getGuidedByConsultantId())
                    .orElseThrow(() -> new RuntimeException("Consultant not found with user id: " + request.getGuidedByConsultantId()));
            post.setGuidedBy(consultant);
        }

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImageUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(post.getLikeCount());

        User author = post.getAuthor();
        dto.setAuthorId(author.getId());
        dto.setAuthorName(author.getNickname());
        // dto.setAuthorAvatarUrl(author.getAvatarUrl()); // Assuming User entity will have an avatar field

        if (post.getGuidedBy() != null) {
            Consultant consultant = post.getGuidedBy();
            dto.setConsultantId(consultant.getUserId());
            dto.setConsultantName(consultant.getName());
        }

        return dto;
    }
}

