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

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public static final DatabaseReference ALL_POSTS_REFERENCE = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_ALL_POSTS);
    public static final DatabaseReference USERS_REFERENCE = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_USER_PROFILES);
    public static final StorageReference STORAGE_REFERENCE = FirebaseStorage.getInstance().getReference();
}
