package com.mikechoch.prism;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;


/**
 * Created by mikechoch on 2/26/18.
 */

public class ToolbarPullDownLayout extends RelativeLayout {

    /*
     * Globals
     */
    private Context context;

    private float viewTouchDownX;
    private float viewTouchDownY;
    private float viewMovingX;
    private float viewMovingY;
    private float viewAlpha;
    private float viewScale;
    private VelocityTracker velocityTracker = null;

    private ViewGroup parentView;
    private ViewGroup[] parentsScrollViews;

    private boolean disabled = false;


    /*
     * Constructors
     */
    public ToolbarPullDownLayout(Context context) {
        super(context);
    }

    public ToolbarPullDownLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolbarPullDownLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ToolbarPullDownLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * Give context and the parent view
     */
    public void addParentView(Context context, ViewGroup view) {
        this.context = context;
        this.parentView = view;
    }

    /**
     * Add all parent view children scroll views for later usage
     */
    public void addScrollViews(ViewGroup[] scrollViews) {
        this.parentsScrollViews = scrollViews;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (!disabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Store the X and Y coordinate on the screen where ACTION_DOWN occurred
                    viewTouchDownX = event.getRawX();
                    viewTouchDownY = event.getRawY();
                    viewScale = 1f;
                    viewAlpha = 1f;
                    parentView.setPivotX(viewTouchDownX);
                    parentView.setPivotY(viewTouchDownY);
                    if (velocityTracker == null) {
                        // Retrieve a new VelocityTracker object to watch the
                        // velocity of a motion
                        velocityTracker = VelocityTracker.obtain();
                    } else {
                        // Reset the velocity tracker back to its initial state.
                        velocityTracker.clear();
                    }

                    // Add the ACTION_DOWN event as a movement to the VelocityTracker
                    velocityTracker.addMovement(event);

                    // Since ACTION_DOWN, disable scroll views from intercepting touch events
                    toggleScrollViewIntercepts(true);
                    toggleClickEventsOfViewGroup(parentView, false);
                    break;
                case MotionEvent.ACTION_UP:
                    // Check that the screen drag Y percentage threshold and velocity threshold booleans
                    boolean isOverDragThreshold = (1 - Math.abs(viewMovingY * 1.0f / parentView.getHeight() * 1.0f)) < 0.60f;
                    boolean isOverVelocityThreshold = velocityTracker.getYVelocity() > 1250;
                    // If one of the thresholds is met, super.onBackPressed to activate ShareTransition
                    // and go back to the UserProfileActivity
                    // Otherwise, animate the view back to its original location, scale, and alpha
                    if (isOverDragThreshold || isOverVelocityThreshold) {
                        ((Activity) context).onBackPressed();
                    } else {
                        parentView.animate()
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .translationX(0)
                                .translationY(0)
                                .setDuration(250)
                                .start();
                    }

                    // Since ACTION_UP, enable scroll views from intercepting touch events
                    toggleScrollViewIntercepts(false);
                    toggleClickEventsOfViewGroup(parentView, true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Update the X and Y coordinate on the screen where ACTION_MOVE occurred
                    // Update the location, scale, and alpha based off the X and Y of the event
                    viewMovingX = event.getRawX();
                    viewMovingY = event.getRawY();
                    viewScale = 1 - Math.abs((viewTouchDownY - viewMovingY) / (viewTouchDownY * 10.0f));
                    viewAlpha = viewScale;
                    parentView.setTranslationX(-viewTouchDownX + viewMovingX);
                    parentView.setTranslationY(-viewTouchDownY + viewMovingY);
                    // A min threshold of 50% scaleX and scaleY is set
                    if (viewScale > 0.5f) {
                        parentView.setScaleX(viewScale);
                        parentView.setScaleY(viewScale);
                    }
                    // A min threshold of 50% alpha is set
                    if (viewAlpha > 0.5f) {
                        parentView.setAlpha(viewAlpha);
                    }

                    // Add the ACTION_MOVE event as a movement to the VelocityTracker
                    velocityTracker.addMovement(event);
                    // When you want to determine the velocity, call computeCurrentVelocity().
                    // Then call getXVelocity() and getYVelocity() to retrieve the
                    // velocity for each pointer ID.
                    velocityTracker.computeCurrentVelocity(1000);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // ONLY ACTION_UP WILL STOP TOUCH EVENTS
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    // ONLY ACTION_UP WILL STOP TOUCH EVENTS
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick();

        // Handle the action for the custom click here
        return true;
    }

    /**
     * A boolean parameter is taken in to disable touch intercepting from all child scroll views
     */
    public void toggleScrollViewIntercepts(boolean disableIntercept) {
        for (ViewGroup scrollView : parentsScrollViews) {
            scrollView.requestDisallowInterceptTouchEvent(disableIntercept);
        }
    }

    /**
     * Enables/Disables all child views of a view group
     */
    private void toggleClickEventsOfViewGroup(ViewGroup viewGroup, boolean enableClick) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enableClick);
            if (view instanceof ViewGroup) {
                toggleClickEventsOfViewGroup((ViewGroup) view, enableClick);
            }
        }
    }

    /**
     * Disables the touch events for the pull down toolbar
     */
    public void disable(boolean shouldDisable) {
        disabled = shouldDisable;
    }
}
