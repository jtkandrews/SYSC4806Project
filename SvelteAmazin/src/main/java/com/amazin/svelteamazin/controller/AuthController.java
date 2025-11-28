package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.service.AuthService;
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

        Cookie c = new Cookie("role", "USER");
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge((int) Duration.ofHours(12).getSeconds());
        res.addCookie(c);

        return Map.of("role", u.getRole());
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

        Cookie c = new Cookie("role", "OWNER");
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge((int) Duration.ofHours(12).getSeconds());
        res.addCookie(c);

        return Map.of("role", "OWNER");
    }

    // --------------------------------------------
    // LOGOUT
    // POST /api/auth/logout
    // --------------------------------------------
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse res) {
        Cookie c = new Cookie("role", "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        res.addCookie(c);
        return Map.of("status", "ok");
    }

    // --------------------------------------------
    // SESSION CHECK
    // GET /api/auth/me
    // --------------------------------------------
    @GetMapping("/me")
    public Map<String, String> me(HttpServletRequest req) {
        String role = "USER";

        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("role".equals(c.getName())) {
                    if ("OWNER".equals(c.getValue())) role = "OWNER";
                    break;
                }
            }
        }
        return Map.of("role", role);
    }
}
