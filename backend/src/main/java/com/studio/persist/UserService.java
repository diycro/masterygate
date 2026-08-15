package com.studio.persist;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    /** Find the learner by name, or create one. (No auth in this MVP — name is the identity.) */
    @Transactional
    public UserEntity findOrCreate(String name) {
        return repo.findByName(name).orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setName(name);
            u.setCreatedAt(Instant.now());
            try {
                return repo.save(u);
            } catch (RuntimeException raceLost) {
                // Another request created the same name concurrently — re-read it.
                return repo.findByName(name).orElseThrow(() -> raceLost);
            }
        });
    }
}
