package com.example.bounty.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BountyDao {

    // Room keeps this Flow alive and re-emits on every write to the table —
    // this is the single source of truth the UI collects from in all four tasks.
    @Query("SELECT * FROM bounties ORDER BY id ASC")
    fun observeAll(): Flow<List<Bounty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bounties: List<Bounty>)

    @Update
    suspend fun update(bounty: Bounty)

    // Used by Task B: dragging left past threshold abandons -> row deleted -> Flow re-emits without it.
    @Delete
    suspend fun delete(bounty: Bounty)

    @Query("SELECT COUNT(*) FROM bounties")
    suspend fun count(): Int
}