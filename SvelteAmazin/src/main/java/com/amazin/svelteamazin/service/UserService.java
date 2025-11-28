package com.amazin.svelteamazin.service;

import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public User createUser(String username, String password, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);  // for demo, no hashing
        u.setRole(role);
        return repo.save(u);
    }
}
