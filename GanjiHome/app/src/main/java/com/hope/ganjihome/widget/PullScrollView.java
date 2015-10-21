package com.hope.ganjihome.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * 仿赶集网首页效果
 *
 * Created by Hope on 15/10/18.
 */
public class PullScrollView extends FrameLayout {

    private static final int DURATION = 200;

    private static final int SNAP_VELOCITY_DIP_PER_SECOND = 300;

    private int mDensityAdjustedSnapVelocity;

    /** 速度跟踪 */
    private VelocityTracker mVelocityTracker;

    private int mMaximumVelocity;

    private static final int STATE_OPEN = 1;
    private static final int STATE_CLOSE = 2;
    private static final int STATE_CLOSE_SLIDING = 3;
    private static final int STATE_CLOSE_CALLBACK = 6;
    private static final int STATE_OPEN_SLIDING = 4;
    private static final int STATE_OPEN_CALLBACK = 5;

    private ViewGroup mTopView;
    private ScrollView mContentView;

    private Scroller mScroller;

    private int mState = STATE_CLOSE;

    private int mTopViewHeight;

    private int mMatchSlidingHeigth;

    /**
     * 黑色遮罩
     */
    private View mShadowView;

    public boolean isAddshadow = true;

    public PullScrollView(Context context) {
        super(context);
    }

    public PullScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAddShadow(boolean isShadow) {
        this.isAddshadow = isShadow;

        if(isAddshadow) {
            addShadow();
        } else {
            if(mShadowView != null) {
                mTopView.removeView(mShadowView);
            }
        }
    }

    private void addShadow() {
        if(mTopView != null) {
            int viewcount = mTopView.getChildCount();

            mShadowView.setLayoutParams(mTopView.getLayoutParams());
            mShadowView.setBackgroundColor(Color.BLACK);
            int index = viewcount == 0 ? 0 : viewcount ;
            mTopView.addView(mShadowView,index);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();

        if(childCount != 2) {
            new RuntimeException("child number is faild");
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        mDensityAdjustedSnapVelocity = (int) (displayMetrics.density * SNAP_VELOCITY_DIP_PER_SECOND);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mShadowView = new View(getContext());

        mTopView = (ViewGroup) getChildAt(0);

        mContentView = (ScrollView) getChildAt(1);

        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mTopViewHeight == 0) {
            mTopViewHeight = mTopView.getHeight();

            mMatchSlidingHeigth = mTopViewHeight / 3;
        }
    }

    private float mLastY, mDownY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!mScroller.isFinished()) {
            return true;
        }
        mLastY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mLastY > mDownY && (mState == STATE_CLOSE || mState == STATE_OPEN_SLIDING) && mContentView.getScrollY() == 0) { //打开
                    mTouchDownY = mDownY;
                    return true;
                } else if(mLastY < mDownY && (mState == STATE_OPEN || mState == STATE_CLOSE_SLIDING) && mContentView.getScrollY() == 0){ //关闭
                    mTouchDownY = mDownY;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return false;
    }


    private float mTouchLastY, mTouchDownY;
    private int distanceY;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mScroller.isFinished()) {
            return false;
        }
        mTouchLastY = event.getY();

        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();

        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                distanceY  = (int) ((Math.abs((int) (mTouchLastY - mTouchDownY))) / 2 + 0.5);

                if(mTouchLastY > mTouchDownY) {

                    mState = STATE_OPEN_SLIDING;

                    showShadowView(distanceY);

                    mContentView.setPadding(0, distanceY, 0, 0);

                    int sliding = (int) (mMatchSlidingHeigth * ((float)distanceY / (float)mTopViewHeight));
                    mTopView.setPadding(0, sliding - mMatchSlidingHeigth, 0, 0);
                    return true;
                } else { //上滑
                    mState = STATE_CLOSE_SLIDING;

                    hideShadowView(distanceY);

                    mContentView.setPadding(0, mTopViewHeight - distanceY, 0, 0);

                    int sliding = (int) (mMatchSlidingHeigth * ((float)distanceY / (float)mTopViewHeight));
                    mTopView.setPadding(0, -sliding, 0, 0);
                    return true;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                distanceY  = (int) ((Math.abs((int) (mTouchLastY - mTouchDownY))) / 2 + 0.5);

                final VelocityTracker velocityTracker = mVelocityTracker;
                // 计算当前速度
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                // y方向的速度
                int velocity = (int) velocityTracker.getYVelocity();

                switch (mState) {
                    case STATE_CLOSE_SLIDING:
                        if(Math.abs(velocity) > mDensityAdjustedSnapVelocity || distanceY > mTopViewHeight / 2) { //速率达标 || 滑动距离超过一半
                            mScroller.startScroll(0 , 0, 0, mTopViewHeight - distanceY, DURATION);
                        } else { //不达标
                            mState = STATE_CLOSE_CALLBACK;
                            mScroller.startScroll(0 , 0, 0, distanceY, DURATION);
                        }
                        break;
                    case STATE_OPEN_SLIDING:
                        if(velocity > mDensityAdjustedSnapVelocity || distanceY > mTopViewHeight / 2) { //速率达标 || 滑动距离超过一半
                            mScroller.startScroll(0 , 0, 0, mTopViewHeight - distanceY, DURATION);
                        } else { //不达标
                            mState = STATE_OPEN_CALLBACK;
                            mScroller.startScroll(0, 0, 0, distanceY, DURATION);
                        }
                        break;
                }
                break;
        }
        return true;
    }

    public void expandTopView() {
        if(!mScroller.isFinished()) {
            return;
        }
        mState = STATE_OPEN_SLIDING;
        mScroller.startScroll(0 , 0, 0, mTopViewHeight, DURATION);
    }

    public void stretchTopView() {
        if(!mScroller.isFinished()) {
            return;
        }
        mState = STATE_CLOSE_SLIDING;
        mScroller.startScroll(0 , 0, 0, mTopViewHeight, DURATION);
    }


    public void showShadowView(int progress) {
        if(isAddshadow) {
            mShadowView.getBackground().setAlpha((int) (255 - 255 * (((float)progress / (float) mTopViewHeight))));
        }
    }

    public void hideShadowView(int progress) {
        if(isAddshadow) {
            mShadowView.getBackground().setAlpha((int) (255 * ((float) progress / (float) mTopViewHeight)));
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int sliding = 0;

            switch (mState) {
                case STATE_CLOSE_CALLBACK:
                    mContentView.setPadding(0, mTopViewHeight - (distanceY - mScroller.getCurrY()), 0, 0);

                    showShadowView(mTopViewHeight - (distanceY - mScroller.getCurrY()));

                    sliding = (int) (mMatchSlidingHeigth * ((float)(mTopViewHeight - (distanceY - mScroller.getCurrY())) / (float)mTopViewHeight));

                    mTopView.setPadding(0, sliding - mMatchSlidingHeigth, 0, 0);

                    if(mScroller.isFinished()) {
                        mState = STATE_OPEN;
                        distanceY = 0;
                    }
                    break;
                case STATE_OPEN_CALLBACK:
                    mContentView.setPadding(0, distanceY - mScroller.getCurrY(), 0, 0);

                    showShadowView(distanceY - mScroller.getCurrY());

                    sliding = (int) (mMatchSlidingHeigth * ((float)( distanceY - mScroller.getCurrY()) / (float)mTopViewHeight));

                    mTopView.setPadding(0, sliding - mMatchSlidingHeigth, 0, 0);

                    if(mScroller.isFinished()) {
                        mState = STATE_CLOSE;
                        distanceY = 0;
                    }
                    break;
                case STATE_CLOSE_SLIDING:
                    
                    mContentView.setPadding(0, mTopViewHeight - (distanceY + mScroller.getCurrY()), 0, 0);

                    showShadowView(mTopViewHeight - (distanceY + mScroller.getCurrY()));

                    sliding = (int) (mMatchSlidingHeigth * ((float)(mTopViewHeight - (distanceY + mScroller.getCurrY())) / (float)mTopViewHeight));

                    mTopView.setPadding(0, sliding - mMatchSlidingHeigth, 0, 0);

                    if(mScroller.isFinished()) {
                        mState = STATE_CLOSE;
                        distanceY = 0;
                    }
                    break;
                case STATE_OPEN_SLIDING:

                    mContentView.setPadding(0, distanceY + mScroller.getCurrY(), 0, 0);

                    showShadowView(distanceY + mScroller.getCurrY());

                    sliding = (int) (mMatchSlidingHeigth * ((float)(distanceY + mScroller.getCurrY()) / (float)mTopViewHeight));

                    mTopView.setPadding(0, sliding - mMatchSlidingHeigth, 0, 0);
                    if(mScroller.isFinished()) {
                        mState = STATE_OPEN;
                        distanceY = 0;
                    }
                    break;
            }
        }
        postInvalidate();
    }
}
