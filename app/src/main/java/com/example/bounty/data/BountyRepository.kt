package com.example.bounty.data

import kotlinx.coroutines.flow.Flow

class BountyRepository(private val dao: BountyDao) {

    val bounties: Flow<List<Bounty>> = dao.observeAll()

    /** Call once on app start. No-op after the first launch since count() will be > 0. */
    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.insertAll(sampleBounties())
        }
    }

    suspend fun update(bounty: Bounty) = dao.update(bounty)

    suspend fun delete(bounty: Bounty) = dao.delete(bounty)

    private fun sampleBounties(): List<Bounty> = listOf(
        Bounty(title = "Clear the Sunken Crypt", tags = listOf("Combat", "Stealth"), rewardGold = 250, progress = 0.2f),
        Bounty(title = "Escort the Merchant Caravan", tags = listOf("Escort"), rewardGold = 120, progress = 0f),
        Bounty(title = "Retrieve the Lost Tome", tags = listOf("Fetch", "Stealth"), rewardGold = 90, progress = 0.5f),
        Bounty(title = "Hunt the Dire Wolves", tags = listOf("Combat", "Hunt", "Outdoors"), rewardGold = 180, progress = 0.75f),
        Bounty(title = "Deliver Medicine to Millhaven", tags = listOf("Escort", "Fetch"), rewardGold = 60, progress = 1f),
        Bounty(title = "Infiltrate the Smugglers' Den", tags = listOf("Stealth", "Combat", "Fetch"), rewardGold = 300, progress = 0.1f)
    )
}