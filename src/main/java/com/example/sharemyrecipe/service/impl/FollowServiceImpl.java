package com.example.sharemyrecipe.service.impl;
import com.example.sharemyrecipe.entity.Follow;
import com.example.sharemyrecipe.entity.User;
import com.example.sharemyrecipe.exception.ConflictException;
import com.example.sharemyrecipe.exception.NotFoundException;
import com.example.sharemyrecipe.repository.FollowRepository;
import com.example.sharemyrecipe.repository.UserRepository;
import com.example.sharemyrecipe.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public void follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) throw new ConflictException("cannot follow yourself");
        User follower = userRepository.findById(followerId).orElseThrow(() -> new NotFoundException("follower not found"));
        User followee = userRepository.findById(followeeId).orElseThrow(() -> new NotFoundException("followee not found"));
        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new ConflictException("already following");
        }
        Follow f = Follow.builder().follower(follower).followee(followee).build();
        followRepository.save(f);
        log.info("User {} followed {}", followerId, followeeId);
    }

    @Override
    @Transactional
    public void unfollow(UUID followerId, UUID followeeId) {
        User follower = userRepository.findById(followerId).orElseThrow(() -> new NotFoundException("follower not found"));
        User followee = userRepository.findById(followeeId).orElseThrow(() -> new NotFoundException("followee not found"));
        followRepository.findByFollowerAndFollowee(follower, followee).ifPresentOrElse(followRepository::delete, () -> {
            throw new NotFoundException("not following");
        });
        log.info("User {} unfollowed {}", followerId, followeeId);
    }

    @Override
    public List<User> listFollowees(UUID followerId) {
        User follower = userRepository.findById(followerId).orElseThrow(() -> new NotFoundException("follower not found"));
        var follows = followRepository.findAllByFollower(follower);
        return follows.stream().map(Follow::getFollowee).collect(Collectors.toList());
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followeeId) {
        User follower = userRepository.findById(followerId).orElseThrow(() -> new NotFoundException("follower not found"));
        User followee = userRepository.findById(followeeId).orElseThrow(() -> new NotFoundException("followee not found"));
        return followRepository.existsByFollowerAndFollowee(follower, followee);
    }
}