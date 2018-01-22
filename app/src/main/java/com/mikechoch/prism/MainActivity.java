package com.mikechoch.prism;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private FloatingActionButton uploadImageFab;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private LayoutInflater layoutInflater;

    private ArrayList<Wallpaper> listOfImages;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_logout:
                auth.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        listOfImages = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");//.child(auth.getCurrentUser().getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        listOfImages.clear(); // clearing arrayList so that
                        // for each user
                        for (DataSnapshot user: dataSnapshot.getChildren()) {
                            System.out.println(user);
                            // for pics for each user
                            for (DataSnapshot snapshot : user.getChildren()) {
                                String caption = (String) snapshot.child("caption").getValue();
                                String imageUri = (String) snapshot.child("image").getValue();
                                listOfImages.add(new Wallpaper(caption, imageUri));
                            }
                        }
                        refreshPageWithImages();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTextView = findViewById(R.id.toolbar_text_view);
        toolbarTextView.setTypeface(sourceSansProBold);
        setSupportActionBar(toolbar);

        layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        recyclerView = findViewById(R.id.my_recycler_view);

        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");
        arrayList.add("Hello");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this, arrayList, null);
        recyclerView.setAdapter(recyclerViewAdapter);

        uploadImageFab = findViewById(R.id.upload_image_fab);

        // Ask user for write permissions to external storage
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }

        // Initialize uploadImageFab and OnClickListener to take you to ImageUploadActivity
        uploadImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageUploadIntent = new Intent( MainActivity.this, ImageUploadActivity.class);
                startActivity(imageUploadIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });




    }

    /**
     * TODO populate card views with images from listOfImages
     */
    private void refreshPageWithImages() {
        System.out.println(listOfImages);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_MEDIA:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Code here for allowing write permission
                }
                break;
            default:
                break;
        }
    }
}
