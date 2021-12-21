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
package com.android.launcher3.quickspace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;

import com.android.launcher3.SmartspaceProto.SmartspaceUpdate;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class SmartSpaceData {
    public static final String ACTION_REFRESH = "com.android.launcher3.action.REFRESH_SMARTSPACE";

    public static final String EXTRA_CARD_GOOGLE = "com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD";
    public static final String RAW_PREFIX = "smartspace_";

    public static final String KEY_WEATHER_ICON = "icon_weather";
    public static final String KEY_CARD = "card";

    public static void storeRawData(Context context, String name, byte[] data) {
        try {
            FileOutputStream saved = context.openFileOutput(RAW_PREFIX + name, 0);
            saved.write(data);
            saved.close();
        } catch (IOException ignored) {
        }
    }

    public static void storeWeatherBitmap(Context context, Intent intent, Stream<SmartspaceUpdate.SmartspaceCard> cards) {
        cards.filter(it -> it.hasIcon() && it.hasDuringEvent())
                .findFirst()
                .ifPresent(it -> {
                    try {
                        Bitmap img = intent.getParcelableExtra(it.getIcon().getKey());
                        if (img != null) {
                            ByteArrayOutputStream iconStream = new ByteArrayOutputStream();
                            img.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                            SmartSpaceData.storeRawData(context, KEY_WEATHER_ICON, iconStream.toByteArray());
                            img.recycle();
                            iconStream.close();
                        }
                    } catch (IOException ignored) {
                    }
                });
    }

    public static void storeIntentData(Context context, Intent intent) {
        byte[] card = intent.getByteArrayExtra(EXTRA_CARD_GOOGLE);
        if (card == null) {
            return;
        }

        try {
            SmartspaceUpdate updates = SmartspaceUpdate.newBuilder().mergeFrom(card).build();
            Supplier<Stream<SmartspaceUpdate.SmartspaceCard>> cards = () -> updates.getCardList().stream();
            boolean hasWeatherCard = cards.get().anyMatch(SmartSpaceData::isCardHasWeatherIcon);
            if (hasWeatherCard) {
                storeRawData(context, KEY_CARD, card);
                storeWeatherBitmap(context, intent, cards.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            context.sendBroadcast(new Intent(ACTION_REFRESH));
        }
    }

    private static boolean isCardHasWeatherIcon(SmartspaceUpdate.SmartspaceCard it) {
        return it.hasIcon() && it.getIcon().hasUri() && it.getIcon().getUri().contains("weather");
    }

    public static byte[] getSavedRawData(Context context, String name) {
        try {
            File saved = context.getFileStreamPath(RAW_PREFIX + name);
            byte[] bytes = new byte[(int) saved.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(saved));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(bytes);
            dis.close();
            return bytes;
        } catch (IOException ignored) {
        }
        return null;
    }

    public static SmartspaceUpdate getUpdate(Context context) {
        try {
            byte[] bytes = getSavedRawData(context, KEY_CARD);
            if (bytes != null) {
                return SmartspaceUpdate.newBuilder().mergeFrom(bytes).build();
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static Bitmap getWeatherBitmap(Context context) {
        byte[] bytes = getSavedRawData(context, KEY_WEATHER_ICON);
        if (bytes == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static Icon getWeatherIcon(Context context) {
        return Icon.createWithBitmap(getWeatherBitmap(context));
    }

    public static String getWeatherText(Context context) {
        SmartspaceUpdate updates = SmartSpaceData.getUpdate(context);
        if (updates == null) {
            return null;
        }

        return updates.getCardList().stream()
                .filter(SmartSpaceData::isCardHasWeatherIcon)
                .findFirst()
                .map(it -> it.getDuringEvent().getTitle().getText())
                .orElse(null);
    }

    public static boolean hasWeatherData(Context context) {
        return getWeatherText(context) != null && getWeatherBitmap(context) != null;
    }
}
