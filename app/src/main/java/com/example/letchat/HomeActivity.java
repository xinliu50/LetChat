package com.example.letchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.letchat.ui.AddFragment;
import com.example.letchat.ui.ChatFragment;
import com.example.letchat.ui.FriendFragment;
import com.example.letchat.ui.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.provider.MediaStore.*;

class BitmapScaler
{
    // scale and keep aspect ratio
    public static Bitmap scaleToFitWidth(Bitmap b, int width)
    {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }
    // scale and keep aspect ratio
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }
    // scale and keep aspect ratio
    public static Bitmap scaleToFill(Bitmap b, int width, int height)
    {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse),
                (int) (b.getHeight() * factorToUse), true);
    }
    // scale and don't keep aspect ratio
    public static Bitmap strechToFill(Bitmap b, int width, int height)
    {
        float factorH = height / (float) b.getHeight();
        float factorW = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW),
                (int) (b.getHeight() * factorH), true);
    }
}

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String user_id =  mAuth.getCurrentUser().getUid();
    private ActionBar toolbar;
    private BottomNavigationView bottomNavigationView;

    private ImageButton avatar, gobackBtn;
    private ListView list;
    private FloatingActionButton soBtn;
    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_chat:
                    selectedFragment = ChatFragment.newInstance();
                    break;
                case R.id.navigation_friend:
                    selectedFragment = FriendFragment.newInstance();
                    break;
                case R.id.navigation_addFriend:
                    selectedFragment = AddFragment.newInstance();
                    break;
                case R.id.navigation_home:
                    selectedFragment = ProfileFragment.newInstance();
                    break;
            }
            openFragment(selectedFragment);
            return true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initialUI();
       // loadUserInfo();
        /*try {
            avatar.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    pickMethod();
                }
            });
        }catch (Throwable e){
            Log.d("status",e.getMessage());
        }
        soBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "Signing Out User!!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

    }
    /*private void loadUserInfo(){
        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference islandRef = storageRef.child("/"+user_id+"/avatar.jpg");

        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(Bitmap.createScaledBitmap(bmp, avatar.getWidth(),
                        avatar.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

    }
    private void updatePic(){
        StorageReference avatarRef = storageRef.child("/"+user_id+"/avatar.jpg");
        avatar.setDrawingCacheEnabled(true);
        avatar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) avatar.getDrawable()).getBitmap();
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
    private void pickMethod(){
        ArrayList<String> items = new ArrayList<>();
        items.add("Take a picture");
        items.add(("Choose from your device"));
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);
        list.setAdapter(adapter);
        list.setVisibility(View.VISIBLE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                if(i == 0){
                    onLaunchCamera(view);
                }else{
                    onPickPhoto(view);
                }
            }
        });
    }
// Trigger gallery selection for a photo
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
                ImageView ivPreview = (ImageView) findViewById(R.id.avatar);
                ivPreview.setImageBitmap(takenImage);
                updatePic();
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (data != null) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = null;
            try {
                selectedImage = Images.Media.getBitmap(this.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageView ivPreview = (ImageView) findViewById(R.id.avatar);
            ivPreview.setImageBitmap(selectedImage);
            updatePic();
        }
        list.setVisibility(View.GONE);
    }
    public void onLaunchCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(HomeActivity.this, "com.codepath.fileprovider", photoFile);
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
   }*/
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
   private void initialUI(){
       avatar = (ImageButton) findViewById(R.id.avatar);
       gobackBtn = (ImageButton)findViewById(R.id.gobackBtn);
       list = (ListView) findViewById(R.id.listView);
       soBtn = (FloatingActionButton) findViewById(R.id.soBtn);
       toolbar = getSupportActionBar();
       bottomNavigationView = findViewById(R.id.bottomNavigationView);


       bottomNavigationView.setOnNavigationItemSelectedListener(
               new BottomNavigationView.OnNavigationItemSelectedListener() {
                   @Override
                   public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                       switch (item.getItemId()) {
                           case R.id.navigation_chat:
                               toolbar.setTitle("Chat");
                               Fragment chatFragment = ChatFragment.newInstance();
                               openFragment(chatFragment);
                               return true;
                           case R.id.navigation_friend:
                               toolbar.setTitle("Friends");
                               Fragment friendFragment = FriendFragment.newInstance();
                               openFragment(friendFragment);
                               return true;
                           case R.id.navigation_addFriend:
                               toolbar.setTitle("Add Friends");
                               Fragment homeFragment = AddFragment.newInstance();
                               openFragment(homeFragment);
                               return true;
                           case R.id.navigation_home:
                               toolbar.setTitle("Profile");
                               Fragment profileFragment = ProfileFragment.newInstance();
                               openFragment(profileFragment);
                               return true;

                       }
                       return false;
                   }
               });
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
