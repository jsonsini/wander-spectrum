/**
 * WanderSpectrum is an Android live wallpaper that animates a vertical stripe aligned down the
 * center of the screen displaying the visible spectrum in a cyclic pattern.  The animation is
 * randomly generated to follow an approximate Brownian motion pattern.  The speed of movement,
 * effective pixel size, frame rate of animation, and minimum duration to switch direction of
 * movement are all configurable.
 *
 * WanderSpectrum is distributed under the GNU Affero General Public License v3 as open source
 * software with attribution required.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License v3 for more details.
 *
 * Copyright (C) 2023 John Sonsini. All rights reserved. Source code available under the AGPLv3.
 */
package com.jsonsini.wanderspectrum

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.round
import kotlin.random.Random

/**
 * MainActivity is the entry point of the app, it sets the content view as a SurfaceView
 * and starts the WanderSpectrumWallpaper service when the application is launched.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Sets the content view of the activity to a SurfaceView.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // sets the content view as a surface view
        setContentView(SurfaceView(this))
    }

    /**
     * Called when the activity becomes visible to the user.  Starts the WanderSpectrumWallpaper
     * service where the user is prompted to set it as their live wallpaper.
     */
    override fun onResume() {
        super.onResume()
        // creates an instance of the wallpaper service
        val myWallpaperService = WanderSpectrumWallpaper()
        // creates a component name for the service
        val componentName = ComponentName(this, myWallpaperService::class.java)
        // creates an intent to change the live wallpaper
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        // adds the service to the intent as an extra
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
        // starts the intent
        startActivity(intent)
    }

}

/**
 * WanderSpectrumWallpaper is a live wallpaper service that provides an animated representation of
 * the visible spectrum.  The animation is randomly generated to emulate a Brownian motion pattern.
 */
class WanderSpectrumWallpaper : WallpaperService() {

    /**
     * onCreateEngine is used to create and return the Engine that will be used to drive the
     * animation.
     *
     * @return WanderSpectrumEngine
     */override fun onCreateEngine(): Engine {
        // creates an instance of the engine
        return WanderSpectrumEngine()
    }

    /**
     * WanderSpectrumEngine is a sub class of Engine that handles the animation of the wallpaper and
     * is in charge of updating the animation frames, handling user preferences, scrolling the
     * animation, and rendering the image.
     */
    private inner class WanderSpectrumEngine : Engine() {
        // sets the scroll velocity as the default value from the resources
        private var scrollVelocity = resources.getString(R.string.scroll_velocity_default).toInt()
        // sets the pixel size as the default value from the resources
        private var pixelSize = resources.getString(R.string.pixel_size_default).toInt()
        private var oldPixelSize = pixelSize
        // sets the frame rate as the default value from the resources
        private var frameRate = resources.getString(R.string.frame_rate_default).toInt()
        // sets the minimum direction switch time as the default value from the resources
        private var minimumDirectionSwitchSeconds =
            resources.getString(R.string.minimum_direction_switch_seconds_default).toInt()
        // flag to toggle moving the image
        private var animate = false
        // position of the upper edge of the image for wrapping around the screen
        private var imageY = 0
        private var startTime = 0L
        private var prerendered = false
        // matrix to store the color values of pixel making up the image
        private var colors: Array<IntArray>? = null
        private var frameCount = 0

        // defines a Handler object for scheduling the next frame of the animation
        private val handler = Handler()

        /**
         * Called when the wallpaper becomes visible or invisible to the user.  Starts or stops the
         * animation based on the visibility of the wallpaper.
         */
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            // starts or stops animating the image when the wallpaper becomes visible or invisible
            animate = visible
            if (visible) {
                startTime = System.currentTimeMillis()
                // loads the saved values from the SharedPreferences
                val sharedPref =
                    getSharedPreferences(
                        getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE
                    )
                // sets the scroll velocity as the value saved in the preferences
                scrollVelocity = sharedPref.getInt(
                    getString(R.string.scroll_velocity_key),
                    resources.getString(R.string.scroll_velocity_default).toInt()
                )
                // sets the pixel size as the value saved in the preferences
                pixelSize =
                    sharedPref.getInt(
                        getString(R.string.pixel_size_key),
                        resources.getString(R.string.pixel_size_default).toInt()
                    )
                // updates the old pixel size and forces the colors array to be recalculated
                if (pixelSize != oldPixelSize) {
                    prerendered = false
                    oldPixelSize = pixelSize
                }
                // sets the frame rate as the value saved in the preferences
                frameRate =
                    sharedPref.getInt(
                        getString(R.string.frame_rate_key),
                        resources.getString(R.string.frame_rate_default).toInt()
                    )
                // sets the minimum direction switch time as the value saved in the preferences
                minimumDirectionSwitchSeconds = sharedPref.getInt(
                    getString(R.string.minimum_direction_switch_seconds_key),
                    resources.getString(R.string.minimum_direction_switch_seconds_default).toInt()
                )
                drawFrame()
            }
        }

        /**
         * Rounding off to the nearest integer between 0 and 255 this method calculates intensity as
         * a function of distance from half of a maximum based on taking the absolute value to a
         * specific power creating a diminishing fog effect around the central point.
         */
        private fun dieOff(x: Double, max: Double): Double {
            return round(255 * (1 - abs(1 - x / (max / 2)).pow(0.09375)))
        }

        /**
         * Defines a piecewise sinusoidal curve limited to positive values.
         */
        private fun period(x: Double, peak: Double, period: Double): Double {
            return max(cos(2 * (x - peak) * PI / period), 0.0)
        }

        /**
         * Calculates the red channel based on pixel coordinates and screen size.
         */
        private fun redPart(x: Double, xmax: Double, y: Double, ymax: Double): Double {
            return dieOff(x, xmax) * (0.8 * (period(y, 0.0, 2 * ymax) + period(
                y, 3 * ymax / 2, 2 * ymax
            )) + 0.2)
        }

        /**
         * Calculates the green channel based on pixel coordinates and screen size.
         */
        private fun greenPart(x: Double, xmax: Double, y: Double, ymax: Double): Double {
            return dieOff(x, xmax) * (0.8 * period(y, ymax / 2, 2 * ymax) + 0.2)
        }

        /**
         * Calculates the blue channel based on pixel coordinates and screen size.
         */
        private fun bluePart(x: Double, xmax: Double, y: Double, ymax: Double): Double {
            return dieOff(x, xmax) * (0.8 * period(y, ymax, 2 * ymax) + 0.2)
        }

        /**
         * Renders the colors making up the scrolling image if not available, then updates the
         * displayed portion based on the frame rate, current velocity direction, and a randomly
         * generated trigger to switch directions.
         */
        private fun drawFrame() {
            val surfaceHolder = surfaceHolder
            val canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                // check to determine whether rendering occurred in a previous frame
                if (!prerendered) {
                    // populate the color values based on the canvas and pixel sizes
                    colors = Array(canvas.width / pixelSize) { i ->
                        IntArray((1.5 * canvas.height / pixelSize).toInt()) { j ->
                            val red = redPart(
                                i.toDouble(),
                                canvas.width.toDouble() / pixelSize,
                                j.toDouble(),
                                canvas.height.toDouble() / pixelSize
                            )
                            val green = greenPart(
                                i.toDouble(),
                                canvas.width.toDouble() / pixelSize,
                                j.toDouble(),
                                canvas.height.toDouble() / pixelSize
                            )
                            val blue = bluePart(
                                i.toDouble(),
                                canvas.width.toDouble() / pixelSize,
                                j.toDouble(),
                                canvas.height.toDouble() / pixelSize
                            )
                            Color.argb(255, red.toInt(), green.toInt(), blue.toInt())
                        }
                    }
                    prerendered = true
                }

                // clear the canvas
                canvas.drawColor(Color.BLACK)

                // update the velocity of the image
                if ((Random.nextInt(0, 10) == 0) &&
                    ((minimumDirectionSwitchSeconds * frameRate) < frameCount)
                ) {
                    scrollVelocity = -scrollVelocity
                    frameCount = 0
                }

                // update the position of the image
                imageY += scrollVelocity
                frameCount += 1

                // iterate over all points in the image
                for (i in 0 until canvas.width / pixelSize) {
                    for (j in 0 until canvas.height / pixelSize) {
                        val color = colors?.get(i)
                            ?.get(((j + imageY) % (1.5 * canvas.height / pixelSize)).toInt())
                        val paint = Paint()
                        if (color != null) {
                            paint.color = color
                        }
                        canvas.drawRect(
                            (i * pixelSize).toFloat(),
                            (j * pixelSize).toFloat(),
                            ((i + 1) * pixelSize).toFloat(),
                            ((j + 1) * pixelSize).toFloat(), paint
                        )
                    }
                }

                surfaceHolder.unlockCanvasAndPost(canvas)
            }

            // schedule the next frame of the animation
            if (animate) {
                handler.postDelayed({
                    drawFrame()
                }, (1000 / frameRate).toLong())
            }
        }
    }
}