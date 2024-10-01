package com.hardik.notepad.common


import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

//class ViewUtils {}
//    private val TAG = BASE_TAG + ViewUtils::class.java.simpleName


// Extension function to fade in a View with animation
fun View.fadeIn(duration: Long = 300) {
    if (visibility == View.VISIBLE) return // No need to animate if already visible
    visibility = View.VISIBLE
    alpha = 0f
    animate().alpha(1f).setDuration(duration).setListener(null)
}

// Extension function to fade out a View with animation
fun View.fadeOut(duration: Long = 300) {
    if (visibility == View.GONE || visibility == View.INVISIBLE) return // No need to animate if already gone
    animate().alpha(0f).setDuration(duration)
        .setListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                visibility = View.GONE
            }
        })
}

// Extension function to set constraints for an included layout
fun View.setConstraintsForIncludedLayout(
    includedLayoutId: Int,
    bottomToBottomOf: Int = ConstraintLayout.LayoutParams.PARENT_ID,
    endToEndOf: Int = ConstraintLayout.LayoutParams.PARENT_ID
) {
    val parent = this.parent as? ConstraintLayout ?: return
    val includedLayout = parent.findViewById<View>(includedLayoutId) ?: return

    val constraintSet = ConstraintSet()
    constraintSet.clone(parent)

    constraintSet.connect(includedLayout.id, ConstraintSet.BOTTOM, bottomToBottomOf, ConstraintSet.BOTTOM)
    constraintSet.connect(includedLayout.id, ConstraintSet.END, endToEndOf, ConstraintSet.END)

    constraintSet.applyTo(parent)
}
