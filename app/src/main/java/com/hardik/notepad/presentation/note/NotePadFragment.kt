package com.hardik.notepad.presentation.note

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hardik.notepad.R
import com.hardik.notepad.common.Constants
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.common.TimeHandler
import com.hardik.notepad.databinding.AlertDialogBoxUpsertNoteBinding
import com.hardik.notepad.databinding.FragmentNotePadBinding
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotePadFragment : Fragment() {

    private val tag = BASE_TAG + NotePadFragment::class.java.simpleName

    private val noteViewModel: NoteViewModel by viewModels()

    private var _binding: FragmentNotePadBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private var note: Note = Note()
    private var isNewNote :Boolean? = null

    // Define the list of subjects
    private var subjects = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        return inflater.inflate(R.layout.fragment_note_pad, container, false)
        _binding = FragmentNotePadBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        // save button
        binding.includedToolbarLayout.toolbarLayoutIBtnMenu.apply {
            setImageResource(R.drawable.baseline_save_24)
            setOnClickListener {

                // open dialog for title & subject(optional) then save
                upsertNoteAlertDialog()

//            activity.switchToHomeFragment()
                CoroutineScope(Dispatchers.Main).launch {
//                    noteViewModel.addNote(
//                        Note(
//                            id = "4",
//                            subject = "Subject3",
//                            title = "Title1",
//                            content = "Description",
//                        )
//                    )
                }
            }
        }

        // this list for automatically suggested for subject
        mainActivity.mainViewModel.subjectsOfNotes.observe(viewLifecycleOwner) {
            subjects = it
        }

        // check for update or new note
        mainActivity.mainViewModel.isNewNote.observe(viewLifecycleOwner) { isNewNote = it
            // title text (toolbar)
            Log.e(tag, "onViewCreated: ${if (it) "New Note" else "Edit Note"}")
            binding.includedToolbarLayout.toolbarLayoutTvTitle.text = getString(if (isNewNote == Constants.NEW_NOTE) R.string.new_note else R.string.update_note)
        }

        // if update note, the UI with note details set
        mainActivity.mainViewModel.updateNote.observe(viewLifecycleOwner) { it ->
            Log.e(tag, "onViewCreated: $it", )
            if (isNewNote != Constants.NEW_NOTE) {// it's false for below action
                it?.let { note = it }
                binding.fragNotePadEdtContent.setText(note.content.takeIf { it.isNotEmpty() } ?: "")
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mainActivity.also {
            it.sivOpenDrawer.visibility = View.GONE // drawer icon is not visible now
            it.setDrawerEnabled(enabled = false) // now drawer is not open yet.
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mainActivity.also {
            it.sivOpenDrawer.visibility = View.VISIBLE // drawer is now visible
            it.setDrawerEnabled(enabled = true) // drawer is enabled now
        }
    }

    /** Upsert Note alert dialog */
    private fun upsertNoteAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_box_upsert_note, null)
        val adBinding = AlertDialogBoxUpsertNoteBinding.bind(dialogView)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(adBinding.root)
        val alertDialog = builder.create()


        // Create an ArrayAdapter with the list of subjects
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
//        val autoCompleteTextView: AutoCompleteTextView = adBinding.autoCompleteTextViewNoteSubject   // Find the AutoCompleteTextView by its ID
//        autoCompleteTextView.setAdapter(adapter) // Set the adapter to the AutoCompleteTextView

        // handle click events
        adBinding.also { it ->
            it.autoCompleteTextViewNoteSubject.setAdapter(adapter)// set the adapter to the AutoCompleteTextView
            if (isNewNote != Constants.NEW_NOTE) {//when updating show old data
                it.tInEdtNoteTitle.setText(note.title.takeIf { it.isNotEmpty() } ?: "")
                it.autoCompleteTextViewNoteSubject.setText(note.subject.takeIf { it.isNotEmpty() } ?: "")
            }

            it.mbBack.setOnClickListener { alertDialog.dismiss() }
            it.mbSave.setOnClickListener { v ->

                CoroutineScope(Dispatchers.IO).launch {
                    val timeStamp = async { return@async TimeHandler().getCurrentDateTimeString() }.await()
                    val idTimeStamp = async { return@async TimeHandler().getCurrentYearDateTimeString() }.await()

                    async {
                        if (isNewNote == Constants.NEW_NOTE) { // for new note enter

                            launch(Dispatchers.Main) {
                                note.apply {// when note null
    //                                id = adBinding.tInEdtNoteTitle.text?.toString()?.takeIf { it.isNotEmpty() } ?: "Invalid id"
                                    id = (adBinding.tInEdtNoteTitle.text?.toString()?.takeIf { it.isNotEmpty() } ?: "Invalid id")+(idTimeStamp.takeIf { it.isNotEmpty() } ?: "")
                                    title = adBinding.tInEdtNoteTitle.text?.toString()?.takeIf { it.isNotEmpty() } ?: "Untitled"
                                    subject = adBinding.autoCompleteTextViewNoteSubject.text?.toString()?.takeIf { it.isNotEmpty() } ?: "Unknown subject"
                                    content = binding.fragNotePadEdtContent.text?.toString()?.takeIf { it.isNotEmpty() } ?: ""
                                    created_time = timeStamp.takeIf { it.isNotEmpty() } ?: ""
                                    updated_time = timeStamp.takeIf { it.isNotEmpty() } ?: ""
                                }

                                println("upsertNoteAlertDialog: new note:\n$note\n")
                                noteViewModel.addNote(note = note) }.join()

                        }else{ // for older note update

                            launch(Dispatchers.Main) {
                                println("upsertNoteAlertDialog: going to update this note:\n$note\n")

                                noteViewModel.upsertNote(
                                    id= note.id,// when update use only id existing note
                                    newTitle = adBinding.tInEdtNoteTitle.text?.toString()?.takeIf { it.isNotEmpty() }?: "Untitled",
                                    newSubject = adBinding.autoCompleteTextViewNoteSubject.text?.toString()?.takeIf { it.isNotEmpty() }?: "Unknown subject",
                                    newContent = binding.fragNotePadEdtContent.text?.toString()?.takeIf { it.isNotEmpty() } ?: "",
                                    createdTime = note.created_time,
                                    updatedTime = timeStamp.takeIf { it.isNotEmpty() } ?: "",
                                )
                            }.join()

                        }
                        launch(Dispatchers.Main) {
                            mainActivity.fragmentSessionUtils.handleBackPressed(requireActivity())
                            alertDialog.dismiss()
                        }.join()
                    }.await()
                }

            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set your background drawable here
        alertDialog.setCancelable(false)
        alertDialog.show()

    }
}
//                        val a = if(it.autoCompleteTextViewNoteSubject.text.isNullOrEmpty()) it.autoCompleteTextViewNoteSubject.text.toString() else "Unknown"
//                        val a = it.autoCompleteTextViewNoteSubject.text?.toString()?.takeIf { it.isNotEmpty() } ?: "Unknown"