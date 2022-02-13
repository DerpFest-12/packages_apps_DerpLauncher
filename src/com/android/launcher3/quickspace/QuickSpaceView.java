/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.quickspace;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import com.android.launcher3.quickspace.QuickspaceController.OnDataListener;
import com.android.launcher3.quickspace.receivers.QuickSpaceActionReceiver;
import com.android.launcher3.quickspace.views.DateTextView;

import io.chaldeaprjkt.seraphixgoogle.Card;
import io.chaldeaprjkt.seraphixgoogle.DataProviderListener;
import io.chaldeaprjkt.seraphixgoogle.SeraphixDataProvider;

public class QuickSpaceView extends FrameLayout implements OnDataListener {

    private static final String TAG = "Launcher3:QuickSpaceView";
    private static final boolean DEBUG = false;

    private DateTextView mTitle;
    private ViewGroup mEventContainer;
    private ImageView mEventIcon;
    private TextView mEventText;
    private ViewGroup mWeatherContainer;
    private ImageView mWeatherIcon;
    private TextView mWeatherTemp;

    private boolean mIsQuickEvent;
    private boolean mFinishedInflate;
    private boolean mWeatherAvailable;

    private QuickSpaceActionReceiver mActionReceiver;
    private QuickspaceController mController;
    private SeraphixDataProvider mSeraphixDataProvider;

    public QuickSpaceView(Context context, AttributeSet set) {
        super(context, set);
        mActionReceiver = new QuickSpaceActionReceiver(context);
        mController = new QuickspaceController(context);
        setClipChildren(false);
        mSeraphixDataProvider = new SeraphixDataProvider(context, 1022, Utilities.getSeraphixHolderId(context));
        mSeraphixDataProvider.setOnDataUpdated(mDataProviderListener);
        getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayout);
    }

    @Override
    public void onDataUpdated() {
        mController.getEventController().initQuickEvents();
        if (mIsQuickEvent != mController.isQuickEvent()) {
            mIsQuickEvent = mController.isQuickEvent();
            loadViews();
        }
        mWeatherAvailable = mController.isWeatherAvailable() &&
                mController.getEventController().isDeviceIntroCompleted();
        loadWeather();
        loadEvent();
    }

    private void loadEvent() {
        mTitle.setEventMode(mIsQuickEvent);
        mEventContainer.setVisibility(mIsQuickEvent ? View.VISIBLE : View.GONE);
        mEventContainer.setOnClickListener(mIsQuickEvent ? null : mController.getEventController().getAction());

        if (!mIsQuickEvent) {
            mTitle.onVisibilityAggregated(true);
            return;
        }

        mTitle.setText(mController.getEventController().getTitle());
        mTitle.setOnClickListener(Utilities.showDateInPlaceOfNowPlaying(getContext()) ?
            mActionReceiver.getCalendarAction() : mController.getEventController().getAction());

        mEventContainer.setOnClickListener(mController.getEventController().getAction());
        mEventText.setText(mController.getEventController().getActionTitle());
        mEventText.setMarqueeRepeatLimit(-1);
        mEventText.setSelected(true);
        mEventIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        mEventIcon.setImageResource(mController.getEventController().getActionIcon());
        Utilities.addShadowToImageView(mEventIcon, 3f, 125);
    }

    private void loadWeather() {
        mWeatherContainer.setVisibility(mWeatherAvailable ? View.VISIBLE : View.GONE);

        if (mWeatherAvailable) {
            mWeatherContainer.setOnClickListener(mActionReceiver.getWeatherAction());
            mWeatherTemp.setText(mController.getWeatherTemp());
            mWeatherIcon.setImageIcon(mController.getWeatherIcon());
            Utilities.addShadowToImageView(mWeatherIcon, 3f, 125);
        }
    }

    private void loadViews() {
        mTitle = (DateTextView) findViewById(R.id.quickspace_title);

        mEventContainer = (ViewGroup) findViewById(R.id.quick_event_container);
        mEventIcon = (ImageView) findViewById(R.id.quick_event_icon);
        mEventText = (TextView) findViewById(R.id.quick_event_text);

        mWeatherContainer = (ViewGroup) findViewById(R.id.quick_event_weather_container);
        mWeatherIcon = (ImageView) findViewById(R.id.quick_event_weather_icon);
        mWeatherTemp = (TextView) findViewById(R.id.quick_event_weather_temp);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mController != null && mFinishedInflate) {
            mController.addListener(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mController != null) {
            mController.removeListener(this);
        }
    }

    private void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayout);
        if (isAttachedToWindow()) {
            mSeraphixDataProvider.bind((id) -> Utilities.setSeraphixHolderId(getContext(), id));
        } else {
            mSeraphixDataProvider.unbind();
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        loadViews();
        mFinishedInflate = true;
        if (isAttachedToWindow()) {
            if (mController != null) {
                mController.addListener(this);
            }
        }
    }

    public void onPause() {
        mController.onPause();
    }

    public void onResume() {
        mController.onResume();
    }

    private final DataProviderListener mDataProviderListener = new DataProviderListener() {
        @Override
        public void onDataUpdated(@NonNull Card card) {
            if (mController == null) return;
            mController.updateWeatherData(card.getText(), card.getImage());
        }
    };

}
