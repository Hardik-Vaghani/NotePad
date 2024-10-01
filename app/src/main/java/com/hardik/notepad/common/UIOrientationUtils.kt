package com.hardik.notepad.common


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.alpha
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hardik.notepad.R

private const val HIDE_DELAY_MILLIS: Long = 10000 // 10 seconds
private const val SHOW_DELAY_MILLIS: Long = 3000 // 3 seconds

@SuppressLint("ObsoleteSdkInt")
class UIOrientationUtils private constructor() {
    init {
        Log.i(TAG, "UIOrientationUtils: ")
    }

    fun isTablet(activity: Activity): Boolean {
        Log.i(TAG, "isTablet: ")
        val config = activity.resources.configuration
        val screenLayout = config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

        // Check if the device is a tablet
        val isTablet = screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE
        Log.i(TAG, "isTablet: $isTablet")
        return isTablet
    }

    fun isOrientationLandscape(activity: Activity): Boolean {
        Log.i(TAG, "isLandscape: ")
        val orientation = activity.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun isOrientationPortrait(activity: Activity): Boolean {
        Log.i(TAG, "isPortrait: ")
        val orientation = activity.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }

    fun setLandscapeOrientation(activity: Activity) {
        Log.i(TAG, "setLandscape: ")
        // Lock the orientation to landscape
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun setPortraitOrientation(activity: Activity) {
        Log.i(TAG, "setPortrait: ")
        // Lock the orientation to portrait
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun isOrientationLock(activity: Activity): Boolean {
        Log.i(TAG, "isLockOrientation: ")
        // The orientation is locked
        return isOrientationLandscape(activity) || isOrientationPortrait(activity)
    }

    fun setOrientationLock(activity: Activity) {
//        if (isOrientationLandscape(activity)) {
//            setLandscapeOrientation(activity)
//        } else {
//            setPortraitOrientation(activity)
//        }
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    fun setOrientationUnlock(activity: Activity) {
        Log.i(TAG, "unlockOrientation: ")
        // Unlock the orientation portrait/landscape both.
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }

    @SuppressLint("Range")
    fun setFullScreenSystemUI(activity: Activity) {// it's full screen
        Log.i(TAG, "setFullScreenUI: UIFullScreen: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set status bar and navigation bar colors to transparent
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window.navigationBarColor = Color.TRANSPARENT

            // Set system UI visibility flags
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // if you want to show
                            or View.SYSTEM_UI_FLAG_LOW_PROFILE
                    )

            // Set behavior for system bars (status bar and navigation bar)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = activity.window.insetsController
                controller?.hide(WindowInsetsCompat.Type.statusBars().alpha)
                controller?.hide(WindowInsetsCompat.Type.navigationBars().alpha)
                controller?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        // Ensure decor fits system windows
//        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
    }

    fun setFullScreenWithStatusBarSystemUI(activity: Activity) {// it's full screen with status bar
        Log.i(TAG, "setNormalScreenUI: ")
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window.navigationBarColor = Color.TRANSPARENT
        }
        //        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Delayed hiding of status bar and navigation bar
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                or View.SYSTEM_UI_FLAG_LOW_PROFILE)

        // Ensure the window decor view fits system windows (optional)
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.hide(WindowInsetsCompat.Type.statusBars().alpha)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun setNormalScreenSystemUI(activity: Activity) {// show system bars and navigation bar
        val window = activity.window

        // Clear all existing flags
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0

        // Set colors for status bar and navigation bar
        window.statusBarColor = activity.resources.getColor(R.color.statusBarColor, activity.theme)
        window.navigationBarColor =
            activity.resources.getColor(R.color.navigationBarColor, activity.theme)

        // Clear flags using WindowCompat (for compatibility)
        // For Android 11 (API level 30) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            controller?.let {
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                it.show(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // For dark text on status bar
//                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR // For dark text on navigation bar
                        )

        }

        // Ensure decor fits system windows
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    fun setNormalScreenSystemUI1(activity: AppCompatActivity) {// show system bars and navigation bar
        // Clear existing fitting of system windows decor
        val window = activity.window
        // Clear all existing flags
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Now set the fitting of system windows decor again
        WindowCompat.setDecorFitsSystemWindows(window, true)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    fun hideSystemUI(activity: AppCompatActivity) {
        // Ensure the window decor view fits system windows (optional)
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        // Get the WindowInsetsControllerCompat
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        // Hide system bars (status bar and navigation bar)
        controller.hide(WindowInsetsCompat.Type.systemBars().alpha)
        // Optionally set behavior for transient bars (such as status bar or navigation bar)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // device is Notch type or not
    fun hasNotch(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayCutout = windowManager.currentWindowMetrics.windowInsets.displayCutout
            return displayCutout != null && displayCutout.boundingRects.isNotEmpty()
        } else {
            // For devices below Android R, handle accordingly (fallback logic)
            // You can implement your own logic here depending on the requirements
            // This example throws an exception but you should replace it with your actual logic
            throw UnsupportedOperationException("Device below Android R is not supported")
        }
    }

    // front camera is inside display or not
    @SuppressLint("NewApi")
    fun isCameraInsideDisplay(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayCutout = windowManager.currentWindowMetrics.windowInsets.displayCutout

        // Example: Estimate camera position based on notch area
        val notchHeight = displayCutout?.safeInsetTop ?: 0
        val screenWidth = context.resources.displayMetrics.widthPixels

        // Adjust these values based on device-specific notch dimensions
        val cameraTop = 0 // Adjust based on your device's notch top position
        val cameraBottom = notchHeight // Adjust based on your device's notch height

        // Check if the camera position intersects with the notch area
        return cameraBottom > 0 && cameraTop < notchHeight
    }

    companion object {
        val TAG = UIOrientationUtils::class.java.simpleName
        var instance: UIOrientationUtils? = null
            get() {
                if (field == null) {
                    synchronized(UIOrientationUtils::class.java) {
                        if (field == null) {
                            field = UIOrientationUtils()
                        }
                    }
                }
                return field
            }
            private set
    }
}

