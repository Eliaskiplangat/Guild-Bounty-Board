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
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BountyDatabase::class.java,
                    "bounty_board.db"
                ).build().also { instance = it }
            }
    }
}