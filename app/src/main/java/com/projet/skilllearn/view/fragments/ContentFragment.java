package com.projet.skilllearn.view.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.projet.skilllearn.R;

public class ContentFragment extends Fragment {
    private static ContentFragment instance;
    private TextView tvContent;
    private String content;

    public static ContentFragment getInstance() {
        if (instance == null) {
            instance = new ContentFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvContent = view.findViewById(R.id.tv_content);

        if (content != null) {
            updateContent(content);
        }
    }

    public void updateContent(String content) {
        this.content = content;
        if (tvContent != null && content != null) {
            tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        }
    }
}