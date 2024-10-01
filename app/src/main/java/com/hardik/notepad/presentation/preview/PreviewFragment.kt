package com.hardik.notepad.presentation.preview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hardik.notepad.R
import com.hardik.notepad.common.Constants
import com.hardik.notepad.common.Constants.AUTOMATIC_CLOSE_POPUP_MENU_WINDOW
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.databinding.AlertDialogBoxExportFileBinding
import com.hardik.notepad.databinding.CustomPopupWindowPreviewBinding
import com.hardik.notepad.databinding.FragmentPreviewBinding
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.model.notesToJson
import com.hardik.notepad.extra_class.createCsvFile
import com.hardik.notepad.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PreviewFragment : Fragment() {

    private val tag = BASE_TAG + PreviewFragment::class.java.simpleName

    private val previewViewModel: PreviewViewModel by viewModels()

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private lateinit var textView: TextView

    private lateinit var selectedNote: List<Note>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onResume() {
        super.onResume()
        mainActivity.also {
            it.mainViewModel.updateNote(note = mainActivity.mainViewModel.selectedNote.value)
            it.sivOpenDrawer.visibility = View.GONE // drawer icon is not visible now
            it.setDrawerEnabled(enabled = false) // now drawer is not open yet.
        }
    }
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // back button
        binding.includedToolbarLayout.toolbarLayoutIBtnBack.apply {
            mainActivity.sivOpenDrawer.visibility = View.GONE
            visibility = View.VISIBLE
            setOnClickListener {
                mainActivity.fragmentSessionUtils.handleBackPressed(mainActivity)
            }
        }

        // menu button
        binding.includedToolbarLayout.toolbarLayoutIBtnMenu.apply {
            setOnClickListener {
                showCustomPopupWindow(it)
            }
        }


        mainActivity.mainViewModel.selectedNote.observe(viewLifecycleOwner) { selectedNote ->
            Log.d(tag, "onViewCreated: $selectedNote")
            val htmlText = "${selectedNote.title} <small><small><i>- (${selectedNote.subject})</i></small></small>"
            binding.includedToolbarLayout.toolbarLayoutTvTitle.apply {
                text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
                setTextIsSelectable(true)
            }
            binding.fragPreviewTvContent.text = selectedNote.content

            this@PreviewFragment.selectedNote = listOf<Note>(selectedNote)
        }

        val scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())
        textView = binding.fragPreviewTvContent
        // Set up touch listener for ScrollView
        textView.setOnTouchListener { _, event ->
            // Handle touch events for scaling
            scaleGestureDetector.onTouchEvent(event)

            true
        }

    }

    private fun slideAnimator(view: View, start: Int, end: Int): ValueAnimator {
        return ValueAnimator.ofInt(start, end).apply {
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                view.layoutParams = view.layoutParams.apply { height = value }
            }
        }
    }


    /**
     * This function for custom popup animation windows for manu (top-right)
     *
     * @author hardik
     * @since 2024/08/02
     * @see PopupMenu
     */
    private var popupWindow: PopupWindow? = null
    private var dismissHandler: Handler? = null
    private val dismissRunnable = Runnable { dismissCustomPopupWindow() }
    private var pmBinding: CustomPopupWindowPreviewBinding? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showCustomPopupWindow(anchorView: View) {
        val inflater = requireContext().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.custom_popup_window_preview, binding.root, false)
        pmBinding = CustomPopupWindowPreviewBinding.bind(popupView)

        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                dismissHandler?.removeCallbacks(dismissRunnable)
            }
            showAsDropDown(anchorView, 0, 0, Gravity.BOTTOM)
        }

        // Handle clicks on the PopupWindow items
        pmBinding?.apply {
            llNewNote.setOnClickListener { dismissCustomPopupWindow(); mainActivity.switchToNotePadFragment() ; mainActivity.mainViewModel.updateNote(note = null); mainActivity.mainViewModel.setIsNewNote(isNewNote = Constants.NEW_NOTE)}
            llEditNote.setOnClickListener { dismissCustomPopupWindow(); mainActivity.switchToNotePadFragment(); mainActivity.mainViewModel.setIsNewNote(isNewNote = !Constants.NEW_NOTE)}
            llExport.setOnClickListener { dismissCustomPopupWindow(); mainActivity.requestPermissions(); exportFileAlertDialog() }
        }

        // Animate the appearance of the PopupWindow
        val popupContentView = popupWindow?.contentView
        popupContentView?.apply {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            layoutParams = layoutParams.apply { height = 0 }
            slideAnimator(this, 0, measuredHeight).start()
        }

        // Setup the handler to dismiss the PopupWindow after a delay
        dismissHandler?.removeCallbacks(dismissRunnable)
        dismissHandler = Handler(Looper.getMainLooper()).apply {
            postDelayed(dismissRunnable, AUTOMATIC_CLOSE_POPUP_MENU_WINDOW) // 5 seconds delay
        }
    }

    private fun dismissCustomPopupWindow() {
        pmBinding?.root?.let { popupView ->
            val initialHeight = popupView.height
            slideAnimator(popupView, initialHeight, 0).apply {
                interpolator = AccelerateDecelerateInterpolator()
                addListener(onEnd = {
                    (popupView.parent as? PopupWindow)?.dismiss()
                })
            }.start()
        }
        dismissHandler?.removeCallbacks(dismissRunnable)
    }


    /**
     * Export File alert dialog
     *
     * @author hardik
     * @since 2024/08/12
     */
    private fun exportFileAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_box_export_file, null)
        val adBinding = AlertDialogBoxExportFileBinding.bind(dialogView)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(adBinding.root)
        val alertDialog = builder.create()

        // handle click events
        alertDialog.apply {
            adBinding.llExportCsvFile.setOnClickListener {
                Toast.makeText(requireContext(), "Exporting...Csv File", Toast.LENGTH_SHORT).show()

                exportFile()

                dismiss()
            }
            adBinding.llExportTxtFile.setOnClickListener {
                Toast.makeText(requireContext(), "Exporting...Txt File", Toast.LENGTH_SHORT).show()

                exportFile(isCsvFile = false)

                dismiss()
            }
            adBinding.mbCancel.setOnClickListener {
                dismiss()
            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set your background drawable here
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    /**
     * Export file upon to totally user defined, call appropriate method.
     *
     * @author hardik
     * @since 2024/08/13
     */
    private fun exportFile(isCsvFile: Boolean = true){
        Log.d(tag, "exportFile: ")
        // Convert to JSON
        val json = notesToJson(notes = selectedNote)
        println(json)

        if (isCsvFile) {
            Log.d(tag, "exportFile: csv")
            checkPermissionBeforeCreateCsvFile(requireContext(), fileName = selectedNote[0].title, jsonData = json)
        }else{
            Log.d(tag, "exportFile: txt")
//            createTxtFile(context = requireContext().applicationContext, fileName = selectedNote[0].title, content = selectedNote[0].content)
            mainActivity.createTxtFile(fileName = selectedNote[0].title, content = selectedNote[0].content)
        }

    }

    /**
     * Check permissions before creating and exporting .csv file.
     *
     * @author hardik
     * @since 2024/08/13
     */
    private fun checkPermissionBeforeCreateCsvFile(context: Context, fileName: String, jsonData: String){
        Log.d(tag, "checkPermissionBeforeCreateCsvFile: ")
        // TODO: do your work here after permission granted
        if (!mainActivity.checkPermission()){
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            createCsvFile(context = context, fileName = fileName, jsonData = jsonData)
        }
    }









    /**
     * for zooming future on textView
     *
     * @author: hardik
     * @since: 2024/08/01
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var baseTextSize = 18f
        private var zoomedOutTextSize = baseTextSize / 1.5f
        private var zoomedInTextSize = baseTextSize * 12.0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Calculate the scale factor from the detector
            val scaleFactor = detector.scaleFactor

            // Calculate new text size based on the current scale factor
            val currentTextSize =
                textView.textSize / textView.context.resources.displayMetrics.scaledDensity
            val newSize = currentTextSize * scaleFactor

            // Limit the text size within a reasonable range
            val minTextSize = zoomedOutTextSize
            val maxTextSize = zoomedInTextSize
            val clampedSize = newSize.coerceIn(minTextSize, maxTextSize)

            // Set the new text size for the TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, clampedSize)

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            // Final adjustments if needed
            // For example, you might want to round the final textSize to avoid very small changes
            val finalTextSize =
                textView.textSize / textView.context.resources.displayMetrics.scaledDensity
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, finalTextSize)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.also {
            it.sivOpenDrawer.visibility = View.VISIBLE // drawer is now visible
            it.setDrawerEnabled(enabled = true) // drawer is enabled now
        }
        mainActivity.mainViewModel.updateNote(note = null)
    }
}