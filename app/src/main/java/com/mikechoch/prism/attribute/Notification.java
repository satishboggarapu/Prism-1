package com.mikechoch.prism.attribute;

import com.mikechoch.prism.type.NotificationType;

/**
 * Created by parth on 3/4/18.
 */

public class Notification {

    private NotificationType type;
    private PrismPost prismPost;
    private long timestamp;
    private boolean viewed;


    public Notification() {

    }

    public Notification(NotificationType type, PrismPost prismPost, long timestamp, boolean viewed) {
        this.type = type;
        this.prismPost = prismPost;
        this.timestamp = timestamp;
        this.viewed = viewed;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public PrismPost getPrismPost() {
        return prismPost;
    }

    public void setPrismPost(PrismPost prismPost) {
        this.prismPost = prismPost;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
}
