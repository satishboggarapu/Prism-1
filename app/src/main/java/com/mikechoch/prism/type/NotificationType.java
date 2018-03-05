package com.mikechoch.prism.type;

/**
 * Created by parth on 3/4/18.
 */

public enum NotificationType {

    LIKE("like"),
    REPOST("repost"),
    FOLLOW("follow");

    private final String notifId;

    NotificationType(String notifId) {
        this.notifId = notifId;
    }

    public String getNotifId() {
        return "_" + notifId;
    }
}
