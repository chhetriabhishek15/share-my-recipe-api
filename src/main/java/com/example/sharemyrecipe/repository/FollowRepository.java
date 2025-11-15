package com.example.sharemyrecipe.repository;
import com.example.sharemyrecipe.entity.Follow;
import com.example.sharemyrecipe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Optional<Follow> findByFollowerAndFollowee(User follower, User followee);
    List<Follow> findAllByFollower(User follower);
    long countByFollowee(User followee);
    boolean existsByFollowerAndFollowee(User follower, User followee);
}