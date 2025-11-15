package com.example.sharemyrecipe.service;

import com.example.sharemyrecipe.entity.User;

import java.util.List;
import java.util.UUID;

public interface FollowService {
    void follow(UUID followerId, UUID followeeId);
    void unfollow(UUID followerId, UUID followeeId);
    List<User> listFollowees(UUID followerId);
    boolean isFollowing(UUID followerId, UUID followeeId);
}
