package io.chn.redisstudy.common;

import org.springframework.stereotype.Component;


@Component
public class UserContext {
    private static final ThreadLocal<String> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentUser(String userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    public static Long getCurrentUser() {
        String userId = USER_ID_THREAD_LOCAL.get();
       if (userId == null) {
           return null;
       }
       return Long.valueOf(userId);
    }

    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }

    private UserContext() {

    }
}
