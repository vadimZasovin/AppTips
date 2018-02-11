package com.imogene.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

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
        testAdvancedTips();
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
        tip.setPointerAnimationEnabled(false);
        tip.setPointerOffset(48);
        return tip;
    }

    private Tip createThirdTip(){
        Tip tip = appTips.newTip(R.id.text1, "Legolas Elf");
        tip.setAlign(Tip.ALIGN_LEFT);
        tip.setMaxWidth(150);
        tip.setMinWidth(100);
        tip.setColor(Color.GREEN);
        tip.setTextColor(Color.DKGRAY);
        return tip;
    }

    private Tip createFourthTip(){
        Tip tip = appTips.newTip(400, 500, "Bilbo Beggins");
        tip.setColor(getResources().getColor(R.color.colorWhiteTranslucent));
        tip.setAlign(Tip.ALIGN_CENTER_ABOVE);
        tip.setHighlightingEnabled(true);
        tip.setPointerPosition(0.7F);
        tip.setGravity(Gravity.START);
        return tip;
    }

    private Tip createOneMoreTip(){
        Tip tip = appTips.newTip(R.id.imageButton1, "Smeagol");
        tip.setAlign(Tip.ALIGN_CENTER_BELOW);
        return tip;
    }

    private void testAdvancedTips(){
        if(appTips == null){
            appTips = new AppTips(this);
            Tip tip1 = createFirstTip();
            Tip tip2 = createSecondTip();
            Tip tip3 = createThirdTip();
            appTips.addTips(true, tip1, tip2, tip3);
            appTips.addTip(createFirstTip());
            appTips.addTip(createSecondTip());
            appTips.addTip(createThirdTip());
            appTips.addTip(createFourthTip());
            appTips.addTip(createOneMoreTip());
        }
        appTips.show();
    }
}
