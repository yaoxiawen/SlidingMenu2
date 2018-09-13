package com.yxw.slidingmenu2;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class MyLinearLayout extends LinearLayout {
    private DragLayout mDragLayout;

    public void setDraglayout(DragLayout mDragLayout) {
        this.mDragLayout = mDragLayout;
    }

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 如果当前是关闭状态, 按之前方法判断
        if (mDragLayout.getStatus() == DragLayout.Status.Close) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果当前是关闭状态, 按之前方法处理
        if (mDragLayout.getStatus() == DragLayout.Status.Close) {
            return super.onTouchEvent(event);
        } else {
            // 手指抬起, 执行关闭操作
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mDragLayout.close();
            }
            return true;
        }
    }
}
