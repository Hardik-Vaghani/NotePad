package com.hardik.notepad.domain.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.hardik.notepad.common.Constants.BASE_TAG
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

// Define your data model
open class Note(
    @PrimaryKey
    var id: String = "Invalid id",
    @Required
    @Index
    var title: String = "Untitled",
    @Required
    @Index
    var subject: String = "Unknown subject",
    @Required
    var content: String = "",
//    var isPin: Boolean = false,
    var created_time: String = "",
    var updated_time: String = "",
//    var device_id: String = ""
) : RealmObject() {
    override fun toString(): String {
        return "Note(id='$id', title='$title', subject='$subject', content='$content', created_time='$created_time', updated_time='$updated_time')"
    }
}



/**
 * Define a data class for serialization.
 *
 * @author hardik
 * @since 2024/08/12
 */
data class NoteData(
    val id: String,
    val title: String,
    val subject: String,
    val content: String,
//    val isPin: Boolean,
    val created_time: String,
    val updated_time: String
)


//
/**
 * Extension function to convert Note to NoteData.
 *
 * @author hardik
 * @since 2024/08/12
 */
fun Note.toNoteData(): NoteData {
    return NoteData(
        id = this.id,
        title = this.title,
        subject = this.subject,
        content = this.content,
//        isPin = this.isPin,
        created_time = this.created_time,
        updated_time = this.updated_time
    )
}

/**
 * Function to serialize Note to JSON to String.
 *
 * @author hardik
 * @since 2024/08/12
 */
fun noteToJson(note: Note): String {
    val noteData = note.toNoteData()
//    val gson = Gson()
    val gson = GsonBuilder().registerTypeAdapter(NoteData::class.java, NoteDataSerializer()).create()

    // TypeToken is used to handle the serialization of a list
    val type = object : TypeToken<NoteData>() {}.type
    val jsonString = gson.toJson(noteData, type)
    Log.w(BASE_TAG, "notesToJson: $jsonString", )
    return jsonString
}


/**
 * Function to convert a list of Note to JSON to String.
 *
 * @author hardik
 * @since 2024/08/12
 */
fun notesToJson(notes: List<Note>): String {
    val noteDataList = notes.map { it.toNoteData() }
//    val gson = Gson()
    val gson = GsonBuilder().registerTypeAdapter(NoteData::class.java, NoteDataSerializer()).create()
    // TypeToken is used to handle the serialization of a list
    val type = object : TypeToken<List<NoteData>>() {}.type
    val jsonString = gson.toJson(noteDataList, type)
    Log.w(BASE_TAG, "notesToJson: $jsonString", )
    return jsonString
}

/**
 * # Custom Serialization with Gson
 * You can create a custom serializer for your NoteData class to control the order of the fields explicitly.
 *
 * @author hardik
 * @since 2024/08/14
 */
class NoteDataSerializer : JsonSerializer<NoteData> {
    override fun serialize(
        src: NoteData,
        typeOfSrc: java.lang.reflect.Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", src.id)
        jsonObject.addProperty("title", src.title)
        jsonObject.addProperty("subject", src.subject)
        jsonObject.addProperty("content", src.content)
        jsonObject.addProperty("created_time", src.created_time)
        jsonObject.addProperty("updated_time", src.updated_time)
        return jsonObject
    }
}


/**
 * # Deserialize the JSON
 * Convert your JSON string into a list of NoteData objects.
 *
 * @author hardik
 * @since 2024/08/14
 */
fun parseJsonToNotes(jsonString: String): List<NoteData> {
    //like """[{"id":"aaa","title":"aaa","subject":"agag","content":"hi there I\u0027m a good time","created_time":"2024-08-14 10:30:01","updated_time":"2024-08-14 10:30:01"}]"""
    val gson = Gson()
    val type = object : TypeToken<List<NoteData>>() {}.type
    return gson.fromJson(jsonString, type)
}
