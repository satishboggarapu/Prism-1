package com.mikechoch.prism.type;

import com.mikechoch.prism.constants.Key;

/**
 * Created by parth on 3/4/18.
 */

public enum NotificationType {

    LIKE("like", Key.DB_REF_POST_LIKED_USERS),
    UNLIKE("like", Key.DB_REF_POST_LIKED_USERS),

    REPOST("repost", Key.DB_REF_POST_REPOSTED_USERS),
    UNREPOST("repost", Key.DB_REF_POST_REPOSTED_USERS),

    FOLLOW("follow", Key.DB_REF_USER_FOLLOWERS),
    UNFOLLOW("follow", Key.DB_REF_USER_FOLLOWERS);



    private final String notifIdSuffix;
    private final String DB_REF_KEY;

    NotificationType(String notifIdSuffix, String dbRefKey) {
        this.notifIdSuffix = notifIdSuffix;
        this.DB_REF_KEY = dbRefKey;
    }

    public String getNotifIdSuffix() {
        return "_" + notifIdSuffix;
    }

    public String getDatabaseRefKey() {
        return DB_REF_KEY;
    }

    public static NotificationType getNotificationType(String notificationId) {
        if (notificationId.endsWith("_like")) {
            return NotificationType.LIKE;
        }
        if (notificationId.endsWith("_repost")) {
            return NotificationType.REPOST;
        }
        if (notificationId.endsWith("_follow")) {
            return NotificationType.FOLLOW;
        }
        return null;
    }

    public static String getNotificationPostId(NotificationType type, String notificationId) {
        return notificationId.replace(type.getNotifIdSuffix(), "");
    }
}
