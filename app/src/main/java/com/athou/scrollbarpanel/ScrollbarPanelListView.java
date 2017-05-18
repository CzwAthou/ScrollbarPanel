package com.athou.scrollbarpanel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by athou on 2017/5/18.
 */

public class ScrollbarPanelListView extends ListView implements AbsListView.OnScrollListener {

    private View mScrollBarPanel = null;
    //定义滑动条的Y坐标位置 --> onscroll里面不断判断和赋值
    private int mScrollBarPanelPosition = 0;
    //定义指示器在listview中Y轴高度
    private int thumbOffset = 0;
    //记录上一次滑动的位置
    private int mLastPosition = -1;

    private Animation mInAnimation = null;
    private Animation mOutAnimation = null;

    OnScrollListener mOnScrollListener = null;
    OnPositionChangedListener mPositionChangedListener = null;

    public ScrollbarPanelListView(Context context) {
        this(context, null);
    }

    public ScrollbarPanelListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public ScrollbarPanelListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnScrollListener(this);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollbarPanelListView);
        final int scrollBarPanelLayoutId = a.getResourceId(R.styleable.ScrollbarPanelListView_scrollBarPanel, -1);
        final int scrollBarPanelInAnimation = a.getResourceId(R.styleable.ScrollbarPanelListView_scrollBarPanelInAnimation, R.anim.animal_in);
        final int scrollBarPanelOutAnimation = a.getResourceId(R.styleable.ScrollbarPanelListView_scrollBarPanelOutAnimation, R.anim.animal_out);
        a.recycle();

        if (scrollBarPanelLayoutId != -1) {
            setScrollBarPanel(scrollBarPanelLayoutId);
        }

        if (scrollBarPanelInAnimation > 0) {
            mInAnimation = AnimationUtils.loadAnimation(getContext(), scrollBarPanelInAnimation);
        }

        if (scrollBarPanelOutAnimation > 0) {
            int scrollBarPanelFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            mOutAnimation = AnimationUtils.loadAnimation(getContext(), scrollBarPanelOutAnimation);
            mOutAnimation.setDuration(scrollBarPanelFadeDuration);

            mOutAnimation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mScrollBarPanel != null) {
                        mScrollBarPanel.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public void setScrollBarPanel(int panelId) {
        //渲染自己的气泡布局进来
        setScrollBarPanel(LayoutInflater.from(getContext()).inflate(panelId, this, false));
    }

    public void setScrollBarPanel(View scrollBarPanel) {
        mScrollBarPanel = scrollBarPanel;
        mScrollBarPanel.setVisibility(View.GONE);
//        requestLayout();
    }

    public View getScrollBarPanel() {
        return mScrollBarPanel;
    }

    public void setOnPositionChangedListener(OnPositionChangedListener onPositionChangedListener) {
        mPositionChangedListener = onPositionChangedListener;
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量气泡控件
        if (mScrollBarPanel != null && getAdapter() != null) {
            measureChild(mScrollBarPanel, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //摆放自己的气泡控件
        if (mScrollBarPanel != null && getAdapter() != null) {
            int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
            mScrollBarPanel.layout(
                    left,
                    mScrollBarPanelPosition,
                    left + mScrollBarPanel.getMeasuredWidth(),
                    mScrollBarPanelPosition + mScrollBarPanel.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //在viewgroup绘制的时候，在上面添加一个自己绘制的气泡布局在上面
        if (mScrollBarPanel != null && mScrollBarPanel.getVisibility() == View.VISIBLE) {
            drawChild(canvas, mScrollBarPanel, getDrawingTime());
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //不断的控制气泡View重新摆放位置
        if (mScrollBarPanel != null && getAdapter() != null) {
            //不断计算mScrollBarPanelPosition值
            //computeVerticalScrollExtent(); //滑块在纵向滑动范围内经过放大后自身的高度
            //computeVerticalScrollOffset(); //滑块纵向幅度的位置 (滑到正中间的话，range刚好是5000)
            //computeVerticalScrollRange(); //滑动的范围 0~10000
            //得到系统滑块的中心点Y坐标

            //1，滑块的高度   滑块的高度/ListView的高度 = extent/range
            int height = Math.round(computeVerticalScrollExtent() * 1.0f / computeVerticalScrollRange() * getMeasuredHeight());
            //2, 得到滑块中间坐标的Y坐标  滑块的高度/extent = thunboffset/offset
            thumbOffset = Math.round(height * 1.0f / computeVerticalScrollExtent() * computeVerticalScrollOffset());
            thumbOffset += height / 2;
            mScrollBarPanelPosition = thumbOffset - mScrollBarPanel.getMeasuredHeight() / 2;


            int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
            mScrollBarPanel.layout(
                    left,
                    mScrollBarPanelPosition,
                    left + mScrollBarPanel.getMeasuredWidth(),
                    mScrollBarPanelPosition + mScrollBarPanel.getMeasuredHeight());

            //找到mScrollBarPanel中心点位置对于的item
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View childView = getChildAt(i);
                if (childView != null && thumbOffset > childView.getTop() && thumbOffset < childView.getBottom()) {
                    Log.i("onScroll", "mLastPosition:" + mLastPosition + " firstVisibleItem + i:" + (firstVisibleItem + i));
                    if (mLastPosition != firstVisibleItem + i) { //优化代码，使结果只回调一次
                        Log.i("onScroll", "onPositionChanged");
                        mLastPosition = firstVisibleItem + i;
                        if (mPositionChangedListener != null) {
                            mPositionChangedListener.onPositionChanged(this, mLastPosition, mScrollBarPanel);
                        }
                    }
                    break;
                }
            }
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        //唤醒scrollbar的回调
        //判断系统的滑块是否唤醒--true:显示自己的气泡
        boolean awaken = super.awakenScrollBars(startDelay, invalidate);
        if (awaken && mScrollBarPanel != null) {
            if (mScrollBarPanel.getVisibility() == View.GONE) {
                mScrollBarPanel.setVisibility(View.VISIBLE);
                //播放In动画
                if (mInAnimation != null) {
                    mScrollBarPanel.startAnimation(mInAnimation);
                }
            }
            mHandler.removeCallbacks(mScrollBarFadeRunnable);
            //过599ms，影藏掉mScrollBarPanel
            mHandler.postAtTime(mScrollBarFadeRunnable, AnimationUtils.currentAnimationTimeMillis() + startDelay);
        }
        return awaken;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mScrollBarFadeRunnable);
    }

    private Handler mHandler = new Handler();

    private final Runnable mScrollBarFadeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOutAnimation != null) {
                mScrollBarPanel.startAnimation(mOutAnimation);
            }
        }
    };

    public interface OnPositionChangedListener {
        void onPositionChanged(ScrollbarPanelListView listView, int position, View scrollBarPanel);
    }
}
