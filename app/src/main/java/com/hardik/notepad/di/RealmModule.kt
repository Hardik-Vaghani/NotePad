package com.hardik.notepad.di

import android.content.Context
import com.hardik.notepad.extra_class.UniqueIdGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealmModule {

    @Provides
    @Singleton
    fun provideRealmConfiguration(@ApplicationContext context: Context): RealmConfiguration {
        Realm.init(context)
        return RealmConfiguration.Builder()
            .name("note_pad.realm") // Name of the realm file
            .schemaVersion(1) // Increment this when you change the schema
            .migration(WholeRealmMigration()) // Replace with your actual migration class
            .allowWritesOnUiThread(true)
            .allowQueriesOnUiThread(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRealm(realmConfiguration: RealmConfiguration): Realm {
        return Realm.getInstance(realmConfiguration)
    }

    @Provides
    @Singleton
    fun provideUniqueIdGenerator(realm: Realm): UniqueIdGenerator {
        return UniqueIdGenerator(realm)
    }
}
