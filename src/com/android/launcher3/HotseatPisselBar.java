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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.android.launcher3.qsb.QsbContainerView;

public class HotseatPisselBar extends LinearLayout {
    private static final String TAG = "HotseatPisselBar";

    public HotseatPisselBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.hotseat_pisselbar, this, true);

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
        Intent lensIntent = Intent.makeMainActivity(new ComponentName("com.google.ar.lens", "com.google.vr.apps.ornament.app.lens.LensLauncherActivity"));
        lensIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        if (context.getPackageManager().resolveActivity(lensIntent, 0) != null) {
            btnLens.setOnClickListener(v -> context.startActivity(lensIntent));
        } else {
            btnLens.setVisibility(View.GONE);
        }
    }
}
