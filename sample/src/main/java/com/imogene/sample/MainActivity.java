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
                            .defaultOptions(TipOptions
                                    .create(MainActivity.this)
                                    .verticalMargin(16)
                                    .align(TipOptions.ALIGN_CENTER_BELOW))
                            .tip(R.id.text, "Marambra")
                            .tip(TipOptions.create(MainActivity.this)
                                    .align(TipOptions.ALIGN_CENTER_ABOVE)
                                    .horizontalMargin(12)
                                    .verticalMargin(12)
                                    .text("Birdman")
                                    .target(R.id.text2))
                            .tip(TipOptions.create(MainActivity.this)
                                    .align(TipOptions.ALIGN_LEFT_BELOW)
                                    .maxWidth(200)
                                    .minWidth(100)
                                    .horizontalMargin(12)
                                    .verticalMargin(12)
                                    .color(getResources().getColor(android.R.color.holo_green_dark))
                                    .textColor(getResources().getColor(android.R.color.darker_gray))
                                    .target(R.id.text1)
                                    .text("Beautiful"))
                            .dimAmount(0.3f)
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
}
