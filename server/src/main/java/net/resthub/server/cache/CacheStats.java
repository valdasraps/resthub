package net.resthub.server.cache;

import lombok.Getter;
import lombok.Setter;

/**
 * CacheStats
 * @author valdo
 */
@Getter
@Setter
public class CacheStats {

    private long lastUpdate;
    private long expTime;
    private long hitCount;
    private boolean expired;
    
}
