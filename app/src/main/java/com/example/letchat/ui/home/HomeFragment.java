package com.example.letchat.ui.home;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.letchat.BitmapScaler;
import com.example.letchat.HomeActivity;
import com.example.letchat.MainActivity;
import com.example.letchat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.EXTRA_OUTPUT;
//import static com.example.letchat.HomeActivity.PICK_PHOTO_CODE;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private HomeViewModel mViewModel;
    private LinearLayout home_photoMethodLayout;
    private ImageButton avatar, soBtn;
    private Button home_tkPicBtn, home_chPicBtn;
    private View root;
    private StorageReference storageRef;
    private String user_id;
    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;
    private Activity activity;


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.home_fragment, container, false);
        initialUI();
        loadUserInfo();
        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent event) {
                        home_photoMethodLayout.setVisibility(View.GONE);
                        Log.d("TAP!!", "onSingleTapConfirmed: " + event.toString());
                        return true;
                    }
                });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
    }
    private void initialUI(){
        avatar = (ImageButton) root.findViewById(R.id.avatar);
        soBtn = (ImageButton) root.findViewById(R.id.soBtn);
        activity = HomeFragment.this.getActivity();
        home_tkPicBtn = (Button) root.findViewById(R.id.home_tkPicBtn);
        home_chPicBtn = (Button) root.findViewById(R.id.home_chPicBtn);
        home_photoMethodLayout = (LinearLayout) root.findViewById(R.id.home_photoMethodLayout);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        user_id =  mAuth.getCurrentUser().getUid();

        soBtn.setOnClickListener(this);
        avatar.setOnClickListener(this);
        home_tkPicBtn.setOnClickListener(this);
        home_chPicBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.soBtn:
                mAuth.signOut();
                Toast.makeText(getActivity(), "Signing Out User!!", Toast.LENGTH_LONG).show();
                activity.finish();
                break;
            case R.id.avatar:
                home_photoMethodLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.home_chPicBtn:
                onPickPhoto(v);
                break;
            case R.id.home_tkPicBtn:
                onLaunchCamera(v);
                break;
        }
    }
    private void loadUserInfo(){
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
                Log.d("status", "erroe!loadUserInfo()");
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
                Log.d("status", "fail!updatePic()");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("status", "updatePic()");
            }
        });

    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
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
                ImageView ivPreview = (ImageView) root.findViewById(R.id.avatar);
                ivPreview.setImageBitmap(takenImage);
                updatePic();
            } else {
                Toast.makeText(activity, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (data != null) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageView ivPreview = (ImageView) root.findViewById(R.id.avatar);
            ivPreview.setImageBitmap(selectedImage);
            updatePic();
        }
        home_photoMethodLayout.setVisibility(View.GONE);
    }
    public void onLaunchCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(activity, "com.codepath.fileprovider", photoFile);
        intent.putExtra(EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d("status", "failed to create directory");
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
}
