package com.hope.ganjihome;

import android.app.Activity;
import android.os.Bundle;

import com.hope.ganjihome.widget.PullScrollView;


public class FullscreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        PullScrollView scrollView = (PullScrollView) findViewById(R.id.pull_scrollView);

        scrollView.setAddShadow(true);
    }

}
