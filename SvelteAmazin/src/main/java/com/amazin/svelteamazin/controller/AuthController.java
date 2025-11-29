package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.service.AuthService;
import com.amazin.svelteamazin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService auth;

    @Autowired
    private UserService users;

    @Value("${app.owner-password}")
    private String configuredOwnerPassword;

    // --------------------------------------------
    // USER LOGIN
    // POST /api/auth/user-login { username, password }
    // --------------------------------------------
    @PostMapping("/user-login")
    public Map<String, String> userLogin(@RequestBody Map<String, String> body, HttpServletResponse res) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");

        User u = auth.authenticateUser(username, password);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Persist minimal session in cookies
        Cookie roleCookie = new Cookie("role", u.getRole());
        roleCookie.setHttpOnly(true);
        roleCookie.setPath("/");
        roleCookie.setMaxAge((int) Duration.ofHours(12).getSeconds());
        res.addCookie(roleCookie);

        Cookie userCookie = new Cookie("username", u.getUsername());
        userCookie.setHttpOnly(true);
        userCookie.setPath("/");
        userCookie.setMaxAge((int) Duration.ofHours(12).getSeconds());
        res.addCookie(userCookie);

        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "role", u.getRole()
        );
    }

    // --------------------------------------------
    // OWNER LOGIN
    // POST /api/auth/owner-login { password }
    // --------------------------------------------
    @PostMapping("/owner-login")
    public Map<String, String> ownerLogin(@RequestBody Map<String, String> body, HttpServletResponse res) {
        String pw = body.getOrDefault("password", "");

        // either check database via AuthService OR use config
        boolean valid = auth.authenticateOwner(pw);
        if (!valid && !pw.equals(configuredOwnerPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad password");
        }

        Cookie roleCookie = new Cookie("role", "OWNER");
        roleCookie.setHttpOnly(true);
        roleCookie.setPath("/");
        roleCookie.setMaxAge((int) Duration.ofHours(12).getSeconds());
        res.addCookie(roleCookie);

        Cookie userCookie = new Cookie("username", "owner");
        userCookie.setHttpOnly(true);
        userCookie.setPath("/");
        userCookie.setMaxAge((int) Duration.ofHours(12).getSeconds());
        res.addCookie(userCookie);

        return Map.of("role", "OWNER");
    }

    // --------------------------------------------
    // LOGOUT
    // POST /api/auth/logout
    // --------------------------------------------
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse res) {
        Cookie roleCookie = new Cookie("role", "");
        roleCookie.setPath("/");
        roleCookie.setMaxAge(0);
        roleCookie.setHttpOnly(true);
        res.addCookie(roleCookie);

        Cookie userCookie = new Cookie("username", "");
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        userCookie.setHttpOnly(true);
        res.addCookie(userCookie);
        return Map.of("status", "ok");
    }

    // --------------------------------------------
    // SESSION CHECK
    // GET /api/auth/me
    // --------------------------------------------
    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest req) {
        String username = null;
        String role = null;

        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("role".equals(c.getName())) {
                    role = c.getValue();
                } else if ("username".equals(c.getName())) {
                    username = c.getValue();
                }
            }
        }

        if (role == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        // Try to load the user if present in DB
        if (username != null) {
            User u = users.findByUsername(username);
            if (u != null) {
                return Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "role", u.getRole()
                );
            }
        }

        // Fallback for owner login using configured password
        if ("OWNER".equals(role)) {
            return Map.of(
                    "username", username == null ? "owner" : username,
                    "role", "OWNER"
            );
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
    }
}
