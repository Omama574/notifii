package com.notifii.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** The four loudness levels. Declared loudest first, so a lower ordinal is louder. Labels live in strings.xml. */
enum class Rung { SIREN, RING, LATER, HIDE }

/** Why a notification got its rung. Stored as a code so the explanation shows in the phone's language. */
enum class Why { EXCEPTION, PERSON, APP, UNSORTED }

/** An app (`person == ""`) or one person inside an app, placed on a rung. */
@Entity(tableName = "placements", primaryKeys = ["pkg", "person"])
data class Placement(
    val pkg: String,
    val person: String,
    val label: String,
    val rung: Rung,
) {
    val isApp get() = person.isEmpty()
}

/** "Upwork is Later, except 'invited you' → Siren". Every non-null condition must match. */
@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pkg: String?,
    val appLabel: String?,
    val sender: String?,
    /** Comma-separated; any one of them matches. */
    val words: String = "",
    val minAmount: Double? = null,
    /** Only when 3+ arrive from the same app/person within 2 minutes. */
    val burst: Boolean = false,
    val rung: Rung,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val wordList get() = words.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "vault", indices = [Index("postedAt"), Index("pkg"), Index("pending")])
data class VaultEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pkg: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val sender: String?,
    val postedAt: Long,
    val rung: Rung,
    /** What decided it, and who it's about (a person or app name), for the "Why?" explanation. */
    val why: Why,
    val subject: String,
    /** Night turned Ring into Later. */
    val night: Boolean = false,
    val ruleId: Long? = null,
    /** A Later notification not yet delivered in a digest. */
    val pending: Boolean = false,
)

data class AppCount(val pkg: String, val appLabel: String, val count: Int)

@Dao
interface NotifiiDao {
    @Query("SELECT * FROM placements ORDER BY label COLLATE NOCASE")
    fun placements(): Flow<List<Placement>>

    @Upsert
    suspend fun place(p: Placement)

    @Query("DELETE FROM placements WHERE pkg = :pkg AND person = :person")
    suspend fun unplace(pkg: String, person: String)

    @Query("SELECT * FROM rules ORDER BY createdAt DESC")
    fun rules(): Flow<List<Rule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRule(r: Rule): Long

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Insert
    suspend fun record(e: VaultEntry): Long

    @Query("SELECT * FROM vault WHERE postedAt >= :since ORDER BY postedAt DESC")
    fun since(since: Long): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault WHERE postedAt >= :since ORDER BY postedAt DESC")
    suspend fun sinceOnce(since: Long): List<VaultEntry>

    // shortcut: LIKE scan; switch to FTS4 if the Vault grows past ~100k rows and search feels slow.
    @Query(
        """
        SELECT * FROM vault
        WHERE (:rung IS NULL OR rung = :rung)
          AND (:q = '' OR title LIKE '%' || :q || '%' OR text LIKE '%' || :q || '%' OR appLabel LIKE '%' || :q || '%')
        ORDER BY postedAt DESC LIMIT 300
        """
    )
    fun search(q: String, rung: Rung?): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault WHERE pending = 1 ORDER BY postedAt DESC")
    fun pending(): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault WHERE pending = 1 ORDER BY postedAt DESC")
    suspend fun pendingOnce(): List<VaultEntry>

    @Query("UPDATE vault SET pending = 0 WHERE pending = 1")
    suspend fun deliverAll()

    @Query("SELECT pkg, appLabel, COUNT(*) AS count FROM vault WHERE postedAt >= :since GROUP BY pkg ORDER BY count DESC")
    fun noisiest(since: Long): Flow<List<AppCount>>

    @Query("DELETE FROM vault WHERE postedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM vault")
    suspend fun clearVault()
}

@Database(entities = [Placement::class, Rule::class, VaultEntry::class], version = 1, exportSchema = true)
abstract class NotifiiDb : RoomDatabase() {
    abstract fun dao(): NotifiiDao

    companion object {
        fun create(context: Context) = Room.databaseBuilder(context, NotifiiDb::class.java, "notifii.db").build()
    }
}
