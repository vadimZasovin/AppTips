package com.imogene.sample;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.imogene.apptips.AppTips;
import com.imogene.apptips.Tip;

/**
 * Created by Admin on 25.04.2016.
 */
public class MainActivity extends Activity {

    private AppTips appTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSimpleTips();
            }
        });
    }

    private void testSimpleTips(){
        if(appTips == null){
            appTips = new AppTips(this);

            Tip tip1 = appTips.newTip(R.id.text, "Gendolf Gray");
            tip1.setVerticalOffset(16);
            tip1.setAlign(Tip.ALIGN_CENTER_BELOW);
            appTips.addTip(tip1);

            appTips.addTips(true);

            Tip tip2 = appTips.newTip(R.id.text2, "Aragorn Great");
            tip2.setHorizontalOffset(12);
            tip2.setVerticalOffset(12);
            tip2.setHighlightingEnabled(false);
            appTips.addTip(tip2);

            Tip tip3 = appTips.newTip(R.id.text1, "Legolas Elf");
            tip3.setMaxWidth(200);
            tip3.setMinWidth(100);
            tip3.setHorizontalOffset(12);
            tip3.setVerticalOffset(12);
            tip3.setColor(Color.GREEN);
            tip3.setTextColor(Color.DKGRAY);
            appTips.addTip(tip3);
        }

        appTips.show();
    }

    private void testViewGroup(){
        FrameLayout container = new FrameLayout(this);
        int windowType = WindowManager.LayoutParams.TYPE_APPLICATION;
        WindowManager.LayoutParams containerLayoutParams = new WindowManager.LayoutParams(windowType);
        containerLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        containerLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        containerLayoutParams.format = PixelFormat.TRANSLUCENT;
        containerLayoutParams.windowAnimations = android.R.style.Animation_Dialog;
        containerLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        containerLayoutParams.dimAmount = 0.3F;

        final int width = FrameLayout.LayoutParams.WRAP_CONTENT;
        final int height = FrameLayout.LayoutParams.WRAP_CONTENT;

        TextView textView1 = new TextView(this);
        textView1.setText("Text 1");
        textView1.setBackgroundColor(Color.WHITE);
        int view1Gravity = Gravity.START | Gravity.BOTTOM;
        FrameLayout.LayoutParams view1LayoutParams = new FrameLayout.LayoutParams(width, height, view1Gravity);

        TextView textView2 = new TextView(this);
        textView2.setText("Text 2");
        textView2.setBackgroundColor(Color.RED);
        int view2Gravity = Gravity.END | Gravity.BOTTOM;
        FrameLayout.LayoutParams view2LayoutParams = new FrameLayout.LayoutParams(width, height, view2Gravity);

        container.addView(textView1, view1LayoutParams);
        container.addView(textView2, view2LayoutParams);
        WindowManager windowManager = getWindowManager();
        windowManager.addView(container, containerLayoutParams);
    }
}
