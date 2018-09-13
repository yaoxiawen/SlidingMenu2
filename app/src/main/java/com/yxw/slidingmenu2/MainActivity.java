package com.yxw.slidingmenu2;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        final ListView mLeftList = findViewById(R.id.lv_left);
        final ListView mMainList = findViewById(R.id.lv_main);
        final ImageView mHeaderImage = findViewById(R.id.iv_header);
        MyLinearLayout mLinearLayout = findViewById(R.id.mll);
        DragLayout mDragLayout = findViewById(R.id.dl);
        mLinearLayout.setDraglayout(mDragLayout);
        mLeftList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = ((TextView) view);
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });

        mMainList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES));
        //设置状态监听
        mDragLayout.setDragStatusListener(new DragLayout.OnDragStatusChangeListener() {
            @Override
            public void onClose() {
                Utils.showToast(MainActivity.this, "onClose");
                // 让图标晃动
                ObjectAnimator mAnim = ObjectAnimator.ofFloat(mHeaderImage, "translationX", 15.0f);
                mAnim.setInterpolator(new CycleInterpolator(4));
                mAnim.setDuration(500);
                mAnim.start();

            }

            @Override
            public void onOpen() {
                Utils.showToast(MainActivity.this, "onOpen");
                // 左面板ListView随机设置一个条目
                Random random = new Random();

                int nextInt = random.nextInt(50);
                mLeftList.smoothScrollToPosition(nextInt);
            }

            @Override
            public void onDraging(float percent) {
                // 更新图标的透明度
                mHeaderImage.setAlpha(1 - percent);
            }
        });
    }
}
