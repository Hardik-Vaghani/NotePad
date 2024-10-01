package com.hardik.notepad.extra_class

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.hardik.notepad.common.Constants
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.common.TimeHandler
import com.hardik.notepad.domain.model.Note
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringWriter

/**
 * This function creating .csv file, version differentiation code
 *
 * @author hardik
 * @since 2024/08/13
 */
suspend fun createCsvFile(context: Context,fileName: String, jsonData: String){
    withContext(Dispatchers.IO) {
        val timeStamp :String = async { return@async TimeHandler().getCurrentDateTimeString() }.await()

        // Ensure the file name has append time stamp
        val fileNameTimeStamp = if (fileName.endsWith(timeStamp, ignoreCase = true)) {
            fileName
        } else {
            "$fileName$timeStamp"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveJsonToCsvInMediaStore(context, fileName=fileNameTimeStamp, jsonData)
        } else {
            createCsvFileInCustomDir(fileName= fileNameTimeStamp, jsonData)
        }
    }
}


/**
 * Android 9 (API 28, Version code Pie) and below,
 * Use app-specific directories on external storage and create .csv file.
 *
 * @author hardik
 * @since 2024/08/13
 */
fun createCsvFileInCustomDir(fileName: String, jsonData: String) {
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        // Define your custom path
        val customDir = File(Environment.getExternalStorageDirectory(), "PermissionDemo/files")
        if (!customDir.exists()) {
            customDir.mkdirs() // Create directory if it doesn't exist
        }

        // Ensure the file name has a .csv extension
        val csvFileName = if (fileName.endsWith(".csv", ignoreCase = true)) {
            fileName
        } else {
            "$fileName.csv"
        }

        // Define the file path
        val file = File(customDir, csvFileName)
        Log.d(Constants.BASE_TAG+"FileCreation", "createCsvFileInCustomDir: filePath: ${file.absolutePath}")

        // Convert JSON data to CSV format
        val csvData = convertJsonToCsv(jsonData)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(csvData.toByteArray())
                Log.d(Constants.BASE_TAG+"FileCreation", "CSV file saved successfully to: ${file.absolutePath}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(Constants.BASE_TAG+"FileCreation", "IOException while saving CSV file:", e)
        }
    } else {
        // Handle the case where external storage is not available
        Log.e(Constants.BASE_TAG+"FileCreation", "External storage is not available.")
    }
}


/**
 * Android 10 (API 29, Version code Q) and above,
 * Use app-specific directories on external storage and create .csv file.
 *
 * @author hardik
 * @since 2024/08/13
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun saveJsonToCsvInMediaStore(context: Context, fileName: String, jsonData: String) {

    // Ensure the file name has a .csv extension
    val csvFileName = if (fileName.endsWith(".csv", ignoreCase = true)) {
        fileName
    } else {
        "$fileName.csv"
    }

    // Define the directory within public storage
    val relativePath = "Documents/PermissionDemo/files" // or use another valid directory

    // Convert JSON data to CSV
    val csvData = convertJsonToCsv(jsonData)

    // Create a ContentValues object to define the file's metadata
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, csvFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
    }

    try {
        // Insert the file into MediaStore
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            context.contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    outputStream.write(csvData.toByteArray())
                    outputStream.flush()
                    Log.d(Constants.BASE_TAG+"FileSave", "CSV file saved successfully to MediaStore: $uri")
                } else {
                    Log.e(Constants.BASE_TAG+"FileSave", "Failed to open output stream for URI: $uri")
                }
            }

            // Retrieve file details using the URI
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val pathIndex = c.getColumnIndex(MediaStore.MediaColumns.DATA)
                    val path = c.getString(pathIndex)
                    Log.d(Constants.BASE_TAG+"FileLocation", "File path: $path")
                } else {
                    Log.e(Constants.BASE_TAG+"FileSave", "Failed to retrieve file details from URI: $uri")
                }
            }
        } ?: run {
            Log.e(Constants.BASE_TAG+"FileSave", "Failed to insert file into MediaStore.")
        }
    } catch (e: IOException) {
        Log.e(Constants.BASE_TAG+"FileSave", "I/O error while saving file to MediaStore: ${e.message}")
    } catch (e: Exception) {
        Log.e(Constants.BASE_TAG+"FileSave", "Unexpected error: ${e.message}")
    }
}


/**
 * It's required json-data in string format and provide .csv string data.
 *
 * @author hardik
 * @since 2024/08/13
 */
fun convertJsonToCsv(jsonData: String): String {
    val jsonArray = JSONArray(jsonData)
    val stringWriter = StringWriter()
    val csvWriter = CSVWriter(stringWriter)

    // Extract header from JSON keys
    val header = jsonArray.getJSONObject(0).keys().asSequence().toList().toTypedArray()
    csvWriter.writeNext(header)

    // Write JSON data to CSV
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val row = header.map { jsonObject.optString(it) }.toTypedArray()
        csvWriter.writeNext(row)
    }

    csvWriter.close()
    return stringWriter.toString()
}



/**
 * Read file from uri
 *
 * @author hardik
 * @since 2024/08/14
 */
suspend fun readFile(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
    Log.d(BASE_TAG, "readFile: ")
    val timeStamp = async { return@async TimeHandler().getCurrentDateTimeString() }.await()
    val notes = mutableListOf<Note>(elements = emptyArray())

    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            //BufferedReader(InputStreamReader(inputStream)).use { reader ->
            //                val csvReader = CSVReader(reader)
            InputStreamReader(inputStream).use { inputStreamReader ->
                val csvReader = CSVReader(inputStreamReader)
                val allRows = csvReader.readAll()

                if (allRows.isNotEmpty()) {
                    val headers = allRows[0]
                    for (row in allRows.drop(1)) {
                        val note = Note(
                            id = row[headers.indexOf("id")],
                            title = row[headers.indexOf("title")],
                            subject = row[headers.indexOf("subject")],
                            content = row[headers.indexOf("content")],
                            created_time = row[headers.indexOf("created_time")],
//                            updated_time = row[headers.indexOf("updated_time")]
                            updated_time = timeStamp.takeIf { it.isNotEmpty() } ?: ""
                        )
                        // Save the note to Realm
                        Log.d(BASE_TAG, "readFile: $note")
                        notes.add(note)
                    }
                } else {
                    Log.d(BASE_TAG, "No data found in CSV")
                }
            }
        }
    } catch (e: Exception) {
        Log.e(BASE_TAG + "FileAccess", "Error reading file", e)
    }
    notes
}


//                var line: String?
//                while (reader.readLine().also { line = it } != null) { Log.d(BASE_TAG+"FileContent", line ?: "") }

