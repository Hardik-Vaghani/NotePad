package com.hardik.notepad.presentation.home

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hardik.notepad.R
import com.hardik.notepad.adapter.IndexHomeAdapter
import com.hardik.notepad.common.Constants
import com.hardik.notepad.common.Constants.AUTOMATIC_CLOSE_POPUP_MENU_WINDOW
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.databinding.AlertDialogBoxExportBinding
import com.hardik.notepad.databinding.CustomPopupWindowHomeBinding
import com.hardik.notepad.databinding.FragmentHomeBinding
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.model.notesToJson
import com.hardik.notepad.extra_class.createCsvFile
import com.hardik.notepad.extra_class.readFile
import com.hardik.notepad.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val tag = BASE_TAG + HomeFragment::class.java.simpleName

    private val homeViewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity
    private lateinit var indexHomeAdapter: IndexHomeAdapter
    private lateinit var currentNoteList:List<Note> // for export specific type of notes
    private lateinit var indexNoteList:List<Note> // for export all notes

//    private lateinit var indexViewModel: IndexViewModel

    private var toolbarTitle:String = "All"
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>

    lateinit var progressBar: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
//        return inflater.inflate(R.layout.fragment_home, container, false)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.fragHomeFabNewNote.setOnClickListener {
            mainActivity.also { it ->
                it.switchToNotePadFragment()
                it.mainViewModel.setIsNewNote(isNewNote = Constants.NEW_NOTE)
                it.mainViewModel.updateNote(note = null)
            }
        }

        /** toolbar menu button */
        binding.includedToolbarLayout.toolbarLayoutIBtnMenu.setOnClickListener {

            showCustomPopupWindow(it)

        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        indexViewModel = ViewModelProvider(this).get(IndexViewModel::class.java)

        setupPickFileFromStorage()

        setUpRecyclerView()

//        if (!::indexHomeAdapter.isInitialized) {
//            indexHomeAdapter = IndexHomeAdapter()
//        }
        homeViewModel.notes.observe(viewLifecycleOwner){notes ->
            if (!notes.isNullOrEmpty()) {
                indexNoteList = notes
//                notes.forEach { Log.d(tag, "onViewCreated: All notes:\n$it") }
            }
        }

        mainActivity.mainViewModel.selectedSubject.observe(viewLifecycleOwner) { selectedSubject ->
//            Log.e(tag, "onViewCreated: $selectedSubject")
            if (!selectedSubject.isNullOrEmpty()) {
                toolbarTitle = selectedSubject
                binding.includedToolbarLayout.toolbarLayoutTvTitle.text = toolbarTitle
                homeViewModel.setNotesOfSameSubject(subject = selectedSubject)
            }
        }

        // for home index data
        homeViewModel.notesOfSameSubject.observe(viewLifecycleOwner){ notes->
            if (!notes.isNullOrEmpty()){
                currentNoteList = notes
//                indexHomeAdapter.submitList(currentNoteList)
                notes.forEach { Log.d(tag, "onViewCreated: specific notes:\n$it") }
            }
        }



        indexHomeAdapter.setOnItemClickListener {
            // Handle item click event here
            mainActivity.mainViewModel.setSelectedNote(note = it)
            mainActivity.switchToPreviewFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissCustomPopupWindow() // Ensure the PopupWindow is dismissed
    }

    private fun setUpRecyclerView() {
        indexHomeAdapter = IndexHomeAdapter()
        binding.recyclerview.apply {
            adapter = indexHomeAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(mainActivity)
            setHasFixedSize(true)
        }
    }


    /**
     * This function for custom Popup animation windows for manu (top-right)
     *
     * @author hardik
     * @since 2024/08/02
     * @see PopupMenu
     */
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
    private var pmBinding: CustomPopupWindowHomeBinding? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showCustomPopupWindow(anchorView: View) {
        val inflater =
            requireContext().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.custom_popup_window_home, binding.root, false)
        pmBinding = CustomPopupWindowHomeBinding.bind(popupView)

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
            llNewNote.setOnClickListener { dismissCustomPopupWindow(); mainActivity.also { it.switchToNotePadFragment(); it.mainViewModel.setIsNewNote(isNewNote = Constants.NEW_NOTE); it.mainViewModel.updateNote(note = null); } }
            llExport.setOnClickListener { dismissCustomPopupWindow(); exportAlertDialog() }
            llImportFile.setOnClickListener { dismissCustomPopupWindow(); importFileFromStorage()}
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
        popupWindow?.takeIf { it.isShowing }?.let {popup->

            pmBinding?.root?.let { popupView ->
                val initialHeight = popupView.height
                slideAnimator(popupView, initialHeight, 0).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    addListener(onEnd = {
//                        (popupView.parent as? PopupWindow)?.dismiss()
                        popup.dismiss() // Directly call dismiss on the PopupWindow
                        popupWindow = null // clear reference dismissal
                    })
                }.start()
            }
            dismissHandler?.removeCallbacks(dismissRunnable)
        }
    }

    /**
     * Setup for Pick file from storage, with modern approach
     *
     * @author hardik - This method is also call inside Fragment->onViewCreated() or Activity->onCreate()
     * @since 2024/08/14
     */
    private fun setupPickFileFromStorage(){
        /** Initialize the ActivityResultLauncher for permission requests */
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, now pick a file
                openDocumentLauncher.launch(arrayOf("*/*"))
            } else {
                // Permission denied, handle appropriately
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        /** Initialize the ActivityResultLauncher for picking documents */
        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                CoroutineScope(Dispatchers.IO).launch {

                    val notes: MutableList<Note> = async { return@async readFile(requireContext(),it)}.await()
                    async { homeViewModel.addNotes(notes = notes) }.await()
                    notes.forEach { Log.e(tag, "importFileFromStorage: $it") }
                    async (Dispatchers.Main){
                        // here set index list and home update after import data
//                        mainActivity.mainViewModel.getAllNotes()
//                        mainActivity.mainViewModel.notes.observe(viewLifecycleOwner){notesList ->
//                            if (notesList.isNotEmpty()){
//                                Log.d(tag, "setupPickFileFromStorage: $notesList")
//                                mainActivity.indexAdapter.submitList(notesList.map { it.subject }.distinct())
//                            }
//                        }
//                        homeViewModel.setNotesOfSameSubject(subject = "All")
//                        mainActivity.switchToHomeFragment()
                    }.await()
                }
            }
        }
    }

    /**
     * Import file from storage
     *
     * @author hardik
     * @since 2024/08/14
     */
    private fun importFileFromStorage() {
        CoroutineScope(Dispatchers.IO).launch {
            async {
                pickFileForReadModernApproach()
            }.await()
        }
    }

    /**
     * Pick file from storage, with modern approach
     *
     * @author hardik - requestPermissionLauncher.launch(MainActivity.READ_EXTERNAL_STORAGE)
     * - Permission according to version
     * @since 2024/08/14
     */
    private fun pickFileForReadModernApproach() {
        // Request permission before picking a file
        requestPermissionLauncher.launch(MainActivity.READ_EXTERNAL_STORAGE)
    }


    /**
     * Export File alert dialog
     *
     * @author hardik
     * @since 2024/08/12
     */
    private fun exportAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_box_export, null)
        val adBinding = AlertDialogBoxExportBinding.bind(dialogView)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(adBinding.root)
        val alertDialog = builder.create()

        // handle click events
        alertDialog.apply {
            adBinding.llExportWholeList.setOnClickListener {
                Toast.makeText(requireContext(), "Exporting...list", Toast.LENGTH_SHORT).show()
                mainActivity.requestPermissions()

                val fileName = binding.includedToolbarLayout.toolbarLayoutTvTitle.text.toString()

                // get export list (notes of which same subject)
                exportFile(notes = currentNoteList, fileName = fileName)

                dismiss()
            }
            adBinding.llExportIndexList.setOnClickListener {
                Toast.makeText(requireContext(), "Exporting...Index list", Toast.LENGTH_SHORT).show()
                mainActivity.requestPermissions()

                // get export index list (all notes)
                exportFile(notes = indexNoteList)

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
     * Export file.
     *
     * @author hardik
     * @since 2024/08/13
     */
    private fun exportFile(notes :List<Note>, fileName: String = resources.getString(R.string.app_name)){
        // Convert to JSON
        val json = notesToJson(notes = notes)
        println(json)

        checkPermissionBeforeCreateCsvFile(requireContext(), fileName = fileName, jsonData = json)

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






}