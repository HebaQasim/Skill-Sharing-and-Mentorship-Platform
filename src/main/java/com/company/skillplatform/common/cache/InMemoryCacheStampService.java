package com.company.skillplatform.common.cache;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryCacheStampService implements CacheStampService {

    private final ConcurrentHashMap<String, AtomicLong> stamps = new ConcurrentHashMap<>();

    @Override
    public long getStamp(String name) {
        return stamps.computeIfAbsent(name, k -> new AtomicLong(1)).get();
    }

    @Override
    public void bump(String name) {
        stamps.computeIfAbsent(name, k -> new AtomicLong(1)).incrementAndGet();
    }
}
