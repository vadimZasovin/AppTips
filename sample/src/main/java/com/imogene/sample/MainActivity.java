package com.imogene.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import com.imogene.apptips.AppTips;
import com.imogene.apptips.Tip;

/**
 * Created by Admin on 25.04.2016.
 */
public class MainActivity extends AppCompatActivity {

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
        return appTips.newTip(targetView, "Gendolf Gray");
    }

    private Tip createSecondTip(){
        Tip tip = appTips.newTip(R.id.text2, "Aragorn Great");
        tip.setHighlightingEnabled(false);
        tip.setPointerAnimationEnabled(false);
        return tip;
    }

    private Tip createThirdTip(){
        Tip tip = appTips.newTip(R.id.text1, "Legolas Elf");
        tip.setColor(Color.GREEN);
        tip.setTextColor(Color.DKGRAY);
        return tip;
    }

    private Tip createFourthTip(){
        Tip tip = appTips.newTip(400, 500, "Bilbo Beggins");
        tip.setColor(getResources().getColor(R.color.colorWhiteTranslucent));
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

    private void testTipForDialog(){
        DialogFragment dialog = TestDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), "TEST_DIALOG");
    }
}
