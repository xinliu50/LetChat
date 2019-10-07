package com.example.letchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private ImageButton avatar;
    private FloatingActionButton addBtn;
    private ListView list;
    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initialUI();

        avatar.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View arg0) {
                //onLaunchCamera(arg0);
                //onPickPhoto(arg0);
                pickMethod();
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

    }
// Trigger gallery selection for a photo
        public void onPickPhoto(View view) {
            // Create intent for picking a photo from the gallery
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Bring up gallery to select a photo
                startActivityForResult(intent, PICK_PHOTO_CODE);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (data != null) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = Images.Media.getBitmap(this.getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Load the selected image into a preview
                ImageView ivPreview = (ImageView) findViewById(R.id.avatar);
                ivPreview.setImageBitmap(selectedImage);
            }
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

   /*@Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
           } else {
               Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
           }
       }
   }*/
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
   private void initialUI(){
       avatar = findViewById(R.id.avatar);
       addBtn = findViewById(R.id.addBtn);
       list = findViewById(R.id.list);
    }
}
