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
                testAdvancedTips();
            }
        });
    }

    private void testSimpleTips(){
        if(appTips == null){
            appTips = new AppTips(this);
            appTips.addTip(createFirstTip());
            appTips.addTip(createSecondTip());
            appTips.addTip(createThirdTip());
        }
        appTips.show();
    }

    private Tip createFirstTip(){
        View targetView = findViewById(R.id.text);
        Tip tip = appTips.newTip(targetView, "Gendolf Gray");
        tip.setVerticalOffset(16);
        tip.setAlign(Tip.ALIGN_CENTER_BELOW);
        return tip;
    }

    private Tip createSecondTip(){
        Tip tip = appTips.newTip(R.id.text2, "Aragorn Great");
        tip.setHorizontalOffset(12);
        tip.setVerticalOffset(12);
        tip.setHighlightingEnabled(false);
        return tip;
    }

    private Tip createThirdTip(){
        Tip tip = appTips.newTip(R.id.text1, "Legolas Elf");
        tip.setMaxWidth(200);
        tip.setMinWidth(100);
        tip.setHorizontalOffset(12);
        tip.setVerticalOffset(12);
        tip.setColor(Color.GREEN);
        tip.setTextColor(Color.DKGRAY);
        return tip;
    }

    private void testAdvancedTips(){
        if(appTips == null){
            appTips = new AppTips(this);
            Tip tip1 = createFirstTip();
            Tip tip2 = createSecondTip();
            Tip tip3 = createThirdTip();
            appTips.addTips(true, tip1, tip2, tip3);
        }
        appTips.show();
    }
}
