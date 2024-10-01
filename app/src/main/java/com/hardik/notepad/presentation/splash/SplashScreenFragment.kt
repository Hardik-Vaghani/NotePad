package com.hardik.notepad.presentation.splash

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hardik.notepad.presentation.MainActivity
import com.hardik.notepad.R
import com.hardik.notepad.common.Constants.LAUNCH_INSTANTLY

class SplashScreenFragment : Fragment() {

    private lateinit var activity: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = requireActivity() as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       activity.switchToHomeFragment(launchInstantly = !LAUNCH_INSTANTLY)

        val rootLayout = view.findViewById<View>(R.id.root_layout)

        val animDrawable = rootLayout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(750)
        animDrawable.setExitFadeDuration(750)
        animDrawable.start()
    }

}
