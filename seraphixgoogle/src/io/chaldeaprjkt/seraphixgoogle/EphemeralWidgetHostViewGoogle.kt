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
package io.chaldeaprjkt.seraphixgoogle

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import io.chaldeaprjkt.seraphixgoogle.SeraphixCompanion.allChildren

class EphemeralWidgetHostViewGoogle(context: Context?) : AppWidgetHostView(context) {
    private val weatherCard = Card()
    private var listener: DataProviderListener? = null

    override fun updateAppWidget(remoteViews: RemoteViews?) {
        super.updateAppWidget(remoteViews)
        allChildren().forEach {
            when (it) {
                is ImageView -> grabWeatherIcon(it)
                is TextView -> grabWeatherText(it)
            }
        }

        listener?.onDataUpdated(weatherCard)
    }

    private fun grabWeatherText(view: TextView) {
        if (view.id == -1) return

        val strId = view.resources.getResourceEntryName(view.id)
        if (strId == VID_WEATHER_TEXT) {
            view.text.takeIf { it.isNotEmpty() }?.let {
                weatherCard.text = it.toString()
            }
        }
    }

    private fun grabWeatherIcon(view: ImageView) {
        if (view.id == -1) return

        val strId = view.resources.getResourceEntryName(view.id)
        if (strId == VID_WEATHER_ICON) {
            (view.drawable as? BitmapDrawable)?.bitmap?.let {
                weatherCard.image = it
            }
        }
    }

    fun setOnUpdateAppWidget(listener: DataProviderListener? = null): EphemeralWidgetHostViewGoogle {
        this.listener = listener
        return this
    }

    companion object {
        const val VID_WEATHER_TEXT = "title_weather_text"
        const val VID_WEATHER_ICON = "title_weather_icon"
    }
}
