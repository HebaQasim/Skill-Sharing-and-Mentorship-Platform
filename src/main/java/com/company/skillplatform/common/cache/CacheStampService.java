package com.company.skillplatform.common.cache;

public interface CacheStampService {
    long getStamp(String name);
    void bump(String name);
}
