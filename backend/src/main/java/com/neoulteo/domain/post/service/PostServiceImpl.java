package com.neoulteo.domain.post.service;

import com.neoulteo.domain.post.dto.PostDto;
import com.neoulteo.domain.post.dto.PostCommentDto;
import com.neoulteo.domain.post.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    @Transactional
    public void writePost(PostDto postDto) {
        postMapper.insertPost(postDto);
    }

    @Override
    @Transactional
    public PostDto getPost(Long id) {
        postMapper.incrementViews(id);
        return postMapper.selectPostById(id);
    }

    @Override
    public PostDto findPost(Long id) {
        return postMapper.selectPostById(id);
    }

    @Override
    public Map<String, Object> getPostsByCategory(String category, String keyword, String sort, int page, int limit) {
        int offset = (page - 1) * limit;
        List<PostDto> posts = postMapper.selectPostsByCategory(category, keyword, sort, offset, limit);
        int totalCount = postMapper.selectPostCountByCategory(category, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("totalCount", totalCount);
        result.put("currentPage", page);
        result.put("totalPage", (int) Math.ceil((double) totalCount / limit));
        
        return result;
    }

    @Override
    @Transactional
    public void modifyPost(PostDto postDto) {
        postMapper.updatePost(postDto);
    }

    @Override
    @Transactional
    public void removePost(Long id) {
        postMapper.deletePost(id);
    }

    @Override
    public List<PostCommentDto> getComments(Long postId) {
        return postMapper.selectCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void writeComment(PostCommentDto commentDto) {
        postMapper.insertComment(commentDto);
    }

    @Override
    @Transactional
    public void removeComment(Long id, Long userId) {
        postMapper.deleteCommentByIdAndUserId(id, userId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        if (postMapper.selectLikeExists(postId, userId) > 0) {
            postMapper.deleteLike(postId, userId);
            return false;
        }
        postMapper.insertLike(postId, userId);
        return true;
    }
}
