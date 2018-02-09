package com.mikechoch.prism.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikechoch.prism.AnimationBounceInterpolator;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.PrismPost;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.LikeRepostActivity;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.helper.MyTimeUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by mikechoch on 1/21/18.
 */

public class PrismPostRecyclerViewAdapter extends RecyclerView.Adapter<PrismPostRecyclerViewAdapter.ViewHolder> {

    /*
     * Global variables
     */
    private final int PROGRESS_BAR_VIEW_TYPE = 0;
    private final int PRISM_POST_VIEW_TYPE = 1;

    private Context context;
    private PrismPost prismPost;
    private ArrayList<String> prismPostKeys;
    private HashMap<String, PrismPost> prismPostHashMap;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;
    private String[] morePostOptionsCurrentUser = {"Report post", "Share", "Delete"};
    private String[] morePostOptions = {"Report post", "Share"};


    public PrismPostRecyclerViewAdapter(Context context, ArrayList<String> prismPostKeys, HashMap<String, PrismPost> prismPostHashMap, int[] screenDimens) {
        this.context = context;
        this.prismPostKeys = prismPostKeys;
        this.prismPostHashMap = prismPostHashMap;

        this.scale = context.getResources().getDisplayMetrics().density;
        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");
        this.screenWidth = screenDimens[0];
        this.screenHeight = screenDimens[1];
    }

    @Override
    public int getItemViewType(int position) {
        return PRISM_POST_VIEW_TYPE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        switch (viewType) {
            case PRISM_POST_VIEW_TYPE:
                viewHolder =  new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.recycler_view_item_layout, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(prismPostHashMap.get(prismPostKeys.get(position)));
    }

    @Override
    public int getItemCount() {
        return prismPostHashMap.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private FirebaseAuth auth;
        private DatabaseReference userReference;
        private StorageReference storageReference;
        private DatabaseReference allPostsReference;

        private Animation likeHeartBounceAnimation;
        private Animation repostIrisBounceAnimation;
        private Animation unrepostIrisBounceAnimation;
        private Animation likeButtonBounceAnimation;
        private Animation shareButtonBounceAnimation;
        private Animation moreButtonBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        private PrismPost prismPost;
        private ImageView userProfilePicImageView;
        private TextView prismUserTextView;
        private TextView prismPostDateTextView;
        private ImageView prismPostImageView;
        private ImageView likeHeartAnimationImageView;
        private TextView likesCountTextView;
        private ImageView likeButton;
        private ImageView repostIrisAnimationImageView;
        private TextView repostsCountTextView;
        private ImageView repostButton;
        private ImageView moreButton;
        private ProgressBar progressBar;

        private String postId;
        private String postDate;
        private int likeCount;
        private int repostCount;
        private boolean postLiked;
        private boolean postReposted;


        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());
            storageReference = Default.STORAGE_REFERENCE.child(Key.STORAGE_POST_IMAGES_REF);
            allPostsReference = Default.ALL_POSTS_REFERENCE;

            // Animation initializations
            likeHeartBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.like_animation);
            repostIrisBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.repost_animation);
            unrepostIrisBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.unrepost_animation);
            likeButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            shareButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            moreButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);

            // Image initializations
            progressBar = itemView.findViewById(R.id.image_progress_bar);
            userProfilePicImageView = itemView.findViewById(R.id.recycler_view_profile_pic_image_view);
            prismUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            prismPostDateTextView = itemView.findViewById(R.id.recycler_view_date_text_view);
            prismPostImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
            likeHeartAnimationImageView = itemView.findViewById(R.id.recycler_view_like_heart);
            repostIrisAnimationImageView = itemView.findViewById(R.id.recycler_view_repost_iris);

            // Image action button initializations
            likeButton = itemView.findViewById(R.id.image_like_button);
            repostButton = itemView.findViewById(R.id.image_repost_button);
            moreButton = itemView.findViewById(R.id.image_more_button);

            // Image like/repost count initializations
            likesCountTextView = itemView.findViewById(R.id.likes_count_text_view);
            repostsCountTextView = itemView.findViewById(R.id.shares_count_text_view);
        }

        /**
         * Set data for the ViewHolder UI elements
         */
        public void setData(PrismPost prismPostObject) {
            this.prismPost = prismPostObject;
            postId = this.prismPost.getPostId();
            postDate = getFancyDateDifferenceString(prismPost.getTimestamp() * -1);
            likeCount = this.prismPost.getLikes();
            repostCount = this.prismPost.getReposts();
            postLiked = CurrentUser.user_liked_posts.containsKey(postId);
            postReposted = CurrentUser.user_reposted_posts.containsKey(postId);

            populateUIElements();
        }

        /**
         * Populate fields related to the user who posted
         * Handles fields for user profile picture, username, and post date
         */
        private void populatePostUserUIElements() {
            if (prismPost.getPrismUser() != null) {
                Glide.with(context)
                        .asBitmap()
                        .thumbnail(0.05f)
                        .load(prismPost.getPrismUser().getProfilePicture().lowResUri)
                        .apply(new RequestOptions().fitCenter())
                        .into(new BitmapImageViewTarget(userProfilePicImageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                if (!prismPost.getPrismUser().getProfilePicture().isDefault) {
                                    int whiteOutlinePadding = (int) (1 * scale);
                                    userProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                                    userProfilePicImageView.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                                } else {
                                    userProfilePicImageView.setPadding(0, 0, 0, 0);
                                    userProfilePicImageView.setBackground(null);
                                }

                                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                drawable.setCircular(true);
                                userProfilePicImageView.setImageDrawable(drawable);
                            }
                        });
                prismUserTextView.setText(prismPost.getPrismUser().getUsername());
                prismPostDateTextView.setText(postDate);
            }
        }

        /**
         * Setup prismPostImageView
         * Sets the sizing, touch events, and Glide image loading
         */
        private void setupPostImageView() {
            // Show the ProgressBar while waiting for image to load
            progressBar.setVisibility(View.VISIBLE);

            /*
             * prismPostImageView will have a width of 90% of the screen
             * prismPostImageView will have a max height of 60% of the screen
             * This causes any images that are stronger in height to not span the entire screen
             */
            prismPostImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            prismPostImageView.setMaxHeight((int) (screenHeight * 0.6));

            /*
             * Using the Glide library to populate the prismPostImageView
             * asBitmap: converts to Bitmap
             * thumbnail: previews a 5% loaded image while the rest of the image is being loaded
             * load: loads the prismPost URI
             * listener: confirms if the image was uploaded properly or not
             * into: the loaded image will be placed inside the prismPostImageView
             */
            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(prismPost.getImage())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            // TODO: @Mike we should hide progressBar here as well and display a toast or something
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            // TODO: @Mike we should hide progressBar here as well and display a toast or something
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(prismPostImageView);

            /*
             * GestureDetector used to replace the prismPostImageView TouchListener
             * This allows detection of Single, Long, and Double tap gestures
             */
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    System.out.println("Image Single Tapped");
                    // TODO: Intent to new Activity for image

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    System.out.println("Image Long Pressed");
                    // TODO: Think of something to do
                    // TODO: Download image?

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    System.out.println("Image Double Tapped");

                    String postId = prismPost.getPostId();
                    boolean postLiked = !CurrentUser.user_liked_posts.containsKey(postId);
                    Drawable heartButtonDrawable = createLikeDrawable(postLiked);
                    likeButton.setImageDrawable(heartButtonDrawable);
                    Drawable heartDrawable = context.getResources().getDrawable(
                            postLiked ? R.drawable.like_heart : R.drawable.unlike_heart);
                    likeHeartAnimationImageView.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);
                    likeHeartAnimationImageView.startAnimation(likeHeartBounceAnimation);

                    likeCount += postLiked ? 1 : -1;
                    String likeStringTail = likeCount == 1 ? " like" : " likes";
                    likesCountTextView.setText(likeCount + likeStringTail);

                    handleLikeButtonClick(prismPost);
                    return super.onDoubleTap(e);
                }
            });

            /*
             * Sets the TouchListener to be handled by the GestureDetector class
             * This allows detection of Single, Long, and Double tap gestures
             */
            prismPostImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });
        }

        /**
         * Setup animations for actionButtons below prismPostImageView and like/repost ImageView
         */
        private void setupUIAnimations() {
            /*
             * Use bounce interpolator with amplitude 0.2 and frequency 20
             * Gives the bounce affect on buttons
             */
            interpolator = new AnimationBounceInterpolator(0.2, 20);
            likeButtonBounceAnimation.setInterpolator(interpolator);
            shareButtonBounceAnimation.setInterpolator(interpolator);
            moreButtonBounceAnimation.setInterpolator(interpolator);

            /*
             * Setup animation for liking and reposting a PrismPost
             * The animations take place inside the same environment as the prismPostImageView
             */
            likeHeartAnimationImageView.setVisibility(View.INVISIBLE);
            likeHeartBounceAnimation.setInterpolator(interpolator);
            likeHeartBounceAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    likeHeartAnimationImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    likeHeartAnimationImageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            repostIrisAnimationImageView.setVisibility(View.INVISIBLE);
            repostIrisBounceAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    repostIrisAnimationImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    repostIrisAnimationImageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        /**
         * Three action buttons are shown for each PrismPost
         * Like button likes the PrismPost
         * Repost button reposts the PrismPost to the users profile
         * More button offers a few options
         */
        private void setupActionButtons() {
            /*
             * Like action button
             */
            String likeStringTail = likeCount == 1 ? " like" : " likes";
            likesCountTextView.setText(likeCount + likeStringTail);

            Drawable heartButtonDrawable = createLikeDrawable(postLiked);
            likeButton.setImageDrawable(heartButtonDrawable);
            Drawable heartDrawable = context.getResources().getDrawable(
                    postLiked ?  R.drawable.unlike_heart : R.drawable.like_heart);
            likeHeartAnimationImageView.setImageDrawable(heartDrawable);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = prismPost.getPostId();
                    boolean postLiked = !CurrentUser.user_liked_posts.containsKey(postId);
                    Drawable heartButtonDrawable = createLikeDrawable(postLiked);
                    likeButton.setImageDrawable(heartButtonDrawable);
                    Drawable heartDrawable = context.getResources().getDrawable(
                            postLiked ? R.drawable.like_heart : R.drawable.unlike_heart);
                    likeHeartAnimationImageView.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);
                    likeHeartAnimationImageView.startAnimation(likeHeartBounceAnimation);

                    likeCount += postLiked ? 1 : -1;
                    String likeStringTail = likeCount == 1 ? " like" : " likes";
                    likesCountTextView.setText(likeCount + likeStringTail);

                    handleLikeButtonClick(prismPost);
                }
            });

            likesCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userLikesIntent = new Intent(context, LikeRepostActivity.class);
                    userLikesIntent.putExtra("LikeRepostBoolean", 1); // Like = 1, Repost = 0
                    userLikesIntent.putExtra("LikeRepostPostId", postId);
                    context.startActivity(userLikesIntent);
                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });

            /*
             * Repost action button
             */
            String repostStringTail = repostCount == 1 ? " repost" : " reposts";
            repostsCountTextView.setText(repostCount + repostStringTail);

            ColorStateList repostColor = getRepostColor(postReposted);
            repostButton.setImageTintList(repostColor);
            repostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = prismPost.getPostId();
                    boolean postReposted = CurrentUser.user_reposted_posts.containsKey(postId);
                    if (postReposted) {
                        ColorStateList repostColor = getRepostColor(!postReposted);
                        repostButton.setImageTintList(repostColor);
                        repostButton.startAnimation(shareButtonBounceAnimation);
                        repostIrisAnimationImageView.startAnimation(unrepostIrisBounceAnimation);

                        repostCount--;
                        String repostStringTail = repostCount == 1 ? " repost" : " reposts";
                        repostsCountTextView.setText(repostCount + repostStringTail);

                        handleRepostButtonClick(prismPost);
                    } else {
                        AlertDialog repostConfirmationAlertDialog = createRepostConfirmationAlertDialog();
                        repostConfirmationAlertDialog.show();
                    }
                }
            });

            repostsCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userRepostsIntent = new Intent(context, LikeRepostActivity.class);
                    userRepostsIntent.putExtra("LikeRepostBoolean", 0); // Like = 1, Repost = 0
                    userRepostsIntent.putExtra("LikeRepostPostId", postId);
                    context.startActivity(userRepostsIntent);
                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });

            /*
             * More action button
             */
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreButton.startAnimation(moreButtonBounceAnimation);
                    // TODO: Show more menu
                    // TODO: Decide what goes in more
                    boolean isCurrentUserThePostCreator =  CurrentUser.user.getUid().equals(prismPost.getPrismUser().getUid());
                    AlertDialog morePrismPostAlertDialog = createMorePrismPostAlertDialog(isCurrentUserThePostCreator);
                    morePrismPostAlertDialog.show();
                }
            });
        }

        /**
         * Populate all UI elements with data
         */
        private void populateUIElements() {
            // Setup Typefaces for all text based UI elements
            prismUserTextView.setTypeface(sourceSansProBold);
            prismPostDateTextView.setTypeface(sourceSansProLight);
            likesCountTextView.setTypeface(sourceSansProLight);
            repostsCountTextView.setTypeface(sourceSansProLight);

            populatePostUserUIElements();
            setupPostImageView();
            setupUIAnimations();
            setupActionButtons();
        }

        /**
         * Pass in a boolean that toggles the icon and color of the like button
         */
        private Drawable createLikeDrawable(boolean isLiked) {
            int heart = isLiked ? R.drawable.ic_heart_black_36dp : R.drawable.ic_heart_outline_black_36dp;
            int color = isLiked ? R.color.colorAccent : android.R.color.white;
            Drawable heartDrawable = context.getResources().getDrawable(heart);
            int heartColor = context.getResources().getColor(color);
            heartDrawable.setTint(heartColor);
            return heartDrawable;
        }

        /**
         * Pass in a boolean that toggles the color of the repost button
         */
        private ColorStateList getRepostColor(boolean isReposted) {
            int color = isReposted ? R.color.colorAccent : android.R.color.white;
            int repostColor = context.getResources().getColor(color);
            return ColorStateList.valueOf(repostColor);
        }

        /**
         * Takes in the time of the post and creates a fancy string difference
         * Examples:
         * 10 seconds ago/Just now      (time < minute)
         * 20 minutes ago               (time < hour)
         * 2 hours ago                  (time < day)
         * 4 days ago                   (time < week)
         * January 21                   (time < year)
         * September 18, 2017           (else)
         */
        private String getFancyDateDifferenceString(long time) {
            // Create a calendar object and calculate the timeFromStart
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            long timeFromCurrent = calendar.getTimeInMillis() - time;

            // Set the calendar object to be the time of the post
            calendar.setTimeInMillis(time);

            // Calculate all units for the given timeFromCurrent
            long secondsTime = TimeUnit.MILLISECONDS.toSeconds(timeFromCurrent);
            long minutesTime = TimeUnit.MILLISECONDS.toMinutes(timeFromCurrent);
            long hoursTime = TimeUnit.MILLISECONDS.toHours(timeFromCurrent);
            long daysTime = TimeUnit.MILLISECONDS.toDays(timeFromCurrent);

            // The fancyDateString will start off as this DateFormat to satisfy the else case
            String fancyDateString = DateFormat.format("MMM dd, yyyy", calendar).toString();

            // Check each calculated time unit until it is clear the unit of timeFromCurrent
            if (secondsTime < MyTimeUnit.SECONDS_UNIT) {
//                String fancyDateTail = secondsTime == 1 ? " second ago" : " seconds ago";
//                fancyDateString = secondsTime + fancyDateTail;
                fancyDateString = "Just now";
            } else if (minutesTime < MyTimeUnit.MINUTES_UNIT) {
                String fancyDateTail = minutesTime == 1 ? " minute ago" : " minutes ago";
                fancyDateString = minutesTime + fancyDateTail;
            } else if (hoursTime < MyTimeUnit.HOURS_UNIT) {
                String fancyDateTail = hoursTime == 1 ? " hour ago" : " hours ago";
                fancyDateString = hoursTime + fancyDateTail;
            } else if (daysTime < MyTimeUnit.DAYS_UNIT) {
                String fancyDateTail = daysTime == 1 ? " day ago" : " days ago";
                fancyDateString = daysTime + fancyDateTail;
            } else if (daysTime < MyTimeUnit.YEARS_UNIT) {
                fancyDateString = DateFormat.format("MMM dd", calendar).toString();
            }
            return fancyDateString;
        }


        /**
         * AlertDialog to confirm the repost of a post
         */
        private AlertDialog createRepostConfirmationAlertDialog() {
            AlertDialog.Builder repostConfirmationAlertDialogBuilder = new AlertDialog.Builder(context);
            repostConfirmationAlertDialogBuilder.setTitle("This post will be shown on your profile, do you want to repost?");
            repostConfirmationAlertDialogBuilder
                    .setPositiveButton("REPOST", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            handleRepostButtonClick(prismPost);
                            ColorStateList repostColor = getRepostColor(true);
                            repostButton.setImageTintList(repostColor);
                            repostButton.startAnimation(shareButtonBounceAnimation);
                            repostIrisAnimationImageView.startAnimation(repostIrisBounceAnimation);

                            repostCount++;
                            String repostStringTail = repostCount == 1 ? " repost" : " reposts";
                            repostsCountTextView.setText(repostCount + repostStringTail);

                            handleRepostButtonClick(prismPost);
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            return repostConfirmationAlertDialogBuilder.create();
        }

        /**
         * AlertDialog that shows several options to the user when the moreActionButton is clicked
         * Report and SHare will show for all users
         * Delete will only show for user who posted the post
         */
        private AlertDialog createMorePrismPostAlertDialog(boolean isCurrentUser) {
            AlertDialog.Builder profilePictureAlertDialog = new AlertDialog.Builder(context);
            profilePictureAlertDialog.setItems(isCurrentUser ? morePostOptionsCurrentUser : morePostOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (which) {
                        case 0:
                            // Report post
                            break;
                        case 1:
                            // Share
                            break;
                        case 2:
                            // Delete
                            AlertDialog deleteConfirmationAlertDialog = createDeleteConfirmationAlertDialog();
                            deleteConfirmationAlertDialog.show();
                            break;
                        default:
                            break;
                    }
                }
            });
            return profilePictureAlertDialog.create();
        }

        /**
         * AlertDialog to confirm the deletion of a post
         */
        private AlertDialog createDeleteConfirmationAlertDialog() {
            AlertDialog.Builder exitAlertDialogBuilder = new AlertDialog.Builder(context);
            exitAlertDialogBuilder.setTitle("Are you sure you want to delete this post?");
            exitAlertDialogBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    FirebaseStorage.getInstance().getReferenceFromUrl(prismPost.getImage())
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (task.isSuccessful()) {
                                    String postId = prismPost.getPostId();
                                    allPostsReference.child(postId).removeValue();
                                    prismPostKeys.remove(postId);
                                    prismPostHashMap.remove(postId);
                                    notifyDataSetChanged();
                                    if (getItemCount() == 0) {
                                        RelativeLayout noMainPostsRelativeLayout = ((Activity) context).findViewById(R.id.no_main_posts_relative_layout);
                                        noMainPostsRelativeLayout.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            return exitAlertDialogBuilder.create();
        }

        /**
         * Check user_liked_posts HashMap if it contains the postId or not. If it contains
         * the postId, then user has already liked the post and perform UNLIKE operation
         * If it doesn't exist, user has not liked it yet, and perform LIKE operation
         * Operation LIKE (performLIKE = true): does 3 things. First it adds the the user's
         * uid to the LIKED_USERS table under the post. Then it adds the postId to the
         * USER_LIKES table under the user. Then it adds the postId and timestamp to the
         * local user_liked_posts HashMap so that recycler view can update
         * Operation UNLIKE (performLike = false): undoes above 3 things
         */
        private void handleLikeButtonClick(PrismPost prismPost) {
            String postId = prismPost.getPostId();
            long timestamp = Calendar.getInstance().getTimeInMillis();
            boolean performLike = !CurrentUser.user_liked_posts.containsKey(postId);

            DatabaseReference postReference = Default.ALL_POSTS_REFERENCE.child(postId);
            postReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    PrismPost post = mutableData.getValue(PrismPost.class);
                    if (post != null) {
                        if (performLike) {

                            // Add the user to LIKED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_LIKED_USERS)
                                    .child(CurrentUser.user.getDisplayName())
                                    .setValue(CurrentUser.user.getUid());

                            // Add postId to user's liked section
                            userReference.child(Key.DB_REF_USER_LIKES)
                                    .child(postId).setValue(timestamp);

                            // Add postId and timestamp to user_liked_posts hashMap
                            CurrentUser.user_liked_posts.put(postId, timestamp);

                        } else {

                            // Remove the user from LIKED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_LIKED_USERS)
                                    .child(CurrentUser.user.getDisplayName())
                                    .removeValue();

                            // Remove postId from user's liked section
                            userReference.child(Key.DB_REF_USER_LIKES)
                                    .child(postId).removeValue();

                            // Remove the postId and timestamp from user_liked_posts hashMap
                            CurrentUser.user_liked_posts.remove(postId);
                        }
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });
        }

        /**
         * Check user_reposted_posts HashMap if it contains the postId or not. If it contains
         * the postId, then user has already reposted the post and perform UNREPOST operation
         * If it doesn't exist, user has not reposted it yet, and perform REPOST operation
         * Operation REPOST (performRepost = true): does 3 things. First it adds the the user's
         * uid to the REPOSTED_USERS table under the post. Then it adds the postId to the
         * USER_REPOSTS table under the user. Then it adds the postId and timestamp to the
         * local user_reposted_posts HashMap so that recycler view can update
         * Operation UNREPOST (performRepost = false): undoes above 3 things
         */
        private void handleRepostButtonClick(PrismPost prismPost) {
            String postId = prismPost.getPostId();
            long timestamp = Calendar.getInstance().getTimeInMillis();
            boolean performRepost = !CurrentUser.user_reposted_posts.containsKey(postId);

            DatabaseReference postReference = Default.ALL_POSTS_REFERENCE.child(postId);
            postReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    PrismPost post = mutableData.getValue(PrismPost.class);
                    if (post != null) {
                        if (performRepost) {

                            // Add postId to user's reposts section
                            userReference.child(Key.DB_REF_USER_REPOSTS)
                                    .child(postId).setValue(timestamp);

                            // Add postId and timestamp to user_reposted_posts hashMap
                            CurrentUser.user_reposted_posts.put(postId, timestamp);

                            // Add the user to REPOSTED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_REPOSTED_USERS)
                                    .child(CurrentUser.user.getDisplayName())
                                    .setValue(CurrentUser.user.getUid());

                        } else {

                            // Remove postId from user's reposts section
                            userReference.child(Key.DB_REF_USER_LIKES)
                                    .child(postId).removeValue();

                            // Remove the postId and timestamp from user_reposted_posts hashMap
                            CurrentUser.user_reposted_posts.remove(postId);

                            // Remove the user from REPOSTED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_REPOSTED_USERS)
                                    .child(CurrentUser.user.getDisplayName())
                                    .removeValue();
                        }
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });
        }
    }
}