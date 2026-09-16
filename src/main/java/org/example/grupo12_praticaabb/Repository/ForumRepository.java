package org.example.grupo12_praticaabb.Repository;

import org.example.grupo12_praticaabb.Entity.ForumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Henry Kazumi Tanaka - RA: 248060
 */

@Repository
public interface ForumRepository extends JpaRepository<ForumEntity, Long> {
    // Só de estender JpaRepository, você já ganha métodos como save(), findById(), etc.
}