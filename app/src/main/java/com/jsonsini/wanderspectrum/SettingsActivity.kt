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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

/**
 * SettingsActivity allows the user to configure the scroll velocity, pixel size, frame rate, and
 * minimum duration to switch the direction of movement via displaying a set of SeekBars for the
 * different configuration options and provides save and reset buttons for storing the selected
 * values in SharedPreferences which are loaded whenever the activity is resumed.
 */
class SettingsActivity : AppCompatActivity() {

    // scroll velocity of the image initialized to the configured default value
    private var scrollVelocity = R.string.scroll_velocity_default
    // image resolution initialized to the configured default value
    private var pixelSize = R.string.pixel_size_default
    // animation refresh rate per second initialized to the configured default value
    private var frameRate = R.string.frame_rate_default
    // minimum duration to switch motion initialized to the configured default value
    private var minimumDirectionSwitchSeconds = R.string.minimum_direction_switch_seconds_default

    /**
     * Loads the saved values from the SharedPreferences.
     */
    private fun updateFromSharedPreferences() {
        val sharedPref =
            getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        scrollVelocity = sharedPref.getInt(
            getString(R.string.scroll_velocity_key),
            resources.getString(R.string.scroll_velocity_default).toInt()
        )
        pixelSize =
            sharedPref.getInt(
                getString(R.string.pixel_size_key),
                resources.getString(R.string.pixel_size_default).toInt()
            )
        frameRate =
            sharedPref.getInt(
                getString(R.string.frame_rate_key),
                resources.getString(R.string.frame_rate_default).toInt()
            )
        minimumDirectionSwitchSeconds = sharedPref.getInt(
            getString(R.string.minimum_direction_switch_seconds_key),
            resources.getString(R.string.minimum_direction_switch_seconds_default).toInt()
        )
    }

    /**
     * Specifies behavior to capture associated value on manipulating scroll velocity seek bar and
     * updates corresponding label displaying current value.
     */
    private fun initializeScrollVelocitySeekBarListener(
        scrollVelocitySeekBar: SeekBar,
        scrollVelocityTextViewValue: TextView
    ) {
        scrollVelocitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scrollVelocity = progress
                scrollVelocityTextViewValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Specifies behavior to capture associated value on manipulating pixel size seek bar and
     * updates corresponding label displaying current value.
     */
    private fun initializePixelSizeSeekBarListener(
        pixelSizeSeekBar: SeekBar,
        pixelSizeTextViewValue: TextView
    ) {
        pixelSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pixelSize = progress
                pixelSizeTextViewValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Specifies behavior to capture associated value on manipulating frame rate seek bar and
     * updates corresponding label displaying current value.
     */
    private fun initializeFrameRateSeekBarListener(
        frameRateSeekBar: SeekBar,
        frameRateTextViewValue: TextView
    ) {
        frameRateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                frameRate = progress
                frameRateTextViewValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Specifies behavior to capture associated value on manipulating minimum direction switch
     * seconds seek bar and updates corresponding label displaying current value.
     */
    private fun initializeMinimumDirectionSwitchSecondsSeekBarListeners(
        minimumDirectionSwitchSecondsSeekBar: SeekBar,
        minimumDirectionSwitchSecondsTextViewValue: TextView
    ) {
        minimumDirectionSwitchSecondsSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                minimumDirectionSwitchSeconds = progress
                minimumDirectionSwitchSecondsTextViewValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Specifies behavior to store current seek bar values in shared preferences and reset to
     * default configured values when necessary.
     */
    private fun initializeButtonListeners(
        saveButton: Button, resetButton: Button,
        scrollVelocitySeekBar: SeekBar,
        pixelSizeSeekBar: SeekBar, frameRateSeekBar: SeekBar,
        minimumDirectionSwitchSecondsSeekBar: SeekBar
    ) {
        // Set up the save button
        saveButton.setOnClickListener {
            // Save the modified values to the SharedPreferences
            val pref =
                getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
            with(pref.edit()) {
                putInt(getString(R.string.scroll_velocity_key), scrollVelocity)
                putInt(getString(R.string.pixel_size_key), pixelSize)
                putInt(getString(R.string.frame_rate_key), frameRate)
                putInt(
                    getString(R.string.minimum_direction_switch_seconds_key),
                    minimumDirectionSwitchSeconds
                )
                apply()
            }
            // Go back to the main activity
            finish()
        }
        // Set up the reset button
        resetButton.setOnClickListener {
            // Reset the values to their default values
            scrollVelocity = resources.getString(R.string.scroll_velocity_default).toInt()
            pixelSize = resources.getString(R.string.pixel_size_default).toInt()
            frameRate = resources.getString(R.string.frame_rate_default).toInt()
            minimumDirectionSwitchSeconds =
                resources.getString(R.string.minimum_direction_switch_seconds_default).toInt()
            // Save the default values to the SharedPreferences
            val pref =
                getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
            with(pref.edit()) {
                putInt(getString(R.string.scroll_velocity_key), scrollVelocity)
                putInt(getString(R.string.pixel_size_key), pixelSize)
                putInt(getString(R.string.frame_rate_key), frameRate)
                putInt(
                    getString(R.string.minimum_direction_switch_seconds_key),
                    minimumDirectionSwitchSeconds
                )
                apply()
            }
            // Update the SeekBar positions
            scrollVelocitySeekBar.progress = scrollVelocity
            pixelSizeSeekBar.progress = pixelSize
            frameRateSeekBar.progress = frameRate
            minimumDirectionSwitchSecondsSeekBar.progress = minimumDirectionSwitchSeconds
        }
    }

    /**
     * Called when the wallpaper settings are accessed, retrieves values from shared preferences to
     * configure each of the SeekBars, and defines the behavior of each SeekBar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        updateFromSharedPreferences()

        val scrollVelocitySeekBar = findViewById<SeekBar>(R.id.scroll_velocity_seek_bar)
        val scrollVelocityTextViewValue =
            findViewById<TextView>(R.id.scroll_velocity_text_value)
        val pixelSizeSeekBar = findViewById<SeekBar>(R.id.pixel_size_seek_bar)
        val pixelSizeTextViewValue = findViewById<TextView>(R.id.pixel_size_text_value)
        val frameRateSeekBar = findViewById<SeekBar>(R.id.frame_rate_seek_bar)
        val frameRateTextViewValue = findViewById<TextView>(R.id.frame_rate_text_value)
        val minimumDirectionSwitchSecondsSeekBar =
            findViewById<SeekBar>(R.id.minimum_direction_switch_seconds_seek_bar)
        val minimumDirectionSwitchSecondsTextViewValue =
            findViewById<TextView>(R.id.minimum_direction_switch_seconds_text_value)
        val saveButton = findViewById<Button>(R.id.save_button)
        val resetButton = findViewById<Button>(R.id.reset_to_default_button)

        // Set the initial values of the SeekBars
        scrollVelocitySeekBar.progress = scrollVelocity
        pixelSizeSeekBar.progress = pixelSize
        frameRateSeekBar.progress = frameRate
        minimumDirectionSwitchSecondsSeekBar.progress = minimumDirectionSwitchSeconds

        // Set the initial values of the value labels
        scrollVelocityTextViewValue.text = scrollVelocity.toString()
        pixelSizeTextViewValue.text = pixelSize.toString()
        frameRateTextViewValue.text = frameRate.toString()
        minimumDirectionSwitchSecondsTextViewValue.text = minimumDirectionSwitchSeconds.toString()

        // Set the behaviors for the SeekBar and their associated value labels
        initializeScrollVelocitySeekBarListener(scrollVelocitySeekBar, scrollVelocityTextViewValue)
        initializePixelSizeSeekBarListener(pixelSizeSeekBar, pixelSizeTextViewValue)
        initializeFrameRateSeekBarListener(frameRateSeekBar, frameRateTextViewValue)
        initializeMinimumDirectionSwitchSecondsSeekBarListeners(
            minimumDirectionSwitchSecondsSeekBar, minimumDirectionSwitchSecondsTextViewValue
        )

        // Set the behaviors for the save and reset buttons
        initializeButtonListeners(
            saveButton, resetButton, scrollVelocitySeekBar, pixelSizeSeekBar,
            frameRateSeekBar, minimumDirectionSwitchSecondsSeekBar
        )
    }
}