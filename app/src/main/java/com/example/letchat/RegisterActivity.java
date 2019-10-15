package com.example.letchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.provider.MediaStore.EXTRA_OUTPUT;

public class RegisterActivity extends AppCompatActivity {


    private EditText password1, password2, username, email;
    private Button regBtn,gobackBtn,tkPicBtn,chPicBtn;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    FirebaseFirestore db;
    private String user_id;
    private LinearLayout photoMethodLayout;

    private ProgressBar progressBar;
    private ImageButton profilePic;
    ImageView ivPreview;
    private ListView listView;
    private static final String TAG = RegisterActivity.class.getName();

    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo1.jpg";
    File photoFile;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeUI();

        regBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pickMethod();
                photoMethodLayout.setVisibility(v.VISIBLE);
            }
        });
        tkPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoMethodLayout.setVisibility(v.GONE);
                onLaunchCamera(v);
            }
        });
        chPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoMethodLayout.setVisibility(v.GONE);
                onPickPhoto(v);
            }
        });
    }
    private void registerNewUser(){
        mAuth = FirebaseAuth.getInstance();
        //user_id = mAuth.getCurrentUser().getUid();
        storageRef = FirebaseStorage.getInstance().getReference();
        Toast.makeText(getApplicationContext(), "Storing Data...", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.VISIBLE);

        final String email,password1,password2,username;
        email = this.email.getText().toString();
        password1 = this.password1.getText().toString();
        password2 = this.password2.getText().toString();
        username = this.username.getText().toString();
        closeKeyboard();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2) || TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(), "Please complete the form!!", Toast.LENGTH_LONG).show();
            return;
        }else if(!TextUtils.equals(password1,password2)){
            Toast.makeText(getApplicationContext(),"Passwords don't match!", Toast.LENGTH_LONG).show();
            return;
        }else if(TextUtils.getTrimmedLength(password1) < 6){
            Toast.makeText(getApplicationContext(),"Password length has to be at least 6!", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Map<String,Object> user = new HashMap<>();
                    user.put("username",username);
                    user.put("email",email);
                    user.put("createDate", new Timestamp(new Date()));
                    db = FirebaseFirestore.getInstance();
                    addUserToFirebase(user);
                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                    Log.d("status","succ");
                    progressBar.setVisibility(View.GONE);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addUserToFirebase(Map<String,Object> user){
        user_id = mAuth.getCurrentUser().getUid();
        db.collection("users").document(user_id).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document", e);
                    }
                });

        updatePic(user_id);
    }
    private void updatePic(String userId){
        StorageReference avatarRef = storageRef.child("/"+userId+"/avatar.jpg");
        profilePic.setDrawingCacheEnabled(true);
        profilePic.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

    }
    //private void pickMethod(){
       /* ArrayList<String> items = new ArrayList<>();
        items.add("Take a picture");
        items.add(("Choose from your device"));
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);
       if(listView.getVisibility() == View.VISIBLE){
           Log.d("status", "visible!!");
       }else {
           Log.d("status", "gone!!");
       }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                if(i == 0){
                    onLaunchCamera(view);
                }else{
                    onPickPhoto(view);
                }
            }
        });*/
    //}
    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    resizeImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPreview = (ImageView) findViewById(R.id.profilePic);
                ivPreview.setImageBitmap(takenImage);
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (data != null) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ivPreview = (ImageView) findViewById(R.id.profilePic);
            ivPreview.setImageBitmap(selectedImage);
        }
        photoMethodLayout.setVisibility(View.GONE);
    }
    public void onLaunchCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(RegisterActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
    private File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }
    public void resizeImage() throws IOException {
        Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
        Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 5);
        resizedBitmap = BitmapScaler.scaleToFitHeight(resizedBitmap,5);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File resizedFile = getPhotoFileUri(photoFileName + "_resized");
        resizedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(resizedFile);
        fos.write(bytes.toByteArray());
        fos.close();
    }
    private void initializeUI() {
        password1 = (EditText)findViewById(R.id.password1);
        password2 = (EditText)findViewById(R.id.password2);
        regBtn = (Button) findViewById(R.id.regBtn);
        email = (EditText)findViewById(R.id.email);
        username = (EditText)findViewById(R.id.username);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        profilePic = (ImageButton) findViewById(R.id.profilePic);
        //listView = (ListView) findViewById(R.id.listView);
        gobackBtn = (Button) findViewById(R.id.gobackBtn1);
        tkPicBtn = (Button) findViewById(R.id.tkPicBtn);
        chPicBtn = (Button) findViewById(R.id.chPicBtn);
        photoMethodLayout = (LinearLayout) findViewById(R.id.photoMethodLayout);
    }
    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
