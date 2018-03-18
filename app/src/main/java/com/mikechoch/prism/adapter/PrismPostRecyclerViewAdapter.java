package com.mikechoch.prism.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.mikechoch.prism.InterfaceAction;
import com.mikechoch.prism.activity.PrismPostDetailActivity;
import com.mikechoch.prism.activity.PrismUserProfileActivity;
import com.mikechoch.prism.fire.DatabaseAction;
import com.mikechoch.prism.fire.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.DisplayUsersActivity;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.helper.BitmapHelper;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;

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
    public static ArrayList<PrismPost> prismPostArrayList;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;


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

        private RelativeLayout prismPostRelativeLayout;
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
        private boolean isPostLiked;
        private boolean isPostReposted;


        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            postAuthorUserReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());
            storageReference = Default.STORAGE_REFERENCE.child(Key.STORAGE_POST_IMAGES_REF);
            allPostsReference = Default.ALL_POSTS_REFERENCE;

            // Image initializations
            progressBar = itemView.findViewById(R.id.prism_post_progress_bar);
            prismPostRelativeLayout = itemView.findViewById(R.id.prism_post_item_relative_layout);
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
            isPostLiked = CurrentUser.hasLiked(prismPost);
            isPostReposted = CurrentUser.hasReposted(prismPost);

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
                    .load(prismPost.getImage())
                    .apply(new RequestOptions().fitCenter().override((int) (screenWidth * 0.9), (int) (screenHeight * 0.6)))
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
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            prismPostRelativeLayout.setBackground(new BitmapDrawable(
                                                    context.getResources(), BitmapHelper.blur(resource, 0.2f, 100)));
                                            prismPostImageView.animate()
                                                    .alpha(1f)
                                                    .setDuration(250)
                                                    .start();
                                            progressBar.animate()
                                                    .alpha(0f)
                                                    .setDuration(0)
                                                    .withEndAction(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    }).start();
                                        }
                                    });
                                }
                            }).start();
                            return false;
                        }
                    }).into(prismPostImageView);

            /*
             * GestureDetector used to replace the prismPostImageView TouchListener
             * This allows detection of Single, Long, and Double tap gestures
             */
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    System.out.println("Image Single Tapped");
                    Intent prismPostDetailIntent = new Intent(context, PrismPostDetailActivity.class);

                    prismPostDetailIntent.putExtra("PrismPostDetail", prismPost);
                    prismPostDetailIntent.putExtra("PrismPostDetailTransitionName", ViewCompat.getTransitionName(prismPostImageView));

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            prismPostImageView,
                            ViewCompat.getTransitionName(prismPostImageView));

                    context.startActivity(prismPostDetailIntent, options.toBundle());
//                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

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
                    handleLikeButtonClick();
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
         * Three action buttons are shown for each PrismPost
         * Like button likes the PrismPost
         * Repost button reposts the PrismPost to the users profile
         * More button offers a few options
         */
        private void setupActionButtons() {
            setupLikeActionButton();
            setupRepostActionButton();
            setupMoreActionButton();
        }

        /**
         *
         */
        private void setupMoreActionButton() {
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InterfaceAction.startMoreActionButtonAnimation(moreButton);
                    // TODO: Show more menu
                    // TODO: Decide what goes in more
                    boolean isCurrentUserThePostCreator = CurrentUser.firebaseUser.getUid().equals(prismPost.getPrismUser().getUid());
                    AlertDialog morePrismPostAlertDialog = InterfaceAction.createMorePrismPostAlertDialog(context, prismPost, isCurrentUserThePostCreator);
                    morePrismPostAlertDialog.show();
                }
            });
        }

        /**
         *
         */
        private void setupRepostActionButton() {
            InterfaceAction.setupRepostActionButton(context, repostButton, isPostReposted);

            String repostStringTail = Helper.getSingularOrPluralText(" repost", repostCount);
            repostsCountTextView.setText(repostCount + repostStringTail);

            repostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean performRepost = !CurrentUser.hasReposted(prismPost);
                    if (performRepost) {
                        repostIrisAnimationImageView.setVisibility(View.INVISIBLE);
                        AlertDialog repostConfirmationAlertDialog = InterfaceAction.createRepostConfirmationAlertDialog(context, prismPost, repostButton, repostsCountTextView);
                        repostConfirmationAlertDialog.show();
                    } else {
                        handleRepostButtonClick(false);
                    }
                }
            });

            repostsCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userRepostsIntent = new Intent(context, DisplayUsersActivity.class);
                    userRepostsIntent.putExtra("UsersInt", 1);
                    userRepostsIntent.putExtra("UsersDataId", postId);
                    context.startActivity(userRepostsIntent);
                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }

        /**
         *
         */
        private void setupLikeActionButton() {
            InterfaceAction.setupLikeActionButton(context, likeHeartAnimationImageView, likeButton, isPostLiked);

            String likeString = likeCount + Helper.getSingularOrPluralText(" like", likeCount);
            likesCountTextView.setText(likeString);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleLikeButtonClick();
                }
            });

            likesCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userLikesIntent = new Intent(context, DisplayUsersActivity.class);
                    userLikesIntent.putExtra("UsersInt", 0);
                    userLikesIntent.putExtra("UsersDataId", postId);
                    context.startActivity(userLikesIntent);
                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
            setupActionButtons();
        }

        //TODO: Handle other UI for deleting a post
        private void handleDeletionOfPost() {
            if (getItemCount() == 0) {
                    RelativeLayout noMainPostsRelativeLayout = ((Activity) context)
                            .findViewById(R.id.no_main_posts_relative_layout);
                    noMainPostsRelativeLayout.setVisibility(View.VISIBLE);
            }
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
         * TODO: update comments
         */
        private void handleLikeButtonClick() {
            boolean performLike = !CurrentUser.hasLiked(prismPost);
            performUIActivitiesForLike(performLike);

            if (performLike) {
                DatabaseAction.performLike(prismPost);
            } else {
                DatabaseAction.performUnlike(prismPost);
            }
        }

        private void performUIActivitiesForLike(boolean performLike) {
            InterfaceAction.startLikeActionButtonAnimation(context, likeButton, performLike);
            InterfaceAction.startLikeActionAnimation(context, likeHeartAnimationImageView, performLike);

            likeCount = performLike ? likeCount + 1 : likeCount - 1;
            prismPost.setLikes(likeCount);
            String likeString = likeCount + Helper.getSingularOrPluralText(" like", likeCount);
            likesCountTextView.setText(likeString);
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
         * TODO: update comments
         * @param performRepost
         */
        private void handleRepostButtonClick(boolean performRepost) {
            performUIActivitiesForRepost(performRepost);

            if (performRepost) {
                DatabaseAction.performRepost(prismPost);
            } else {
                DatabaseAction.performUnrepost(prismPost);
            }
        }

        private void performUIActivitiesForRepost(boolean performRepost) {
            InterfaceAction.startRepostActionButtonAnimation(context, repostButton, performRepost);

            repostCount = prismPost.getReposts() + (performRepost ? 1 : -1);
            prismPost.setReposts(repostCount);
            String repostString = repostCount + Helper.getSingularOrPluralText(" repost", repostCount);
            repostsCountTextView.setText(repostString);
        }

    }
}
