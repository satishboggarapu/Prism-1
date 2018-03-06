package com.mikechoch.prism.attribute;

import com.mikechoch.prism.type.NotificationType;

/**
 * Created by parth on 3/4/18.
 */

public class Notification {

    private NotificationType type;
    private PrismPost prismPost;
    private long actionTimestamp;
    private PrismUser mostRecentUser;
    private int otherUserCount;
    private boolean viewed;


    public Notification() {

    }

    public Notification(NotificationType type, PrismPost prismPost, PrismUser mostRecentUser, long actionTimestamp, boolean viewed) {
        this.type = type;
        this.prismPost = prismPost;
        this.mostRecentUser = mostRecentUser;
        this.actionTimestamp = actionTimestamp;
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

    public long getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(long actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }


    public PrismUser getMostRecentUser() {
        return mostRecentUser;
    }

    public void setMostRecentUser(PrismUser mostRecentUser) {
        this.mostRecentUser = mostRecentUser;
    }

    public Integer getOtherUserCount() {
        switch (type) {
            case LIKE:
                return prismPost.getLikes() > 1 ? prismPost.getLikes() - 1 : 0;
            case REPOST:
                return  prismPost.getReposts() > 1 ? prismPost.getReposts() -1 : 0;
            default:
                return 0;
        }
    }
}
