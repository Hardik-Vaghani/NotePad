package com.hardik.notepad.presentation

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.hardik.notepad.R
import com.hardik.notepad.adapter.IndexAdapter
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.common.Constants.LAUNCH_INSTANTLY
import com.hardik.notepad.common.FragmentSessionUtils
import com.hardik.notepad.common.UIOrientationUtils
import com.hardik.notepad.databinding.ActivityMainBinding
import com.hardik.notepad.databinding.AlertDialogBoxBinding
import com.hardik.notepad.presentation.home.HomeFragment
import com.hardik.notepad.presentation.note.NotePadFragment
import com.hardik.notepad.presentation.preview.PreviewFragment
import com.hardik.notepad.presentation.splash.SplashScreenFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val tag = BASE_TAG + MainActivity::class.java.simpleName

    val mainViewModel: MainViewModel by viewModels()

    val uiOrientationUtils: UIOrientationUtils? = UIOrientationUtils.instance
    val fragmentSessionUtils = FragmentSessionUtils.getInstance()

    lateinit var mainActivity: MainActivity
    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: View

    private lateinit var prefs: SharedPreferences

    val currentFragment =
        supportFragmentManager.findFragmentById(R.id.navDrawer_host_fragment)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawer: LinearLayout
    lateinit var sivOpenDrawer: ShapeableImageView

    private lateinit var mainContent: ConstraintLayout // Adjust this based on your main content type
    lateinit var indexAdapter: IndexAdapter

    private var drawerOpened = false
    private val maxSlideOffset = 0.50f // Adjust as needed
    private val maxScaleDown = 0.65f // Adjust as needed

    private val handler = Handler(Looper.getMainLooper())
    private val intervalMillis: Long = 1000 // 1 second
    private val intervalMillisSplash: Long = 1500 // 2 second

    private val myRunnable = object : Runnable {
        override fun run() {
            // Your code to execute repeatedly
            Log.e("TAG", "Continuously running task")

            // Schedule the next execution
            handler.postDelayed(this, intervalMillis)
        }
    }

    /**
    * Uou should request for permission with request code.
    *
    * @author hardik
    * @since 2024/08/12
    */
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        lateinit var READ_EXTERNAL_STORAGE :String
        private lateinit var WRITE_EXTERNAL_STORAGE :String

        private const val REQUEST_CODE_OPEN_DOCUMENT = 101
    }

    @SuppressLint("HardwareIds", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiOrientationUtils?.setFullScreenSystemUI(activity = this)
        mainActivity = this

        setPermissionAccordingToVersion()

        requestPermissions()



//        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
//        prefs = applicationContext.getSharedPreferences()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()

        drawerLayout = binding.includedDrawerLayout.drawerLayout
        mainContent = binding.includedDrawerLayout.mainContent
        drawer = binding.includedDrawerLayout.navigationDrawer
        sivOpenDrawer = binding.includedDrawerLayout.sivOpenDrawer
        progressBar = binding.includedDrawerLayout.includedProgressLayout.progressBar


        // load the default Fragment with data
        if (savedInstanceState == null) {
            switchToSplashScreenFragment()
        }

        val animDrawable =
            binding.includedDrawerLayout.drawerLayout.background as? AnimationDrawable

        animDrawable?.let {
            it.setEnterFadeDuration(750) // Set fade-in duration for the animation
            it.setExitFadeDuration(750)  // Set fade-out duration for the animation
            it.start()
        }

//        drawerLayout.setScrimColor(resources.getColor(android.R.color.white, theme))
//        drawerLayout.setScrimColor(Color.parseColor("#BBDEFB"))
        drawerLayout.setScrimColor(android.R.color.transparent)
        // Enable the Up button for a more complete drawer experience
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Create a DrawerToggle and set it as the DrawerListener
        drawerToggle = object : ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.drawer_open, R.string.drawer_close
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)

                // Animate scaleX and scaleY based on slideOffset
                val scaleX = 1 - (slideOffset * maxScaleDown)
                val scaleY = 1 - (slideOffset * maxScaleDown)
                animateScale(mainContent, scaleX, scaleY)

                // Animate translationX based on slideOffset
                val offsetX = slideOffset * drawerView.width * maxSlideOffset
                val offsetX1 =
                    if (uiOrientationUtils?.isOrientationLandscape(this@MainActivity) == true) offsetX else offsetX * 1.2f
                animateTranslationX(mainContent, offsetX1)

                // Optionally, change background based on slideOffset
                if (slideOffset > 0) {
                    mainContent.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.background_open
                    )
                    startRotationAnimationClockWise()
                } else {
                    mainContent.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.background_closed
                    )
                    startRotationAnimationAntiClockWise()
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

                uiOrientationUtils?.setOrientationLock(this@MainActivity) // Lock orientation

                drawerOpened = true

                // Fade out animation for hiding
                sivOpenDrawer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        sivOpenDrawer.visibility = View.GONE
                    }
                    .start()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)

                uiOrientationUtils?.setOrientationUnlock(this@MainActivity) // Unlock orientation

                drawerOpened = false

                // Make the view visible before starting the animation
                sivOpenDrawer.visibility = View.VISIBLE

                // Fade in animation for showing
                sivOpenDrawer.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
        }
        // Add drawerToggle as a drawer listener
        drawerLayout.addDrawerListener(drawerToggle)

        // Synchronize the state of the drawerToggle
        drawerToggle.syncState()


        // Set click listener on tvOpenDrawer to open the drawer
        sivOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Assuming you want to open from the start (left) side
        }

        // Search for drawer index on Adapter
        binding.includedDrawerLayout.searchView.apply {
            // Set query text listener
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // Handle query submission here
                    if (!query.isNullOrBlank()) {
                        // Perform search or filtering based on the query
                        indexAdapter.filter.filter(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // Handle query text changes here
                    indexAdapter.filter.filter(newText ?: "")
                    return true
                }
            })

            // Set close listener
            setOnCloseListener {
                // Handle close event here, reset any filters or clear search results
                indexAdapter.filter.filter("")
                false // Return true if you have consumed the event
            }
//            this.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).background = null
        }

        // Initialize OnBackPressedCallback
        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle back press event
                    Log.i(tag, "handleOnBackPressed: ")
                    fragmentSessionUtils.handleBackPressed(mainActivity)
                }
            }
        /// Add the callback to the back press dispatcher
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // get subject Index list | set data on recyclerview`s adapter here
        mainViewModel.subjectsOfNotes.observe(this) { subjectsOfNotes ->
            indexAdapter.submitList(subjectsOfNotes)
        }

        // When user click on index item, prospective list will be set in side '_subjectDataList' of IndexViewModel
        indexAdapter.setOnItemClickListener {
            // Handle item click event here
            Log.e("xyz", "onCreate: click $it", )
            mainViewModel.setSelectedSubject(subject = it)

            switchToHomeFragment()

            /**supportFragmentManager.popBackStack(
            SettingsFragment::class.java.simpleName,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
            )*/
        }
    }


    private fun setUpRecyclerView() {
        indexAdapter = IndexAdapter()
        binding.includedDrawerLayout.recyclerview.apply {
            adapter = indexAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.d(tag, "onPostCreate: ")
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(tag, "onConfigurationChanged: ")
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun animateScale(view: View, scaleX: Float, scaleY: Float) {
        Log.d(tag, "animateScale: ")
        view.animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .setDuration(0) // Set duration as needed
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    // Function to animate translationX
    private fun animateTranslationX(view: View, translationX: Float) {
        Log.d(tag, "animateTranslationX: ")
        view.animate()
            .translationX(
                translationX
//                if(UI_ORIENTATION_UTILS?.isOrientationPortrait(this) == true) translationX * 1.2f else translationX
            )//translationX*1.2f
            .setDuration(0) // Set duration as needed
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private var rotationAnimator: ObjectAnimator? = null

    // Drawer icon animation function
    private fun startRotationAnimationClockWise() {
        Log.d(tag, "startRotationAnimationClockWise: ")
        rotationAnimator?.resume()
        rotationAnimator = ObjectAnimator.ofFloat(sivOpenDrawer, View.ROTATION, 0f, 90f)
        rotationAnimator?.apply {
            duration = 200 // Adjust duration as needed
//            repeatCount = ObjectAnimator.INFINITE
            repeatCount = 0
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }
    }

    // Drawer icon animation function
    private fun startRotationAnimationAntiClockWise() {
        Log.d(tag, "startRotationAnimationAntiClockWise: ")
        rotationAnimator?.resume()
        rotationAnimator = ObjectAnimator.ofFloat(sivOpenDrawer, View.ROTATION, 90f, 0f)
        rotationAnimator?.apply {
            duration = 200 // Adjust duration as needed
            //repeatCount = ObjectAnimator.INFINITE
            repeatCount = 0
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }
    }

    fun setDrawerEnabled(enabled: Boolean) {
        if (enabled) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume: ")
            }


    /**
     * Set permission according to version.
     *
     * @author hardik
     * @since 2024/08/13
     */
    private fun setPermissionAccordingToVersion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_MEDIA_AUDIO
            WRITE_EXTERNAL_STORAGE = android.Manifest.permission.READ_MEDIA_IMAGES
        }else {
            READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
            WRITE_EXTERNAL_STORAGE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            else
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

    }

    /**
     * Request permission to any where.
     *
     * @author hardik
     * @since 2024/08/13
     */
    fun requestPermissions(){
        Log.d(tag, "requestPermissions: ")

        if (checkPermission()){
            Log.d(tag, "requestPermissions: Permission Already Granted")
//            Toast.makeText(this, "Permission Already Granted", Toast.LENGTH_SHORT).show()
//            createTxtFile(fileName = "fileName",content = "content") // call common function

        }else{
            Log.d(tag, "requestPermissions: Requesting for permission grant!!")
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),PERMISSION_REQUEST_CODE)

        }
    }

    /**
     * First you should check permission is granted or not.
     *
     * @author hardik
     * @since 2024/08/12
     */
    fun checkPermission():Boolean{
        val resultRead = ActivityCompat.checkSelfPermission(this@MainActivity, READ_EXTERNAL_STORAGE)
        val resultWrite = ActivityCompat.checkSelfPermission(this@MainActivity, WRITE_EXTERNAL_STORAGE)

        return resultRead == PackageManager.PERMISSION_GRANTED && resultWrite == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Once you requesting for permissions, you got result here.
     *
     * @author hardik
     * @since 2024/08/12
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE){
            Log.d(tag, "onRequestPermissionsResult: requestCode: $requestCode")
            if (grantResults.isNotEmpty()){
                Log.d(tag, "onRequestPermissionsResult: permissions: is not empty")

                val resultRead = grantResults[0]
                val resultWrite = grantResults[1]

                val checkRead = resultRead == PackageManager.PERMISSION_GRANTED
                val checkWrite = resultWrite == PackageManager.PERMISSION_GRANTED

                if (checkRead && checkWrite){
                    Log.d(tag, "onRequestPermissionsResult: Permission granted")
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//                    createTxtFile() // call common function // Call a function to proceed with operations that require permission

                }else{
                    Log.d(tag, "onRequestPermissionsResult: Permission denied")
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    // TODO: show message to user that permission denied and ask again
                    // Optionally, you can explain why the permissions are needed and ask the user to grant them again
//                    showPermissionExplanation()

                }
            }
        }
    }

    /**
    * User allow it's to create .txt file, Now you can do you work.
    *
    * @author hardik
    * @since 2024/08/12
    */
    fun createTxtFile(fileName: String, content: String){
        Log.d(tag, "createTxtFile: ")
        if (!checkPermission()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveTxtFileToMediaStore(this, fileName = fileName, content = content)
        } else {
            createTxtFileInCustomDir(fileName = fileName, content = content)
        }
    }

    /**
     * Android 9 (API 28, Version code Pie) and below, use app-specific directories on external storage and create file.
     *
     * @author hardik
     * @since 2024/08/12
     */
    private fun createTxtFileInCustomDir(fileName: String, content: String) {
        Log.d(tag, "createFileInCustomDir: ")
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // Define your custom path
            val customDir = File(Environment.getExternalStorageDirectory(), "PermissionDemo/files")
            if (!customDir.exists()) {
                customDir.mkdirs() // Create directory if it doesn't exist
            }

            // Ensure the file name has a .txt extension
            val txtFileName = if (fileName.endsWith(".txt", ignoreCase = true)) {
                fileName
            } else {
                "$fileName.txt"
            }


            // Define the file path
            val file = File(customDir, txtFileName)
            Log.d(tag, "createFileInCustomDir: filePath: ${file.absolutePath}")
            try {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(tag, "createFileInCustomDir: IOException:",e.fillInStackTrace() )
            }

        } else {
            // Handle the case where external storage is not available
            Log.e(tag, "External storage is not available.")
        }
    }

    /**
     * Android 10 (API 29, Version code Q) and above, use app-specific directories on external storage and create file.
     *
     * @author hardik
     * @since 2024/08/12
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveTxtFileToMediaStore(context: Context, fileName: String, content: String) {
        Log.d(tag, "saveFileToMediaStore: ")

        // Ensure the file name has a .txt extension
        val txtFileName = if (fileName.endsWith(".txt", ignoreCase = true)) {
            fileName
        } else {
            "$fileName.txt"
        }

        // Define the directory within public storage
        val relativePath = "Documents/PermissionDemo/files" // or use another valid directory

        // Create a ContentValues object to define the file's metadata
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, txtFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        try {
            // Insert the file into MediaStore
            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let {
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    if (outputStream != null) {
                        outputStream.write(content.toByteArray())
                        outputStream.flush()
                        Log.d(tag, "File saved successfully to MediaStore: $uri")
                    } else {
                        Log.e(tag, "Failed to open output stream for URI: $uri")
                    }
                }

                // Retrieve file details using the URI
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val pathIndex = c.getColumnIndex(MediaStore.MediaColumns.DATA)
                        val path = c.getString(pathIndex)
                        Log.d("FileLocation", "File path: $path")
                    } else {
                        Log.e(tag, "Failed to retrieve file details from URI: $uri")
                    }
                }
            } ?: run {
                Log.e(tag, "Failed to insert file into MediaStore.")
            }
        } catch (e: IOException) {
            Log.e(tag, "I/O error while saving file to MediaStore: ${e.message}")
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error: ${e.message}")
        }
    }












    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy: ")
    }

    /**Navigation function*/
    // The first navigation screen is splash screen
    private fun switchToSplashScreenFragment() {
        Log.d(tag, "switchToSplashScreenFragment: ")
        if (currentFragment !is SplashScreenFragment) {
            fragmentSessionUtils.switchFragment(
                supportFragmentManager,
                SplashScreenFragment(),
                false,
            )
            setSplashScreenUIAutomatic()//set splash screen functionality
        }
    }

    // before and after splash screen functionality
    private fun setSplashScreenUIAutomatic() {
        Log.d(tag, "setSplashScreenUIAutomatic: ")
        uiOrientationUtils?.setFullScreenSystemUI(activity = this)//set full screen
        sivOpenDrawer.visibility = View.GONE
        setDrawerEnabled(false)// drawer can not be opened

        Handler(Looper.getMainLooper()).run {
            postDelayed({

                sivOpenDrawer.visibility = View.VISIBLE
                drawerLayout.closeDrawer(GravityCompat.START)
                setDrawerEnabled(true)// drawer can be opened

                uiOrientationUtils?.setNormalScreenSystemUI(activity = this@MainActivity)//set orientation ui system you want

            }, intervalMillisSplash)
        }
    }

    // for default home screen with all notes list
    fun switchToHomeFragment(launchInstantly: Boolean = LAUNCH_INSTANTLY) {
        Log.d(tag, "switchToHomeFragment: ")

        Handler(Looper.getMainLooper()).run {
            postDelayed({
                if (currentFragment !is HomeFragment) {
                    fragmentSessionUtils.switchFragment(
                        supportFragmentManager,
                        HomeFragment(),
                        false,
                    )
                }
            }, if (launchInstantly) 0 else intervalMillisSplash)
        }
    }

    // for showing the existing notes
    fun switchToPreviewFragment() {
        Log.d(tag, "switchToPreviewFragment: ")

        Handler(Looper.getMainLooper()).run {
            postDelayed({
                if (currentFragment !is PreviewFragment) {
                    fragmentSessionUtils.switchFragment(
                        supportFragmentManager,
                        PreviewFragment(),
                        true,
                    )
                }
            }, 0)
        }
    }

    // for editing & Inserting notes
    fun switchToNotePadFragment() {
        Log.d(tag, "switchToNotepadFragment: ")

        Handler(Looper.getMainLooper()).run {
            postDelayed({
                if (currentFragment !is NotePadFragment) {
                    fragmentSessionUtils.switchFragment(
                        supportFragmentManager,
                        NotePadFragment(),
                        true,
                    )
                }
            }, 0)
        }

    }

    private var adBinding: AlertDialogBoxBinding? = null

    @SuppressLint("SetTextI18n")
    fun showCustomAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_box, null)
        adBinding = AlertDialogBoxBinding.bind(dialogView)

//        val dialog = Dialog(requireContext())
//        dialog.setContentView(adBinding.root)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        //dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent) // Set your background drawable here
//        dialog.show()

        val builder = AlertDialog.Builder(this)
        builder.setView(adBinding!!.root)
        val alertDialog = builder.create()
//        alertDialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent) // Set your background drawable here
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set your background drawable here
        alertDialog.setCancelable(true)
        alertDialog.show()

    }































    // Create a folder and write a file in external storage
    private fun createFolderAndWriteFile() {

        val directory = File(getExternalFilesDir(null), "MyFolder")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "example.txt")
        try {
            FileOutputStream(file).use { fos ->
                val data = "Hello, World!"
                fos.write(data.toByteArray())
            }
            Toast.makeText(this, "File written successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to write file", Toast.LENGTH_SHORT).show()
        }
    }

    // Read and display content from a file
    private fun readFile() {

        val directory = File(getExternalFilesDir(null), "MyFolder")
        val file = File(directory, "example.txt")
        if (file.exists()) {
            try {
                val content = file.bufferedReader().use { it.readText() }
                Toast.makeText(this, "File content: $content", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    // Read and parse CSV file
    private fun readCsvFile() {
        val directory = File(getExternalFilesDir(null), "MyFolder")
        val file = File(directory, "data.csv")
        if (file.exists()) {
            try {
                val reader = file.bufferedReader()
                val lines = reader.readLines()
                for (line in lines) {
                    val values = line.split(",") // Assuming CSV is comma-separated
                    // Process values
                    println(values)
                }
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to read CSV file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "CSV file does not exist", Toast.LENGTH_SHORT).show()
        }
    }

}

//        fragmentSessionUtils.switchFragment(
//            mainActivity.supportFragmentManager,
//            IntroScreenFragment(),
//            false,// if you want to track history fragments than set 'ture'
//        )
//override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        hideHandler.run {
//            postDelayed({
////                mainActivity.fragmentSessionUtils.switchFragment(mainActivity.supportFragmentManager, HomeFragment(), false, navHostContainer = R.id.nav_host_fragment_drawer_layout)// if you want to track history fragments than set 'ture')
////                mainActivity.fragmentSessionUtils.switchFragment(mainActivity.supportFragmentManager, IntroScreenFragment(), false)// if you want to track history fragments than set 'ture')
//                val i = Intent(firstActivity, MainActivity::class.java)
//                startActivity(i)
//                firstActivity.finish()
//            }, AUTO_DELAY_MILLIS.toLong())
//        }
//    }
//
//private val hideHandler = Handler(Looper.myLooper()!!)
//private const val AUTO_DELAY_MILLIS = 3000