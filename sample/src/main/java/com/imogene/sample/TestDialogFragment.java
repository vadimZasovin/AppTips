package com.imogene.sample;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.imogene.apptips.AppTips;
import com.imogene.apptips.Tip;

/**
 * Created by Vadim Zasovin on 06.03.18.
 */

public class TestDialogFragment extends DialogFragment {

    public static TestDialogFragment newInstance() {
        return new TestDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_test, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        if(window != null){
            Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
            window.setBackgroundDrawable(drawable);
        }

        AppTips appTips = new AppTips(this);
        Tip tip = appTips.newTip(R.id.text, "Tip for dialog");
        tip.setAlign(Tip.ALIGN_CENTER_BELOW);
        appTips.addTip(tip);
        appTips.show();
    }
}
