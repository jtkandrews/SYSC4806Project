package com.amazin.svelteamazin.repository;

import com.amazin.svelteamazin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
