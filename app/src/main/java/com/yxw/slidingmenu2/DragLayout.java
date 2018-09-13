package com.yxw.slidingmenu2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 侧滑面板
 */
public class DragLayout extends FrameLayout {
    private ViewDragHelper mDragHelper;
    private ViewGroup mLeftContent; //侧边页面
    private ViewGroup mMainContent; //主页面
    private int mHeight;
    private int mWidth;
    private int mRange; //拖拽的最大范围
    private Status mStatus = Status.Close;
    private OnDragStatusChangeListener mListener;

    public DragLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mDragHelper = ViewDragHelper.create(this, mCallback);
    }

    /**
     * 回调
     */
    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        /**
         * 根据返回结果决定当前child是否可以拖拽
         * @param child 当前被拖拽的View
         * @param pointerId 区分多点触摸的id
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        /**
         * 返回拖拽的范围, 不对拖拽进行真正的限制. 仅仅决定了动画执行速度
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /**
         * 根据建议值 修正将要移动到的(横向)位置
         * 此时没有发生真正的移动
         * left = oldLeft + dx;
         * @param child 当前拖拽的View
         * @param left 新的位置的建议值
         * @param dx 位置变化量
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //修正left，保证在0-mRange
            left = left > 0 ? left : 0;
            left = left > mRange ? mRange : left;
            return left;
        }

        /**
         * 当View位置改变的时候, 处理要做的事情 (更新状态, 伴随动画, 重绘界面)
         * 此时,View已经发生了位置的改变
         * @param changedView 改变位置的View
         * @param left 新的左边值
         * @param top
         * @param dx 水平方向变化量
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            int newLeft = left;
            //如果触摸是主面板，直接就是newLeft
            if (changedView == mLeftContent) {
                // 把当前变化量传递给mMainContent
                newLeft = mMainContent.getLeft() + dx;
            }
            //修正left，保证在0-mRange
            newLeft = newLeft > 0 ? newLeft : 0;
            newLeft = newLeft > mRange ? mRange : newLeft;
            //如果是主面板，已经在clampViewPositionHorizontal方法中对主面板进行位置移动
            if (changedView == mLeftContent) {
                // 当左面板移动之后, 再强制放回去.
                mLeftContent.layout(0, 0, 0 + mWidth, 0 + mHeight);
                mMainContent.layout(newLeft, 0, newLeft + mWidth, 0 + mHeight);
            }
            // 更新状态,执行动画
            dispatchDragEvent(newLeft);
            // 为了兼容低版本, 每次修改值之后, 进行重绘
            invalidate();
        }

        /**
         * 当View被释放的时候, 处理的事情(执行动画)
         * @param releasedChild 被释放的子View
         * @param xvel 水平方向的速度, 向右为+
         * @param yvel 竖直方向的速度, 向下为+
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            // 判断执行 关闭/开启
            // 先考虑所有开启的情况,剩下的就都是关闭的情况
            if (xvel == 0 && mMainContent.getLeft() > mRange / 2.0f) {
                open();
            } else if (xvel > 0) {
                open();
            } else {
                close();
            }
        }
    };

    protected void dispatchDragEvent(int newLeft) {
        float percent = newLeft * 1.0f / mRange;
        if(mListener != null){
            mListener.onDraging(percent);
        }
        // 更新状态, 执行回调
        Status preStatus = mStatus;
        mStatus = updateStatus(percent);
        if (mStatus != preStatus) {
            // 状态发生变化
            if (mStatus == Status.Close) {
                // 当前变为关闭状态
                if (mListener != null) {
                    mListener.onClose();
                }
            } else if (mStatus == Status.Open) {
                if (mListener != null) {
                    mListener.onOpen();
                }
            }
        }
        //伴随动画
        animViews(percent);
    }

    private Status updateStatus(float percent) {
        if (percent == 0f) {
            return Status.Close;
        } else if (percent == 1.0f) {
            return Status.Open;
        }
        return Status.Draging;
    }

    /**
     * 状态枚举
     */
    public enum Status {
        Close, Open, Draging;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    /**
     * 状态改变接口，向外暴露
     */
    public interface OnDragStatusChangeListener {
        void onClose();

        void onOpen();

        void onDraging(float percent);
    }

    /**
     * 设置状态监听，传入状态改变接口
     *
     * @param mListener
     */
    public void setDragStatusListener(OnDragStatusChangeListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 伴随动画
     *
     * @param percent 百分比
     */
    private void animViews(float percent) {
        //左面板: 缩放动画, 平移动画, 透明度动画
        // 缩放动画
        mLeftContent.setScaleX(0.5f + 0.5f * percent);
        mLeftContent.setScaleY(0.5f + 0.5f * percent);
        mLeftContent.setTranslationX(-mWidth / 2.0f + mWidth / 2.0f * percent);
        mLeftContent.setAlpha(0.5f + 0.5f * percent);
        //主面板: 缩放动画
        mMainContent.setScaleX(1.0f - 0.2f * percent);
        mMainContent.setScaleY(1.0f - 0.2f * percent);
        //背景动画: 亮度变化 (颜色变化)
        //参1，颜色，参2，颜色填充的模式
        getBackground().setColorFilter((Integer) evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    /**
     * 颜色变化过度
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * 将触摸事件传递给mDragHelper处理
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 获取子view的引用
     * 该方法当1级的子view全部加载完时会调用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 容错性检查 (至少有俩子View, 子View必须是ViewGroup的子类)
        if (getChildCount() < 2) {
            throw new IllegalStateException("布局至少有俩孩子. Your ViewGroup must has 2 children at least.");
        }
        if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("子View必须是ViewGroup的子类. Your children must be an instance of ViewGroup");
        }
        //获取子view的引用
        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);
    }

    /**
     * 当尺寸有变化的时候调用
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        mRange = (int) (mWidth * 0.6f);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //持续平滑动画 (高频率调用)
        if (mDragHelper.continueSettling(true)) {
            //如果返回true, 动画还需要继续执行
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 关闭
     */
    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            //触发一个平滑动画
            if (mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)) {
                //返回true代表还没有移动到指定位置, 需要刷新界面.
                //参数传this(child所在的ViewGroup)
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
        }
    }

    /**
     * 开启
     */
    public void open() {
        open(true);
    }


    public void open(boolean isSmooth) {
        int finalLeft = mRange;
        if (isSmooth) {
            //触发一个平滑动画
            if (mDragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)) {
                //返回true代表还没有移动到指定位置, 需要刷新界面.
                //参数传this(child所在的ViewGroup)
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
        }
    }
}
