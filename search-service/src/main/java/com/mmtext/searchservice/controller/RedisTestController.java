package com.mmtext.searchservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/redis")
    public Map<String, String> testRedis() {
        try {
            // Write
            redisTemplate.opsForValue().set("test:key", "Hello Redis!", 60, TimeUnit.SECONDS);

            // Read
            String value = redisTemplate.opsForValue().get("test:key");

            return Map.of(
                    "status", "SUCCESS",
                    "value", value != null ? value : "null",
                    "message", "Redis is working!"
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "FAILED",
                    "error", e.getMessage()
            );
        }
    }
}