package com.example.letchat.ui;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.letchat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileFragment extends Fragment {

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
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }
    private void initialUI() {
        avatar = (ImageButton) findViewById(R.id.avatar);
        gobackBtn = (ImageButton) findViewById(R.id.gobackBtn);
        list = (ListView) findViewById(R.id.listView);
        soBtn = (FloatingActionButton) findViewById(R.id.soBtn);
    }

}
