package com.imogene.apptips;

/**
 * Created by Admin on 25.04.2016.
 */
public interface IBuilder {

    IBuilder setDimAmount(float dimAmount);

    IBuilder addTip(TipOptions options);

    IBuilder addTip(int viewId, CharSequence text);

    IBuilder setDefaultOptions(TipOptions options);

    AppTips build();

    AppTips show();

}
