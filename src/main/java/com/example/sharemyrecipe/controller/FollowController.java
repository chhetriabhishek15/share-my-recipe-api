package com.example.sharemyrecipe.controller;
import com.example.sharemyrecipe.service.FollowService;
import com.example.sharemyrecipe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chefs")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    // POST /api/chefs/{chefId}/follow
    @PostMapping("/{chefId}/follow")
    public ResponseEntity<?> follow(@PathVariable UUID chefId, Principal principal) {
        var current = userService.findByEmail(principal.getName()).orElseThrow();
        followService.follow(current.getId(), chefId);
        return ResponseEntity.ok().body("followed");
    }

    @DeleteMapping("/{chefId}/follow")
    public ResponseEntity<?> unfollow(@PathVariable UUID chefId, Principal principal) {
        var current = userService.findByEmail(principal.getName()).orElseThrow();
        followService.unfollow(current.getId(), chefId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chefId}/followers/count")
    public ResponseEntity<?> followerCount(@PathVariable UUID chefId) {
        var count = followService.isFollowing(chefId, chefId) ? 0L : 0L; // placeholder, implement if needed
        return ResponseEntity.ok().body(count);
    }
}