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

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.launcher3.qsb.QsbContainerView;

public class HotseatPisselBar extends LinearLayout {
    private static final String TAG = "HotseatPisselBar";
    private static final String GOOGLE_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String GOOGLE_LENS_CLASS = "com.google.android.apps.lens.MainActivity";
    private static final Uri GOOGLE_LENS_URI = Uri.parse("google://lens");

    public HotseatPisselBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.hotseat_pisselbar, this, true);

        boolean hidePisselBar = Utilities.hidePisselBar(getContext());
        ViewGroup content = findViewById(R.id.pisselbar_content);
        if (content == null)
            return;

        content.setVisibility(hidePisselBar ? View.GONE : View.VISIBLE);
        if (hidePisselBar)
            return;

        setOnClickListener(v -> {
            Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setPackage(QsbContainerView.getSearchWidgetPackageName(context));

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No activity found for android.search.action.GLOBAL_SEARCH");
            }
        });

        ImageView btnAssist = findViewById(R.id.pisselbar_btn_assistant);
        btnAssist.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setPackage(QsbContainerView.getSearchWidgetPackageName(context));
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No activity found for ACTION_VOICE_COMMAND");
            }
        });

        ImageView btnLens = findViewById(R.id.pisselbar_btn_lens);
        btnLens.setOnClickListener(v -> launchLens(context));
    }

    private void setPaddingStart(View view, int padding) {
        if (view == null) return;

        view.setPaddingRelative(padding, view.getPaddingTop(),
                view.getPaddingEnd(), view.getPaddingBottom());
    }

    private void setPaddingEnd(View view, int padding) {
        if (view == null) return;

        view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(),
                padding, view.getPaddingBottom());
    }

    public static void launchLens(Context context) {
        Bundle params = new Bundle();
        params.putString("caller_package", GOOGLE_PACKAGE);
        params.putLong("start_activity_time_nanos", SystemClock.elapsedRealtimeNanos());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setPackage(GOOGLE_PACKAGE);
        intent.setComponent(new ComponentName(GOOGLE_PACKAGE, GOOGLE_LENS_CLASS));
        intent.setData(GOOGLE_LENS_URI);
        intent.putExtra("lens_activity_params", params);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found for GOOGLE_LENS_CLASS");
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ViewGroup content = findViewById(R.id.pisselbar_content);
        if (content == null)
            return;

        int lastIndex = content.getChildCount() - 1;
        View firstIcon = content.getChildAt(0);
        View lastIcon = content.getChildAt(lastIndex);
        View secondLastIcon = content.getChildAt(lastIndex - 1);
        if (firstIcon == null || lastIcon == null || secondLastIcon == null)
            return;

        int sidePadding = getResources().getDimensionPixelSize(R.dimen.pisselbar_icon_padding_side);
        setPaddingStart(firstIcon, sidePadding);
        if (lastIcon.getVisibility() == View.GONE) {
            setPaddingEnd(lastIcon, lastIcon.getPaddingStart());
            setPaddingEnd(secondLastIcon, sidePadding);
        } else {
            setPaddingEnd(lastIcon, sidePadding);
            setPaddingEnd(secondLastIcon, secondLastIcon.getPaddingStart());
        }
    }
}
