package com.hardik.notepad.common


import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// Kotlin class handling time and date
class TimeHandler {

    // Private method to get the current date and time
    private fun getCurrentDateTime(): Date {
        return Date()
    }

    // Method to get date in "yyyy-MM-dd" format
    fun getCurrentDate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26 and above
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDateTime = LocalDateTime.now()
            formatter.format(currentDateTime.toLocalDate())
        } else {
            // For below API 26
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(getCurrentDateTime())
        }
    }

    // Method to get time in "HH:mm:ss" format
    fun getCurrentTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26 and above
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val currentDateTime = LocalDateTime.now()
            formatter.format(currentDateTime.toLocalTime())
        } else {
            // For below API 26
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            formatter.format(getCurrentDateTime())
        }
    }

    // Method to get the current year in "yyyy" format
    fun getCurrentYear(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26 and above
            val formatter = DateTimeFormatter.ofPattern("yyyy")
            val currentDateTime = LocalDateTime.now()
            formatter.format(currentDateTime.toLocalDate())
        } else {
            // For below API 26
            val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
            formatter.format(getCurrentDateTime())
        }
    }

    // Method to get date and time in "yyyy-MM-dd HH:mm:ss" format
    fun getCurrentDateTimeString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26 and above
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val currentDateTime = LocalDateTime.now()
            formatter.format(currentDateTime)
        } else {
            // For below API 26
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatter.format(getCurrentDateTime())
        }
    }

    // Method to get date and time in "yyyy-MM-dd HH:mm:ss" format
    fun getCurrentYearDateTimeString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26 and above
            val formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm")
            val currentDateTime = LocalDateTime.now()
            formatter.format(currentDateTime)
        } else {
            // For below API 26
            val formatter = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault())
            formatter.format(getCurrentDateTime())
        }
    }
}

//fun main() {
//    val timeHandler = TimeHandler()
//
//    println("Current Date: ${timeHandler.getCurrentDate()}")
//    println("Current Time: ${timeHandler.getCurrentTime()}")
//    println("Current Year: ${timeHandler.getCurrentYear()}")
//    println("Current Date and Time: ${timeHandler.getCurrentDateTimeString()}")
//}
