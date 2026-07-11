package com.neoulteo.domain.post.service;

import com.neoulteo.domain.post.dto.PostDto;
import com.neoulteo.domain.post.dto.PostCommentDto;
import java.util.List;
import java.util.Map;

public interface PostService {
    void writePost(PostDto postDto);
    PostDto getPost(Long id);
    PostDto findPost(Long id);
    Map<String, Object> getPostsByCategory(String category, String keyword, String sort, int page, int limit);
    void modifyPost(PostDto postDto);
    void removePost(Long id);
    List<PostCommentDto> getComments(Long postId);
    void writeComment(PostCommentDto commentDto);
    void removeComment(Long id, Long userId);
    boolean toggleLike(Long postId, Long userId);
}
