package org.fossify.keyboard.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fossify.keyboard.models.Clip

@Dao
interface ClipsDao {
    @Query("SELECT * FROM clips ORDER BY is_pinned ASC, id DESC")
    fun getClips(): List<Clip>

    @Query("SELECT * FROM clips WHERE is_pinned = 1 ORDER BY id DESC")
    fun getPinnedClips(): List<Clip>

    @Query("SELECT * FROM clips WHERE is_pinned = 0 ORDER BY id DESC")
    fun getRecentClips(): List<Clip>

    @Query("SELECT id FROM clips WHERE value = :value")
    fun getClipWithValue(value: String): Long?

    @Query("SELECT id FROM clips WHERE value = :value AND is_pinned = 1 LIMIT 1")
    fun getPinnedClipWithValue(value: String): Long?

    @Query("SELECT id FROM clips WHERE value = :value AND is_pinned = 0 LIMIT 1")
    fun getRecentClipWithValue(value: String): Long?

    @Query("DELETE FROM clips WHERE value = :value AND is_pinned = 0")
    fun deleteRecentClipWithValue(value: String)

    @Query("DELETE FROM clips WHERE value = :value")
    fun deleteClipWithValue(value: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(clip: Clip): Long

    @Query("UPDATE clips SET is_pinned = :isPinned WHERE id = :id")
    fun updatePinned(id: Long, isPinned: Boolean)

    // IMPORTANT: Rule specified by user:
    // Pinned texts in the clipboard MUST NOT be deleted until unpinned!
    @Query("DELETE FROM clips WHERE id = :id AND is_pinned = 0")
    fun delete(id: Long)

    @Query("DELETE FROM clips WHERE id = :id AND is_pinned = 0")
    fun forceDelete(id: Long)

    // Deleting all clipboard texts deletes ONLY unpinned clips! Pinned items are protected!
    @Query("DELETE FROM clips WHERE is_pinned = 0")
    fun deleteAll()

    @Query("SELECT MAX(id) FROM clips")
    fun getMaxId(): Long?

    @Query("UPDATE clips SET id = :newId WHERE id = :oldId")
    fun updateId(oldId: Long, newId: Long)

    @androidx.room.Transaction
    fun reorderPinnedClips(orderedPinnedClips: List<Clip>) {
        if (orderedPinnedClips.isEmpty()) return
        val currentMax = (getMaxId() ?: 0L).coerceAtLeast(0L)
        val tempBase = -(currentMax + 10000L)
        for (i in orderedPinnedClips.indices) {
            val clip = orderedPinnedClips[i]
            val oldId = clip.id ?: continue
            val tempId = tempBase - i
            updateId(oldId, tempId)
            clip.id = tempId
        }
        val safeMax = (getMaxId() ?: 0L).coerceAtLeast(0L)
        val n = orderedPinnedClips.size
        for (i in orderedPinnedClips.indices) {
            val clip = orderedPinnedClips[i]
            val tempId = clip.id ?: continue
            val newId = safeMax + (n - i)
            updateId(tempId, newId)
            clip.id = newId
        }
    }

    // Keep up to 4000 unpinned items (accommodating >3000 clips as requested by user)
    @Query("DELETE FROM clips WHERE is_pinned = 0 AND id NOT IN (SELECT id FROM clips WHERE is_pinned = 0 ORDER BY id DESC LIMIT 4000)")
    fun trimRecentClips()
}
