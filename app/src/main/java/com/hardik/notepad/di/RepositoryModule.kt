package com.hardik.notepad.di

import com.hardik.notepad.data.repository.NoteRepositoryImpl
import com.hardik.notepad.domain.repository.NoteRepository
import com.hardik.notepad.extra_class.UniqueIdGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.Realm
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(realm: Realm, uniqueIdGenerator: UniqueIdGenerator): NoteRepository {
        return NoteRepositoryImpl(realm = realm, uniqueIdGenerator = uniqueIdGenerator)
    }
}