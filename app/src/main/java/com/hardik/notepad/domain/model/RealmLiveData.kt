package com.hardik.notepad.domain.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults

/*class RealmLiveData<T>(private val results: RealmResults<T>) : LiveData<List<T>>() {

    private val listener = RealmChangeListener<RealmResults<T>> { results ->
        value = results
    }

    override fun onActive() {
        super.onActive()
        results.addChangeListener(listener)
        value = results
    }

    override fun onInactive() {
        super.onInactive()
        results.removeChangeListener(listener)
    }
}*/
class RealmLiveData<T>(private val realmResults: RealmResults<T>) : LiveData<List<T>>() {

    private val listener = RealmChangeListener<RealmResults<T>> { results ->
        postValue(results)
    }

    init {
        // Observe RealmResults changes
        realmResults.addChangeListener(listener)
        // Set initial value
        postValue(realmResults)
    }

    override fun onActive() {
        super.onActive()
        // Post value when LiveData becomes active
        postValue(realmResults)
    }

    override fun onInactive() {
        super.onInactive()
        // Remove listener when LiveData becomes inactive
        realmResults.removeChangeListener(listener)
    }
}
