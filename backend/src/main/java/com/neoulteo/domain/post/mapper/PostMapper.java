package com.neoulteo.domain.post.mapper;

import com.neoulteo.domain.post.dto.PostDto;
import com.neoulteo.domain.post.dto.PostCommentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    void insertPost(PostDto postDto);
    PostDto selectPostById(Long id);
    List<PostDto> selectPostsByCategory(@Param("category") String category, @Param("keyword") String keyword,
            @Param("sort") String sort, @Param("offset") int offset, @Param("limit") int limit);
    int selectPostCountByCategory(@Param("category") String category, @Param("keyword") String keyword);
    void updatePost(PostDto postDto);
    void deletePost(Long id);
    void incrementViews(Long id);
    List<PostCommentDto> selectCommentsByPostId(@Param("postId") Long postId);
    void insertComment(PostCommentDto commentDto);
    void deleteCommentByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    int selectLikeExists(@Param("postId") Long postId, @Param("userId") Long userId);
    void insertLike(@Param("postId") Long postId, @Param("userId") Long userId);
    void deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);
}
