package com.neoulteo.domain.user.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.neoulteo.domain.user.dao.DbUserDao;
import com.neoulteo.domain.user.dto.UserDto;
import com.neoulteo.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final DbUserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserController(DbUserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Map<String, Object> apiInfo() {
        return message(true, "Neoulteo users API");
    }

    @GetMapping("/me")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> profile(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        return ResponseEntity.ok(userResponse(principal.getUser(), null));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        UserDto newUser = new UserDto(
                request.name(),
                request.email(),
                request.password());
        if (userDao.registerUser(newUser)) {
            return ResponseEntity.ok(message(true, "Signup completed."));
        }
        return error(HttpStatus.BAD_REQUEST, "Email already exists or server error occurred.");
    }

    @PatchMapping("/me")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> update(@RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        String name = request.name();
        String password = hasText(request.password()) ? passwordEncoder.encode(request.password()) : null;
        if (!hasText(name) && !hasText(password)) {
            return error(HttpStatus.BAD_REQUEST, "No profile data to update.");
        }

        if (!userDao.updateUser(principal.getUsername(), name, password)) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update profile.");
        }

        UserDto updatedUser = userDao.findByEmail(principal.getUsername());
        return ResponseEntity.ok(userResponse(updatedUser, "Profile updated."));
    }

    @DeleteMapping("/me")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody DeleteAccountRequest deleteRequest,
            @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest request) {
        if (principal == null) {
            return error(HttpStatus.UNAUTHORIZED, "Login is required.");
        }

        UserDto user = userDao.findByEmail(principal.getUsername());
        if (user == null || !passwordEncoder.matches(deleteRequest.password(), user.getPassword())) {
            return error(HttpStatus.BAD_REQUEST, "Password does not match.");
        }

        if (!userDao.deleteUser(principal.getUsername())) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete account.");
        }

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ResponseEntity.ok(message(true, "Account deleted."));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String email = request.email();
        if (userDao.findByEmail(email) == null) {
            return error(HttpStatus.NOT_FOUND, "Email does not exist.");
        }

        if (userDao.updateUser(email, null, passwordEncoder.encode(request.password()))) {
            return ResponseEntity.ok(message(true, "Password reset completed."));
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reset password.");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(message(false, message));
    }

    private Map<String, Object> message(boolean success, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        return body;
    }

    private Map<String, Object> userResponse(UserDto user, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        if (message != null) {
            body.put("message", message);
        }
        body.put("id", user.getId());
        body.put("name", user.getName());
        body.put("email", user.getEmail());
        return body;
    }

    public record SignupRequest(String name, String email, String password) {
    }

    public record UpdateProfileRequest(String name, String password) {
    }

    public record DeleteAccountRequest(String password) {
    }

    public record ResetPasswordRequest(String email, String password) {
    }
}
