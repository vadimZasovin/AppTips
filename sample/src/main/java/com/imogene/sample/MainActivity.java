package com.imogene.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.imogene.apptips.AppTips;
import com.imogene.apptips.TipOptions;

/**
 * Created by Admin on 25.04.2016.
 */
public class MainActivity extends Activity {

    private AppTips mAppTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAppTips == null){
                    mAppTips = new AppTips.Builder(MainActivity.this)
                            .setDefaultOptions(TipOptions
                                    .create(MainActivity.this)
                                    .setVerticalMargin(16)
                                    .setAlign(TipOptions.ALIGN_CENTER_BELOW))
                            .addTip(R.id.text, "Marambra")
                            .addTip(TipOptions.create(MainActivity.this)
                                    .setAlign(TipOptions.ALIGN_CENTER_ABOVE)
                                    .setHorizontalMargin(12)
                                    .setVerticalMargin(12)
                                    .setText("Birdman")
                                    .setTarget(R.id.text2))
                            .addTip(TipOptions.create(MainActivity.this)
                                    .setAlign(TipOptions.ALIGN_LEFT)
                                    .setMaxWidth(200)
                                    .setMinWidth(100)
                                    .setHorizontalMargin(12)
                                    .setVerticalMargin(12)
                                    .setColor(getResources().getColor(android.R.color.holo_green_dark))
                                    .setTextColor(getResources().getColor(android.R.color.darker_gray))
                                    .setTarget(R.id.text1)
                                    .setText("Beautiful"))
                            .setDimAmount(0.3f)
                            .show();
                }else {
                    mAppTips.show();
                }

            }
        });

        findViewById(R.id.text1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppTips.close();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
