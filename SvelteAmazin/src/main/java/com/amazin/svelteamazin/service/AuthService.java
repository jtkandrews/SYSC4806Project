package com.amazin.svelteamazin.service;

import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService users;

    @Autowired
    public AuthService(UserService users) {
        this.users = users;
    }

    // ---------------------------
    // USER AUTHENTICATION
    // ---------------------------
    public User authenticateUser(String username, String password) {
        User u = users.findByUsername(username);
        if (u == null) return null;
        if (!u.getPassword().equals(password)) return null;
        return u;
    }

    // ---------------------------
    // OWNER AUTH
    // (optional: stored in DB)
    // ---------------------------
    public boolean authenticateOwner(String password) {
        // Check database for any user with role OWNER
        User owner = users.findByUsername("owner"); // you can change this
        if (owner != null && owner.getPassword().equals(password)) {
            return true;
        }
        return false;
    }
}
