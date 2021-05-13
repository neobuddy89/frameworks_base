/*
 * Copyright (C) 2021 ExtendedUI
 *               2021 Wave-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.biometrics

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.provider.Settings
import android.provider.Settings.System.FOD_ICON_ANIMATION
import android.view.WindowManager
import android.widget.ImageView

import com.android.systemui.R

class FODIconView(context: Context, size: Int, x: Int, y: Int) :
    ImageView(context) {
    private var iconAnim: AnimationDrawable? = null
    private var isFODIconAnimated: Boolean
    private var isKeyguard = false
    private val params = WindowManager.LayoutParams()
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    init {
        scaleType = ScaleType.CENTER_INSIDE
        context.resources
        params.height = size
        params.width = size
        params.format = -3
        params.packageName = "android"
        params.type = 2020
        params.flags = 264
        params.gravity = 51
        params.x = x
        params.y = y
        params.title = "Fingerprint on display icon"
        windowManager.addView(this, params)
        isFODIconAnimated =
            Settings.System.getInt(
                getContext().contentResolver,
                FOD_ICON_ANIMATION,
                0
            ) != 0
        if (isFODIconAnimated) {
            setBackgroundResource(R.drawable.fod_icon_anim)
            iconAnim = background as AnimationDrawable
        } else {
            setImageResource(R.drawable.fod_icon_default)
        }
        hide()
    }

    fun show() {
        setIsAnimationEnabled(
            Settings.System.getInt(
                context.contentResolver,
                FOD_ICON_ANIMATION,
                0
            ) != 0
        )
        visibility = VISIBLE
        if (isFODIconAnimated && isKeyguard) {
            iconAnim?.start()
        }
    }

    fun hide() {
        visibility = GONE
        if (isFODIconAnimated) {
            clearAnimation()
            iconAnim?.stop()
            iconAnim?.selectDrawable(0)
        }
    }

    fun updatePosition(x: Int, y: Int) {
        params.x = x
        params.y = y
        windowManager.updateViewLayout(this, params)
    }

    fun setIsAnimationEnabled(isAnimationEnabled: Boolean) {
        isFODIconAnimated = isAnimationEnabled
        if (isAnimationEnabled) {
            setImageResource(0)
            setBackgroundResource(R.drawable.fod_icon_anim)
            iconAnim = background as AnimationDrawable
            return
        }
        setBackgroundResource(0)
        setImageResource(R.drawable.fod_icon_default)
    }

    fun setIsKeyguard(keyguard: Boolean) {
        isKeyguard = keyguard
        if (isKeyguard && !isFODIconAnimated) {
            setColorFilter(-1)
        } else if (isKeyguard || !isFODIconAnimated) {
            backgroundTintList = null
            colorFilter = null
        } else {
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#807B7E"))
        }
    }
}
