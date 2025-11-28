package com.amazin.svelteamazin.config;

import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final UserRepository repo;

    public DataLoader(UserRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void init() {
        if (repo.count() == 0) {
            repo.save(new User(null, "owner", "letmein", "OWNER"));
            repo.save(new User(null, "john", "password123", "USER"));
            repo.save(new User(null, "sarah", "mypassword", "USER"));
        }
    }
}
