package app.gossamer.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WebDao {
    @Query("SELECT * FROM thoughts ORDER BY at")
    fun thoughts(): Flow<List<Thought>>

    @Query("SELECT * FROM thoughts ORDER BY at")
    suspend fun thoughtList(): List<Thought>

    @Query("SELECT * FROM thoughts WHERE id = :id")
    suspend fun thought(id: Long): Thought?

    @Insert
    suspend fun insert(t: Thought): Long

    @Update
    suspend fun update(t: Thought)

    @Update
    suspend fun updateAll(t: List<Thought>)

    @Delete
    suspend fun delete(t: Thought)

    @Query("SELECT COUNT(*) FROM thoughts WHERE gone IS NULL AND cocoon IS NULL")
    suspend fun liveCount(): Int

    @Query("SELECT * FROM strands ORDER BY at")
    fun strands(): Flow<List<Strand>>

    @Query("SELECT * FROM strands ORDER BY at")
    suspend fun strandList(): List<Strand>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun tie(s: Strand): Long

    @Query("DELETE FROM strands WHERE a = :a AND b = :b")
    suspend fun untie(a: Long, b: Long)

    @Query("DELETE FROM strands WHERE a = :id OR b = :id")
    suspend fun untieAll(id: Long)

    @Query("SELECT * FROM cocoons ORDER BY at")
    fun cocoons(): Flow<List<Cocoon>>

    @Query("SELECT * FROM cocoons ORDER BY at")
    suspend fun cocoonList(): List<Cocoon>

    @Insert
    suspend fun spin(c: Cocoon): Long

    @Update
    suspend fun update(c: Cocoon)

    @Delete
    suspend fun delete(c: Cocoon)
}

@Database(entities = [Thought::class, Strand::class, Cocoon::class], version = 1, exportSchema = true)
abstract class WebDb : RoomDatabase() {
    abstract fun web(): WebDao

    companion object {
        fun create(context: Context): WebDb = Room.databaseBuilder(context, WebDb::class.java, "gossamer.db").build()
    }
}
