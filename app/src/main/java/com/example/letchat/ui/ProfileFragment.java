package com.example.letchat.ui;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.letchat.MainActivity;
import com.example.letchat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class ProfileFragment extends Fragment{

    private FirebaseAuth mAuth;
    private View view;
    private ProfileViewModel mViewModel;
    private ImageButton avatar, gobackBtn;
    private ListView list;
    private FloatingActionButton soBtn;
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_fragment, container, false);
       // initialUI();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

    }
    private void initialUI() {
        avatar = (ImageButton) view.findViewById(R.id.avatar);
        gobackBtn = (ImageButton) view.findViewById(R.id.gobackBtn);
        list = (ListView) view.findViewById(R.id.listView);
        soBtn = (FloatingActionButton) view.findViewById(R.id.soBtn);
       // avatar.setOnClickListener(this);
       // gobackBtn.setOnClickListener(this);
       // list.setOnClickListener(this);
        //soBtn.setOnClickListener(this);
        //mAuth = FirebaseAuth.getInstance();
    }

   /* @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.soBtn:
                mAuth.signOut();
                Toast.makeText(getActivity(), "Signing Out User!!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ProfileFragment.this.getActivity(), MainActivity.class);
                ProfileFragment.this.getActivity().startActivity(intent);
                break;
        }
    }*/
}
