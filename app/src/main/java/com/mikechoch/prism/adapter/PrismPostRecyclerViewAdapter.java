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
import com.mikechoch.prism.helper.AnimationBounceInterpolator;
import com.mikechoch.prism.attribute.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.LikeRepostActivity;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.MyTimeUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by mikechoch on 1/21/18.
 */

public class PrismPostRecyclerViewAdapter extends RecyclerView.Adapter<PrismPostRecyclerViewAdapter.ViewHolder> {

    private final int PROGRESS_BAR_VIEW_TYPE = 0;
    private final int PRISM_POST_VIEW_TYPE = 1;

    private Context context;
    private PrismPost prismPost;
    private ArrayList<PrismPost> prismPostArrayList;

    private String[] morePostOptionsCurrentUser = {"Report post", "Share", "Delete"};
    private String[] morePostOptions = {"Report post", "Share"};

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private int screenWidth;
    private int screenHeight;

    private float scale;

    public PrismPostRecyclerViewAdapter(Context context, ArrayList<PrismPost> prismPostArrayList, int[] screenDimens) {
        this.context = context;
        this.prismPostArrayList = prismPostArrayList;
        this.screenWidth = screenDimens[0];
        this.screenHeight = screenDimens[1];

        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");

        scale = context.getResources().getDisplayMetrics().density;
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
        holder.setData(prismPostArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return prismPostArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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

        private Animation likeHeartBounceAnimation;
        private Animation repostIrisBounceAnimation;
        private Animation unrepostIrisBounceAnimation;
        private Animation likeButtonBounceAnimation;
        private Animation shareButtonBounceAnimation;
        private Animation moreButtonBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        private FirebaseAuth auth;
        private DatabaseReference userReference;
        private StorageReference storageReference;
        private DatabaseReference allPostsReference;

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

        public void setData(PrismPost prismPostObject) {
            this.prismPost = prismPostObject;

            /*
             * Post ID
             */
            String postId = this.prismPost.getPostId();
            String postDate = getFancyDateDifferenceString(prismPost.getTimestamp() * -1);
            final int[] likeCount = {this.prismPost.getLikes()};
            final int[] repostCount = {this.prismPost.getReposts()};
            boolean postLiked = CurrentUser.user_liked_posts.containsKey(postId);
            boolean postReposted = CurrentUser.user_reposted_posts.containsKey(postId);

            /*
             * Username
             */
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
                prismUserTextView.setTypeface(sourceSansProBold);
                prismPostDateTextView.setText(postDate);
                prismPostDateTextView.setTypeface(sourceSansProLight);
            }

            /*
             * Image
             */
            progressBar.setVisibility(View.VISIBLE);

            /*
             * prismPostImageView will have a width of 90% of the screen
             * prismPostImageView will have a max height of 60% of the screen
             * This causes any images that are stronger in height to not span the entire screen
             */
            prismPostImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            prismPostImageView.setMaxHeight((int) (screenHeight * 0.6));

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

                    likeCount[0] += postLiked ? 1 : -1;
                    String likeStringTail = likeCount[0] == 1 ? " like" : " likes";
                    likesCountTextView.setText(likeCount[0] + likeStringTail);

                    handleLikeButtonClick(prismPost);
                    return super.onDoubleTap(e);
                }
            });

            prismPostImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    /*
                     * Sets the TouchListener to be handled by the GestureDetector class
                     * This allows detection of Single, Long, and Double tap gestures
                     */
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });

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
             * Animation
             */
            // Use bounce interpolator with amplitude 0.2 and frequency 20
            interpolator = new AnimationBounceInterpolator(0.2, 20);
            likeButtonBounceAnimation.setInterpolator(interpolator);
            shareButtonBounceAnimation.setInterpolator(interpolator);
            moreButtonBounceAnimation.setInterpolator(interpolator);

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
//            repostIrisBounceAnimation.setInterpolator(interpolator);
//            unrepostIrisBounceAnimation.setInterpolator(interpolator);
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


            /*
             * Like
             */
            String likeStringTail = likeCount[0] == 1 ? " like" : " likes";
            likesCountTextView.setText(likeCount[0] + likeStringTail);
            likesCountTextView.setTypeface(sourceSansProLight);

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

                    likeCount[0] += postLiked ? 1 : -1;
                    String likeStringTail = likeCount[0] == 1 ? " like" : " likes";
                    likesCountTextView.setText(likeCount[0] + likeStringTail);

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
             * Repost
             */
            String repostStringTail = repostCount[0] == 1 ? " repost" : " reposts";
            repostsCountTextView.setText(repostCount[0] + repostStringTail);
            repostsCountTextView.setTypeface(sourceSansProLight);

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

                        repostCount[0]--;
                        String repostStringTail = repostCount[0] == 1 ? " repost" : " reposts";
                        repostsCountTextView.setText(repostCount[0] + repostStringTail);

                        handleRepostButtonClick(prismPost);
                    } else {
                        AlertDialog repostConfirmationAlertDialog = createRepostConfirmationAlertDialog(repostCount);
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
             * More
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
         *
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
         *
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
         *
         */
        private AlertDialog createRepostConfirmationAlertDialog(int[] repostCount) {
            AlertDialog.Builder repostConfirmationAlertDialogBuilder = new AlertDialog.Builder(context);
            repostConfirmationAlertDialogBuilder.setTitle("This post will be shown on your profile, do you want to repost?");
            repostConfirmationAlertDialogBuilder
                    .setPositiveButton("REPOST", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    handleRepostButtonClick(prismPost);
                    ColorStateList repostColor = getRepostColor(true);
                    repostButton.setImageTintList(repostColor);
                    repostButton.startAnimation(shareButtonBounceAnimation);
                    repostIrisAnimationImageView.startAnimation(repostIrisBounceAnimation);

                    repostCount[0]++;
                    String repostStringTail = repostCount[0] == 1 ? " repost" : " reposts";
                    repostsCountTextView.setText(repostCount[0] + repostStringTail);

                    handleRepostButtonClick(prismPost);

                    dialogInterface.dismiss();
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
         *
         */
        private AlertDialog createMorePrismPostAlertDialog(boolean isCurrentUser) {
            AlertDialog.Builder profilePictureAlertDialog = new AlertDialog.Builder(context);
            profilePictureAlertDialog.setItems(isCurrentUser ? morePostOptionsCurrentUser : morePostOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // Report post
                            break;
                        case 1:
                            // Share
                            break;
                        case 2:
                            // Delete
                            FirebaseStorage.getInstance().getReferenceFromUrl(prismPost.getImage())
                                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (task.isSuccessful()) {
                                            String postId = prismPost.getPostId();
                                            allPostsReference.child(postId).removeValue();
                                            prismPostArrayList.remove(postId);
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
                            break;
                        default:
                            break;
                    }
                }
            });

            return profilePictureAlertDialog.create();
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
