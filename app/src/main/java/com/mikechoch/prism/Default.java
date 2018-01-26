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
}
