package com.mikechoch.prism;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;

/**
 * Created by parth on 1/22/18.
 */

public class Default {

    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public static final DatabaseReference ALL_POSTS_REFERENCE = databaseReference.child(Key.DB_REF_ALL_POSTS);
    public static final DatabaseReference USERS_REFERENCE = databaseReference.child(Key.DB_REF_USER_PROFILES);
    public static final DatabaseReference ACCOUNT_REFERENCE = databaseReference.child(Key.DB_REF_ACCOUNTS);

    public static final StorageReference STORAGE_REFERENCE = FirebaseStorage.getInstance().getReference();

    public static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 99;
    public static final int IMAGE_UPLOAD_INTENT_REQUEST_CODE = 100;
    public static final int GALLERY_INTENT_REQUEST = 101;
    public static final int PROFILE_PIC_UPLOAD_INTENT_REQUEST_CODE = 102;

    public static final int IMAGE_LOAD_THRESHOLD = 3;
    public static final int IMAGE_LOAD_COUNT = 10;

    public static final int VIEW_PAGER_SIZE = 5 - 1;
    public static final int VIEW_PAGER_HOME = 0;
    public static final int VIEW_PAGER_TRENDING = 1;
    public static final int VIEW_PAGER_SEARCH = 2;
    public static final int VIEW_PAGER_NOTIFICATIONS = 3;
    public static final int VIEW_PAGER_PROFILE = 4;
}
