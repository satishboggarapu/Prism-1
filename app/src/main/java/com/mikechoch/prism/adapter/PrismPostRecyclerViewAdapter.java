package com.mikechoch.prism.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikechoch.prism.activity.PrismUserProfileActivity;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.helper.AnimationBounceInterpolator;
import com.mikechoch.prism.attribute.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.UsersActivity;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
    private ArrayList<PrismPost> prismPostArrayList;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;
    private String[] morePostOptionsCurrentUser = {"Report post", "Share", "Delete"};
    private String[] morePostOptions = {"Report post", "Share"};


    public PrismPostRecyclerViewAdapter(Context context, ArrayList<PrismPost> prismPostArrayList, int[] screenDimens) {
        this.context = context;
        this.prismPostArrayList = prismPostArrayList;
        this.screenWidth = screenDimens[0];
        this.screenHeight = screenDimens[1];

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
                        R.layout.prism_post_recycler_view_item_layout, parent, false));
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

        private FirebaseAuth auth;
        private DatabaseReference postAuthorUserReference;
        private StorageReference storageReference;
        private DatabaseReference allPostsReference;

        private Animation likeHeartBounceAnimation;
        private Animation repostIrisBounceAnimation;
        private Animation unrepostIrisBounceAnimation;
        private Animation likeButtonBounceAnimation;
        private Animation shareButtonBounceAnimation;
        private Animation moreButtonBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        private ImageView userProfilePicImageView;
        private RelativeLayout postInformationRelativeLayout;
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

        private PrismPost prismPost;
        private String postId;
        private String postDate;
        private Integer likeCount;
        private Integer repostCount;
        private boolean postLiked;
        private boolean postReposted;


        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            postAuthorUserReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());
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
            postInformationRelativeLayout = itemView.findViewById(R.id.recycler_view_post_info_relative_layout);
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
            postDate = Helper.getFancyDateDifferenceString(prismPost.getTimestamp() * -1);
            likeCount = this.prismPost.getLikes();
            repostCount = this.prismPost.getReposts();
            postLiked = CurrentUser.liked_posts_map.containsKey(postId);
            postReposted = CurrentUser.reposted_posts_map.containsKey(postId);

            if (likeCount == null) likeCount = 0;
            if (repostCount == null) repostCount = 0;
            populateUIElements();
        }

        /**
         * Populate fields related to the user who posted
         * Handles fields for user profile picture, username, and post date
         */
        private void setupPostUserUIElements() {
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
                                    int whiteOutlinePadding = (int) (1.5 * scale);
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

                postInformationRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intentToUserProfileActivity();
                    }
                });
            }
        }

        /**
         * Intent from the current clicked PrismPost user to their PrismUserProfileActivity
         */
        private void intentToUserProfileActivity() {
            Intent prismUserProfileIntent = new Intent(context, PrismUserProfileActivity.class);
            prismUserProfileIntent.putExtra("PrismUser", prismPost.getPrismUser());
            context.startActivity(prismUserProfileIntent);
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                    boolean postLiked = !CurrentUser.liked_posts_map.containsKey(postId);

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
            Drawable heartButtonDrawable = createLikeDrawable(postLiked);
            likeButton.setImageDrawable(heartButtonDrawable);
            Drawable heartDrawable = context.getResources().getDrawable(
                    postLiked ?  R.drawable.unlike_heart : R.drawable.like_heart);
            likeHeartAnimationImageView.setImageDrawable(heartDrawable);

            String likeStringTail = likeCount == 1 ? " like" : " likes";
            likesCountTextView.setText(likeCount + likeStringTail);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = prismPost.getPostId();
                    boolean postLiked = !CurrentUser.liked_posts_map.containsKey(postId);

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
                    Intent userLikesIntent = new Intent(context, UsersActivity.class);
                    userLikesIntent.putExtra("UsersInt", 0);
                    userLikesIntent.putExtra("UsersDataId", postId);
                    context.startActivity(userLikesIntent);
                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });

            /*
             * Repost action button
             */
            Drawable repostDrawable = createRepostDrawable(postReposted);
            repostButton.setImageDrawable(repostDrawable);

            String repostStringTail = repostCount == 1 ? " repost" : " reposts";
            repostsCountTextView.setText(repostCount + repostStringTail);

            repostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = prismPost.getPostId();
                    boolean postReposted = !CurrentUser.reposted_posts_map.containsKey(postId);
                    if (!postReposted) {
                        Drawable repostDrawable = createRepostDrawable(postReposted);
                        repostButton.setImageDrawable(repostDrawable);
                        repostButton.startAnimation(shareButtonBounceAnimation);
                        repostIrisAnimationImageView.startAnimation(unrepostIrisBounceAnimation);

                        repostCount--;
                        String repostStringTail = repostCount == 1 ? " repost" : " reposts";
                        repostsCountTextView.setText(repostCount + repostStringTail);

                        handleRepostButtonClick(prismPost);
                    } else {
                        repostIrisAnimationImageView.setVisibility(View.INVISIBLE);
                        AlertDialog repostConfirmationAlertDialog = createRepostConfirmationAlertDialog();
                        repostConfirmationAlertDialog.show();
                    }
                }
            });

            repostsCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userRepostsIntent = new Intent(context, UsersActivity.class);
                    userRepostsIntent.putExtra("UsersInt", 1);
                    userRepostsIntent.putExtra("UsersDataId", postId);
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
                    boolean isCurrentUserThePostCreator =  CurrentUser.firebaseUser.getUid().equals(prismPost.getPrismUser().getUid());
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

            setupPostUserUIElements();
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
        private Drawable createRepostDrawable(boolean isReposted) {
            int repost = R.drawable.ic_camera_iris_black_36dp;
            Drawable repostDrawable = context.getResources().getDrawable(repost);
            int color = isReposted ? R.color.colorAccent : android.R.color.white;
            int repostColor = context.getResources().getColor(color);
            repostDrawable.setTint(repostColor);
            return repostDrawable;
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
                            Drawable repostDrawable = createRepostDrawable(true);
                            repostButton.setImageDrawable(repostDrawable);
                            repostButton.startAnimation(shareButtonBounceAnimation);
                            repostIrisAnimationImageView.startAnimation(repostIrisBounceAnimation);

                            repostCount++;
                            String repostStringTail = repostCount == 1 ? " repost" : " reposts";
                            repostsCountTextView.setText(repostCount + repostStringTail);

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
                    handleDeletionOfPost();

                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            return exitAlertDialogBuilder.create();
        }

        private void handleDeletionOfPost() {
            // Remove the post image from storage
            FirebaseStorage.getInstance().getReferenceFromUrl(prismPost.getImage())
                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        String postId = prismPost.getPostId();
                        String postUserId = prismPost.getPrismUser().getUid();
                        allPostsReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    HashMap<String, String> likedUsers = new HashMap<>();
                                    HashMap<String, String> repostedUsers = new HashMap<>();
                                    DatabaseReference usersReference = Default.USERS_REFERENCE;
                                    // Removes postId from all user's USER_LIKES section
                                    if (dataSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getChildrenCount() > 0) {
                                        likedUsers.putAll((Map) dataSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getValue());
                                        for (String userId : likedUsers.values()) {
                                            usersReference.child(userId)
                                                    .child(Key.DB_REF_USER_LIKES)
                                                    .child(postId).removeValue();
                                        }
                                        Toast.makeText(context, "Removed Liked Users", Toast.LENGTH_SHORT).show();
                                    }

                                    // Removes postId from all users's USER_REPOSTS section
                                    if (dataSnapshot.child(Key.DB_REF_POST_REPOSTED_USERS).getChildrenCount() > 0) {
                                        repostedUsers.putAll((Map) dataSnapshot.child(Key.DB_REF_POST_REPOSTED_USERS).getValue());
                                        for (String userId : repostedUsers.values()) {
                                            usersReference.child(userId)
                                                    .child(Key.DB_REF_USER_REPOSTS)
                                                    .child(postId).removeValue();
                                        }
                                        Toast.makeText(context, "Removed Reposted Users", Toast.LENGTH_SHORT).show();
                                    }

                                    // Removes postId from author user's USER_UPLOADS section
                                    postAuthorUserReference
                                            .child(Key.DB_REF_USER_UPLOADS)
                                            .child(postId).removeValue();
                                    Toast.makeText(context, "Removed author of post", Toast.LENGTH_SHORT).show();

                                    // Removes the entire post from ALL_POSTS section
                                    allPostsReference.child(postId).removeValue();
                                    Toast.makeText(context, "Removed Post From ALL_POSTS", Toast.LENGTH_SHORT).show();

                                    // Remove post from local hashMap
                                    prismPostArrayList.remove(prismPost);
                                    notifyDataSetChanged();

                                } else {
                                    Log.wtf(Default.TAG_DB, Message.NO_DATA);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                databaseError.toException().printStackTrace();
                                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
                            }
                        });

                        if (getItemCount() == 0) {
                            RelativeLayout noMainPostsRelativeLayout = ((Activity) context).findViewById(R.id.no_main_posts_relative_layout);
                            noMainPostsRelativeLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // TODO error log
                        Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        /**
         * Check liked_posts_map HashMap if it contains the postId or not. If it contains
         * the postId, then firebaseUser has already liked the post and perform UNLIKE operation
         * If it doesn't exist, firebaseUser has not liked it yet, and perform LIKE operation
         * Operation LIKE (performLIKE = true): does 3 things. First it adds the the firebaseUser's
         * uid to the LIKED_USERS table under the post. Then it adds the postId to the
         * USER_LIKES table under the firebaseUser. Then it adds the postId and timestamp to the
         * local liked_posts_map HashMap so that recycler view can update
         * Operation UNLIKE (performLike = false): undoes above 3 things
         */
        private void handleLikeButtonClick(PrismPost prismPost) {
            String postId = prismPost.getPostId();
            long timestamp = Calendar.getInstance().getTimeInMillis();
            boolean performLike = !CurrentUser.liked_posts_map.containsKey(postId);

            DatabaseReference postReference = Default.ALL_POSTS_REFERENCE.child(postId);
            postReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    PrismPost post = mutableData.getValue(PrismPost.class);
                    if (post != null) {
                        if (performLike) {

                            // Add the firebaseUser to LIKED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_LIKED_USERS)
                                    .child(CurrentUser.firebaseUser.getDisplayName())
                                    .setValue(CurrentUser.firebaseUser.getUid());

                            // Add postId to firebaseUser's liked section
                            postAuthorUserReference.child(Key.DB_REF_USER_LIKES)
                                    .child(postId).setValue(timestamp);

                            // Add postId and timestamp to liked_posts_map hashMap
                            CurrentUser.liked_posts_map.put(postId, timestamp);

                        } else {

                            // Remove the firebaseUser from LIKED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_LIKED_USERS)
                                    .child(CurrentUser.firebaseUser.getDisplayName())
                                    .removeValue();

                            // Remove postId from firebaseUser's liked section
                            postAuthorUserReference.child(Key.DB_REF_USER_LIKES)
                                    .child(postId).removeValue();

                            // Remove the postId and timestamp from liked_posts_map hashMap
                            CurrentUser.liked_posts_map.remove(postId);
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
         * Check reposted_posts_map HashMap if it contains the postId or not. If it contains
         * the postId, then firebaseUser has already reposted the post and perform UNREPOST operation
         * If it doesn't exist, firebaseUser has not reposted it yet, and perform REPOST operation
         * Operation REPOST (performRepost = true): does 3 things. First it adds the the firebaseUser's
         * uid to the REPOSTED_USERS table under the post. Then it adds the postId to the
         * USER_REPOSTS table under the firebaseUser. Then it adds the postId and timestamp to the
         * local reposted_posts_map HashMap so that recycler view can update
         * Operation UNREPOST (performRepost = false): undoes above 3 things
         */
        private void handleRepostButtonClick(PrismPost prismPost) {
            String postId = prismPost.getPostId();
            long timestamp = Calendar.getInstance().getTimeInMillis();
            boolean performRepost = !CurrentUser.reposted_posts_map.containsKey(postId);

            DatabaseReference postReference = Default.ALL_POSTS_REFERENCE.child(postId);
            postReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    PrismPost post = mutableData.getValue(PrismPost.class);
                    if (post != null) {
                        if (performRepost) {

                            // Add postId to firebaseUser's reposts section
                            postAuthorUserReference.child(Key.DB_REF_USER_REPOSTS)
                                    .child(postId).setValue(timestamp);

                            // Add postId and timestamp to reposted_posts_map hashMap
                            CurrentUser.reposted_posts_map.put(postId, timestamp);

                            // Add the firebaseUser to REPOSTED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_REPOSTED_USERS)
                                    .child(CurrentUser.firebaseUser.getDisplayName())
                                    .setValue(CurrentUser.firebaseUser.getUid());

                        } else {

                            // Remove postId from firebaseUser's reposts section
                            postAuthorUserReference.child(Key.DB_REF_USER_LIKES)
                                    .child(postId).removeValue();

                            // Remove the postId and timestamp from reposted_posts_map hashMap
                            CurrentUser.reposted_posts_map.remove(postId);

                            // Remove the firebaseUser from REPOSTED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_REPOSTED_USERS)
                                    .child(CurrentUser.firebaseUser.getDisplayName())
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
