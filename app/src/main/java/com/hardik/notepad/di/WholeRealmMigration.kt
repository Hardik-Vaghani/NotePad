package com.hardik.notepad.di

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import io.realm.RealmSchema

class WholeRealmMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema: RealmSchema = realm.schema

        /*// Migrate from version 1 to 2
        if (oldVersion == 1L) {
            schema.get("User")
                ?.addField("isSelected", Boolean::class.java, FieldAttribute.REQUIRED) // Adding new field as required
            // No need to modify oldVersion here; it's handled by checking oldVersion in subsequent migrations
        }

        // Migrate from version 1 to 2
        if (oldVersion == 2L) {
            schema.get("User")
                ?.addField("newField", String::class.java) // Example: Add a new field
        }

        // Additional migrations can be added here
        if (oldVersion == 3L) {
            // Handle migration from version 2 to 3, if necessary
        }*/
    }
}