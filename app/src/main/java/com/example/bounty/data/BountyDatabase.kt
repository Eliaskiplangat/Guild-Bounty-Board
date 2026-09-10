package com.example.bounty.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Bounty::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BountyDatabase : RoomDatabase() {

    abstract fun bountyDao(): BountyDao

    companion object {
        @Volatile private var instance: BountyDatabase? = null

        fun getInstance(context: Context): BountyDatabase =
            instance ?: synchronized(this) {
                // Room 3's databaseBuilder takes a reified type param instead of ::class.java;
                // on Android the factory lambda defaults to reflection, same as Room 2.x did.
                instance ?: Room.databaseBuilder<BountyDatabase>(
                    context = context.applicationContext,
                    name = "bounty_board.db"
                ).build().also { instance = it }
            }
    }
}