package com.hardik.notepad.extra_class

import android.util.Log
import com.hardik.notepad.domain.model.Note
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log

@Singleton
class UniqueIdGenerator @Inject constructor(private val realm: Realm) {

    private val TAG = UniqueIdGenerator::class.java.simpleName

    suspend fun generateUniqueId(): Long {
        return withContext(Dispatchers.Main) {
            // Get the maximum ID from the Realm database.
            val maxId: Number? = realm.where(Note::class.java).max("id")
            val nextId = if (maxId != null) maxId.toLong() + 1 else 1

            Log.d(TAG, "generateUniqueId: $nextId")
            nextId
        }
    }

    private val idCounter = AtomicLong(0)

    fun generateUniqueId1(): Long {
        val id = idCounter.incrementAndGet()
        Log.i(TAG, "generateUniqueId1: $id")
        return id
    }
}
