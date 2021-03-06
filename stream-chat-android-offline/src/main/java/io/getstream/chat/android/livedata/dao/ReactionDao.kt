package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.ReactionEntity

@Dao
internal interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionEntity: ReactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionEntities: List<ReactionEntity>)

    @Query(
        "SELECT * FROM stream_chat_reaction " +
            "WHERE stream_chat_reaction.syncStatus IN (:syncStatus)"
    )
    suspend fun selectSyncNeeded(syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED): List<ReactionEntity>

    @Query(
        "SELECT * FROM stream_chat_reaction " +
            "WHERE stream_chat_reaction.messageid = :messageId AND userId = :userId AND type = :type"
    )
    suspend fun select(messageId: String, userId: String, type: String): ReactionEntity?
}
