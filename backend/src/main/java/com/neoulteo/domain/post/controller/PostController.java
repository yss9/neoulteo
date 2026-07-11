package com.neoulteo.domain.post.controller;

import com.neoulteo.domain.post.dto.PostDto;
import com.neoulteo.domain.post.dto.PostCommentDto;
import com.neoulteo.domain.post.service.PostService;
import com.neoulteo.domain.travelplan.dao.DbTravelPlanDao;
import com.neoulteo.global.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final DbTravelPlanDao travelPlanDao;
    private final Set<String> adminEmails;

    public PostController(PostService postService, DbTravelPlanDao travelPlanDao,
            @Value("${community.admin-emails:}") String adminEmails) {
        this.postService = postService;
        this.travelPlanDao = travelPlanDao;
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(this::hasText)
                .collect(Collectors.toSet());
    }

    @PostMapping
    public ResponseEntity<String> write(@RequestBody PostDto postDto, @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login is required.");
        }

        postDto.setUserId(principal.getUser().getId());
        if (isNotice(postDto) && !isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can write notices.");
        }
        if (isPlanShare(postDto)) {
            if (!hasText(postDto.getTravelPlanId())) {
                return ResponseEntity.badRequest().body("Travel plan selection is required.");
            }
            boolean shared = travelPlanDao.updateShared(postDto.getTravelPlanId(), principal.getUsername(), true);
            if (!shared) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only your travel plan can be shared.");
            }
        } else {
            postDto.setTravelPlanId(null);
        }

        postService.writePost(postDto);
        return ResponseEntity.ok("Post created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> get(@PathVariable Long id) {
        PostDto post = postService.getPost(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getPostsByCategory(category, keyword, sort, page, limit));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Map<String, Object>> comments(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "items", postService.getComments(id)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<String> writeComment(@PathVariable Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login is required.");
        }
        if (request == null || !hasText(request.content())) {
            return ResponseEntity.badRequest().body("Comment content is required.");
        }

        PostCommentDto comment = new PostCommentDto();
        comment.setPostId(id);
        comment.setUserId(principal.getUser().getId());
        comment.setContent(request.content().trim());
        postService.writeComment(comment);
        return ResponseEntity.ok("Comment created successfully");
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> removeComment(@PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login is required.");
        }

        postService.removeComment(commentId, principal.getUser().getId());
        return ResponseEntity.ok("Comment deleted successfully");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Login is required."));
        }

        boolean liked = postService.toggleLike(id, principal.getUser().getId());
        return ResponseEntity.ok(Map.of("success", true, "liked", liked));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> modify(@PathVariable Long id, @RequestBody PostDto postDto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login is required.");
        }

        PostDto existing = postService.findPost(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canManage(existing, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own posts.");
        }
        if ((isNotice(existing) || isNotice(postDto)) && !isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can manage notices.");
        }

        postDto.setId(id);
        if (isPlanShare(postDto)) {
            if (!hasText(postDto.getTravelPlanId())) {
                return ResponseEntity.badRequest().body("Travel plan selection is required.");
            }
            boolean shared = travelPlanDao.updateShared(postDto.getTravelPlanId(), principal.getUsername(), true);
            if (!shared) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only your travel plan can be shared.");
            }
        } else {
            postDto.setTravelPlanId(null);
        }
        postService.modifyPost(postDto);
        return ResponseEntity.ok("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> remove(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login is required.");
        }

        PostDto existing = postService.findPost(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canManage(existing, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own posts.");
        }
        if (isNotice(existing) && !isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can manage notices.");
        }

        postService.removePost(id);
        return ResponseEntity.ok("Post deleted successfully");
    }

    private boolean isPlanShare(PostDto postDto) {
        return postDto != null && "PLAN_SHARE".equals(postDto.getCategory());
    }

    private boolean isNotice(PostDto postDto) {
        return postDto != null && "NOTICE".equals(postDto.getCategory());
    }

    private boolean canManage(PostDto postDto, CustomUserDetails principal) {
        return isAdmin(principal)
                || (postDto.getUserId() != null
                && principal.getUser() != null
                && postDto.getUserId().equals(principal.getUser().getId()));
    }

    private boolean isAdmin(CustomUserDetails principal) {
        return principal != null && adminEmails.contains(principal.getUsername());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public record CommentRequest(String content) {
    }
}
