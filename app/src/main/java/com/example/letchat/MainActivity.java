package com.example.letchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.ProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText username, password;
    private Button regBtn;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        initializeUI();
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }
    private void initializeUI() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        regBtn = findViewById(R.id.regBtn);
    }
    private void registerNewUser(){
        progressBar.setVisibility(View.VISIBLE);
        String username, password;
        username = this.username.getText().toString();
        password = this.password.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(),"Please enter your email!", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please enter yor password!", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(username,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Registration successful!",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }else{
                            Toast.makeText(getApplicationContext(),"Registration failed! Please try again!",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
