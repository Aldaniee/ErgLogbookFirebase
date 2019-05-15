package io.aidanlee.erglogbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    private static final String ENTRY_EXTRA = "entry_extra";

    public static final String DEFAULT_WORKOUT = "40:00";
    private static final String DEFAULT_RECORDED_UNITS = "time meters /500m spm";
    private final static String DEFAULT_OVERALL_RESULTS = "40:00.0 9722 2:03.4 20 145";
    private final static String[] DEFAUlT_BREAKDOWN_RESULTS = new String[] {
                    "8:00.0 1946 2:03.3 20 141",
                    "16:00.0 1946 2:03.5 20 144",
                    "24:00.0 1946 2:04.4 20 144",
                    "32:00.0 1946 2:02.9 20 149",
                    "40:00.0 1946 2:02.8 20 148"};
    private static final int DEFAULT_BREAKDOWN_LINES = 5;

    // Request Codes
    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;
    public static final int RC_EDIT_ENTRY = 3;

    private ListView mEntryListView;
    private EntryAdapter mEntryAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mWorkoutEditText;
    private Button mPostButton;

    private String mUsername;
    private List<Entry> entries;
    private String editKey; // used to save the position of an entry when it is edited

    // Floating Action Buttons
    private boolean fabOpen;
    private LinearLayout cameraLayout;
    private LinearLayout writeLayout;

    private FloatingActionButton fabCreate;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabWrite;

    private Animation showButton;
    private Animation hideButton;
    private Animation showLayout;
    private Animation hideLayout;

    // Firebase Instance Variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEntriesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        // Initialize Firebase components
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = mFirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mEntriesDatabaseReference = mFirebaseDatabase.getReference().child("adapterEntries");
        mPhotosStorageReference = mFirebaseStorage.getReference().child("photos");



        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        mEntryListView = findViewById(R.id.resultListView);
        initializeFloatingActionButton();

        // Initialize entry ListView and its adapter
        entries = new ArrayList<Entry>();
        mEntryAdapter = new EntryAdapter(this, R.layout.item_entry, entries);
        mEntryListView.setAdapter(mEntryAdapter);
        mEntryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getApplicationContext(), EntryDisplayActivity.class);

                Entry entry = entries.get(position);
                //Passes the entry (as a parcelable object) into the display activity
                i.putExtra(ENTRY_EXTRA, entry);
                startActivityForResult(i, RC_EDIT_ENTRY);
            }
        });

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    onSignedInInitialized(user.getDisplayName());
                } else {
                    // user is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }
    private void initializeFloatingActionButton() {
        fabCreate = findViewById(R.id.fab_create_new_item);

        fabCamera = findViewById(R.id.fab_camera);
        fabWrite = findViewById(R.id.fab_write);
        cameraLayout = findViewById(R.id.camera_layout);
        writeLayout = findViewById(R.id.write_layout);

        showButton = AnimationUtils.loadAnimation(this, R.anim.show_button);
        hideButton = AnimationUtils.loadAnimation(this, R.anim.hide_button);
        showLayout = AnimationUtils.loadAnimation(this, R.anim.show_layout);
        hideLayout = AnimationUtils.loadAnimation(this, R.anim.hide_layout);
        fabCreate.startAnimation(hideButton);
        cameraLayout.setVisibility(View.INVISIBLE);
        writeLayout.setVisibility(View.INVISIBLE);
        fabOpen = false;
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabOpen) {
                    cameraLayout.setVisibility(View.INVISIBLE);
                    writeLayout.setVisibility(View.INVISIBLE);
                    cameraLayout.startAnimation(hideLayout);
                    writeLayout.startAnimation(hideLayout);
                    fabCreate.startAnimation(hideButton);
                    fabOpen = false;
                } else {
                    cameraLayout.setVisibility(View.VISIBLE);
                    writeLayout.setVisibility(View.VISIBLE);
                    cameraLayout.startAnimation(showLayout);
                    writeLayout.startAnimation(showLayout);
                    fabCreate.startAnimation(showButton);
                    fabOpen = true;
                }
            }
        });
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        fabWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // Get a reference to store file at chat_photos/<FILENAME>
            final StorageReference photoRef = mPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            mPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //When the image has successfully uploaded, get its download URL
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String photoUrl = uri.toString();
                                    String key = mEntriesDatabaseReference.push().getKey();
                                    Entry entry = new Entry(mUsername, key, photoUrl);
                                    entry.setWorkout(DEFAULT_WORKOUT);
                                    entry.setOverallString(DEFAULT_OVERALL_RESULTS, DEFAULT_RECORDED_UNITS);
                                    entry.setBreakdownString(DEFAUlT_BREAKDOWN_RESULTS, DEFAULT_BREAKDOWN_LINES, DEFAULT_RECORDED_UNITS);
                                    mEntriesDatabaseReference.child(key).setValue(entry);
                                }
                            });
                        }
                    });
        }
        else if (requestCode == RC_EDIT_ENTRY) {
            if(data != null && data.hasExtra(ENTRY_EXTRA)) {
                Entry newEntry = data.getParcelableExtra(ENTRY_EXTRA);
                String key = newEntry.getKey();
                mEntriesDatabaseReference.child(key).setValue(newEntry);
            }
        }
    }

    private void onSignedInInitialized(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mEntryAdapter.clear();
        detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        detachDatabaseReadListener();
        mEntryAdapter.clear();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Entry entry = dataSnapshot.getValue(io.aidanlee.erglogbook.Entry.class); //deserializes to an entry object
                    mEntryAdapter.add(entry);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Entry entry = dataSnapshot.getValue(io.aidanlee.erglogbook.Entry.class);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //Typically called when you don't have permission to view entry
                }
            };
            mEntriesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mEntriesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
    private String getCurrentTimeAndDate() {
        Date currentDate = Calendar.getInstance().getTime();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd/kk:mm:ss", Locale.US);
        return format.format(currentDate);
    }
    private void makeToast(String input) {
        Toast.makeText(getApplicationContext(), input, Toast.LENGTH_SHORT).show();
    }
}
