/*
 * Copyright (C) 2021 Chaldeaprjkt
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

package com.android.launcher3;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.util.Themes;

public class HotseatPisselBar extends LinearLayout implements OnSharedPreferenceChangeListener {
    private static final String TAG = "HotseatPisselBar";
    private static final String GSA_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String LENS_CLASS = "com.google.android.apps.lens.MainActivity";
    private static final Uri LENS_URI = Uri.parse("google://lens");

    public HotseatPisselBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupViews(context);
        SharedPreferences prefs = Utilities.getPrefs(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    private void setupViews(Context context) {
        ContextThemeWrapper themedContext = new ContextThemeWrapper(context,
            Themes.isThemedIconEnabled(context) ? R.style.PisselBarTheme_ThemedIcon
                : R.style.PisselBarTheme);

        LayoutInflater.from(context).cloneInContext(themedContext)
            .inflate(R.layout.hotseat_pisselbar, this, true);

        boolean hidePisselBar = Utilities.hidePisselBar(context);
        ViewGroup content = findViewById(R.id.pisselbar_content);
        if (content == null)
            return;

        content.setVisibility(hidePisselBar ? View.GONE : View.VISIBLE);
        if (hidePisselBar)
            return;

        setOnClickListener(v -> launchSearch());

        ImageView btnAssist = findViewById(R.id.pisselbar_btn_assistant);
        btnAssist.setOnClickListener(v -> launchAssistant());

        ImageView btnLens = findViewById(R.id.pisselbar_btn_lens);
        btnLens.setOnClickListener(v -> launchLens());
    }

    private void launchSearch() {
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setPackage(GSA_PACKAGE);

        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found for GLOBAL_SEARCH");
        }
    }

    private void launchLens() {
        Bundle params = new Bundle();
        params.putString("caller_package", GSA_PACKAGE);
        params.putLong("start_activity_time_nanos", SystemClock.elapsedRealtimeNanos());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setPackage(GSA_PACKAGE);
        intent.setComponent(new ComponentName(GSA_PACKAGE, LENS_CLASS));
        intent.setData(LENS_URI);
        intent.putExtra("lens_activity_params", params);

        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found for LENS_CLASS");
        }
    }

    private void launchAssistant() {
        Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setPackage(GSA_PACKAGE);

        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found for ACTION_VOICE_COMMAND");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (Themes.KEY_THEMED_ICONS.equals(key)) {
            removeAllViewsInLayout();
            setupViews(getContext());
        }
    }
}
