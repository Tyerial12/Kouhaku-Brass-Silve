package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "player_save")
data class PlayerSaveEntity(
    @PrimaryKey val id: Int = 1,
    val playerName: String = "Akira",
    val starterChosen: Boolean = false,
    val mapId: String = "momiji_hamlet",
    val tileX: Int = 5,
    val tileY: Int = 6,
    val facing: String = "DOWN",
    val followerX: Int = 5,
    val followerY: Int = 7,
    val stepCount: Int = 0,
    val unredeemedSteps: Int = 0,
    val timeOfDay: String = "TWILIGHT",
    val brassEmblemEarned: Boolean = false,
    val silverEmblemEarned: Boolean = false,
    val grandmasterDefeated: Boolean = false,
    val hououAwakened: Boolean = false,
    val ryujinAwakened: Boolean = false,
    val spiritOrbs: Int = 6,
    val emberOrbs: Int = 2,
    val duskOrbs: Int = 2,
    val heavyBrassOrbs: Int = 1,
    val crimsonAcorns: Int = 2,
    val duskAcorns: Int = 2,
    val brassAcorns: Int = 1,
    val healingTeas: Int = 5,
    val lastSavedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "owned_monsters")
data class OwnedMonsterEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val speciesId: Int,
    val nickname: String,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val exp: Int = 0,
    val amity: Int = 70, // 0..255 bonding score from walking together
    val partySlot: Int? = 0 // 0..5 for party, null for Sanctuary Box
)

@Dao
interface KouhakuDao {
    @Query("SELECT * FROM player_save WHERE id = 1 LIMIT 1")
    fun observePlayerSave(): Flow<PlayerSaveEntity?>

    @Query("SELECT * FROM player_save WHERE id = 1 LIMIT 1")
    suspend fun getPlayerSaveOnce(): PlayerSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerSave(save: PlayerSaveEntity)

    @Query("SELECT * FROM owned_monsters ORDER BY CASE WHEN partySlot IS NULL THEN 99 ELSE partySlot END ASC, uid ASC")
    fun observeAllMonsters(): Flow<List<OwnedMonsterEntity>>

    @Query("SELECT * FROM owned_monsters ORDER BY CASE WHEN partySlot IS NULL THEN 99 ELSE partySlot END ASC, uid ASC")
    suspend fun getAllMonstersOnce(): List<OwnedMonsterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonster(monster: OwnedMonsterEntity): Long

    @Update
    suspend fun updateMonster(monster: OwnedMonsterEntity)

    @Query("DELETE FROM owned_monsters")
    suspend fun clearAllMonsters()

    @Query("DELETE FROM player_save")
    suspend fun clearPlayerSave()
}

@Database(
    entities = [PlayerSaveEntity::class, OwnedMonsterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KouhakuDatabase : RoomDatabase() {
    abstract fun kouhakuDao(): KouhakuDao

    companion object {
        @Volatile
        private var INSTANCE: KouhakuDatabase? = null

        fun getInstance(context: Context): KouhakuDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KouhakuDatabase::class.java,
                    "kouhaku_chronicles.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { INSTANCE = it }
            }
        }
    }
}

class KouhakuRepository(private val dao: KouhakuDao) {
    val playerSaveFlow: Flow<PlayerSaveEntity?> = dao.observePlayerSave()
    val monstersFlow: Flow<List<OwnedMonsterEntity>> = dao.observeAllMonsters()

    suspend fun ensureInitialized(): PlayerSaveEntity {
        val existing = dao.getPlayerSaveOnce()
        if (existing != null) return existing
        val initial = PlayerSaveEntity()
        dao.upsertPlayerSave(initial)
        return initial
    }

    suspend fun savePlayer(save: PlayerSaveEntity) {
        dao.upsertPlayerSave(save.copy(lastSavedTimestamp = System.currentTimeMillis()))
    }

    suspend fun getMonstersOnce(): List<OwnedMonsterEntity> = dao.getAllMonstersOnce()

    suspend fun addMonster(monster: OwnedMonsterEntity): Long = dao.insertMonster(monster)

    suspend fun updateMonster(monster: OwnedMonsterEntity) = dao.updateMonster(monster)

    suspend fun healAllPartyMonsters() {
        val all = dao.getAllMonstersOnce()
        for (m in all) {
            if (m.currentHp < m.maxHp) {
                dao.updateMonster(m.copy(currentHp = m.maxHp))
            }
        }
    }

    suspend fun resetSaveForNewJourney() {
        dao.clearAllMonsters()
        dao.clearPlayerSave()
        dao.upsertPlayerSave(PlayerSaveEntity())
    }
}
