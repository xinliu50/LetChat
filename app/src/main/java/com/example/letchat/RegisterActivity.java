package com.example.letchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText password1, password2, username, email;
    private Button regBtn;
    private  FirebaseAuth mAuth;
    private ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "DocSnippets";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        initializeUI();
        regBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }
    private void registerNewUser(){
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
                    addUserToFirebase(user);
                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addUserToFirebase(Map<String,Object> user){
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,"DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
    private void initializeUI() {
        password1 = findViewById(R.id.password1);
        password2 = findViewById(R.id.password2);
        regBtn = findViewById(R.id.regBtn);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        progressBar = findViewById(R.id.progressBar2);
    }
    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
}
