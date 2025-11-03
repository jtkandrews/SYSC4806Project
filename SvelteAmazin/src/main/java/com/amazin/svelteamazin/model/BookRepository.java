package com.amazin.svelteamazin.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// JpaRepository<EntityType, IdType>
public interface BookRepository extends JpaRepository<Book, String> {

    // Search by title OR author (case-insensitive)
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}
