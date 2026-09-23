package io.chn.redisstudy.utils;

import org.springframework.stereotype.Component;


public interface IRedisCreateLock {
    boolean setLock(long timeout);
    void deleteLock();
}
