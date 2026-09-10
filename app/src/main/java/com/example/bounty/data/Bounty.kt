package com.example.bounty.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * A single guild bounty.
 *
 * @param progress objective completion, always clamped to 0f..1f by whoever writes it
 *                 (the DAO/repository don't clamp for you — see BountyViewModel.advanceProgress).
 */
@Entity(tableName = "bounties")
data class Bounty(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val tags: List<String>,
    val rewardGold: Int,
    val progress: Float,
    val isAccepted: Boolean = false
)

/**
 * Stores tags as a single comma-separated column rather than a join table —
 * plenty for a handful of short tags per bounty, and keeps the schema to one table.
 */
class Converters {
    @TypeConverter
    fun fromTagList(tags: List<String>): String = tags.joinToString(separator = ",")

    @TypeConverter
    fun toTagList(raw: String): List<String> =
        if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }
}