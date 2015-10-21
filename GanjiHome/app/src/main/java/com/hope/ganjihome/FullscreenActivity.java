package com.hope.ganjihome;

import android.app.Activity;
import android.os.Bundle;

import com.hope.ganjihome.widget.PullScrollView;


public class FullscreenActivity extends Activity {

    private PullScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        scrollView = (PullScrollView) findViewById(R.id.pull_scrollView);

        scrollView.setAddShadow(true);
    }

    private boolean isFrist = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if (isFrist) {

            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.expandTopView();
                }
            }, 500);

            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.stretchTopView();
                }
            }, 1500);
            isFrist = true;
        }
    }

}
