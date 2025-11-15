package com.amazin.svelteamazin.controller;

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
@CrossOrigin(origins = "*") // your SPA is same-origin in prod; dev proxy makes this safe in dev
public class AuthController {

    @Value("${app.owner-password}")
    private String configuredOwnerPassword;

    // POST /api/auth/owner-login { "password": "..." }
    @PostMapping("/owner-login")
    public Map<String, String> ownerLogin(@RequestBody Map<String, String> body, HttpServletResponse res) {
        String pw = body.getOrDefault("password", "");
        if (!pw.equals(configuredOwnerPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad password");
        }
        Cookie c = new Cookie("role", "OWNER");
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge((int) Duration.ofHours(12).getSeconds());
        // In Azure add HTTPS binding and uncomment next line:
        // c.setSecure(true);
        res.addCookie(c);
        return Map.of("role", "OWNER");
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse res) {
        Cookie c = new Cookie("role", "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        res.addCookie(c);
        return Map.of("status", "ok");
    }

    // GET /api/auth/me → { role: "OWNER" | "USER" }
    @GetMapping("/me")
    public Map<String, String> me(HttpServletRequest req) {
        String role = "USER";
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("role".equals(c.getName()) && "OWNER".equals(c.getValue())) {
                    role = "OWNER";
                    break;
                }
            }
        }
        return Map.of("role", role);
    }
}
