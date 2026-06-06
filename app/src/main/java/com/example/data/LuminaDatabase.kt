package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val event: String,
    val location: String,
    val confidence: Float,
    val isFavorite: Boolean,
    val brushIndex: Int,
    val detectedPeople: String, // Comma separated list of names
    val tags: String // Comma separated tags
)

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relation: String,
    val photoCount: Int,
    val lastSeen: String,
    val confidence: Float,
    val yearsActive: String,
    val eventsActive: Int,
    val soloCount: Int,
    val groupCount: Int,
    val faceBrushIndex: Int
)

@Entity(tableName = "face_clusters")
data class FaceCluster(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: Int,
    val confidence: Float,
    val brushIndex: Int,
    val representativeTags: String
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val resultsCount: Int,
    val confidence: Float,
    val brushIndex: Int,
    val labelName: String
)

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY date DESC")
    fun getAllPhotos(): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoritePhotos(): Flow<List<Photo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<Photo>)

    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhoto(id: Int)
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY photoCount DESC")
    fun getAllPeople(): Flow<List<Person>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<Person>)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deletePerson(id: Int)
}

@Dao
interface FaceClusterDao {
    @Query("SELECT * FROM face_clusters ORDER BY size DESC")
    fun getAllClusters(): Flow<List<FaceCluster>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCluster(cluster: FaceCluster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClusters(clusters: List<FaceCluster>)

    @Query("DELETE FROM face_clusters WHERE id = :id")
    suspend fun deleteCluster(id: Int)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getSearchHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SearchHistory)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}

@Database(entities = [Photo::class, Person::class, FaceCluster::class, SearchHistory::class], version = 1, exportSchema = false)
abstract class LuminaDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun personDao(): PersonDao
    abstract fun faceClusterDao(): FaceClusterDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: LuminaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LuminaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LuminaDatabase::class.java,
                    "lumina_database"
                )
                    .addCallback(LuminaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class LuminaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: LuminaDatabase) {
            // Delete all existing and write beautiful sample memories
            val people = listOf(
                Person(1, "Abby Williams", "Daughter", 242, "Yesterday", 0.994f, "2018 - 2026", 14, 58, 184, 0),
                Person(2, "Daniel Williams", "Father", 489, "Yesterday", 0.998f, "1998 - 2026", 35, 104, 385, 1),
                Person(3, "Nora Chen", "Grandmother", 124, "3 days ago", 0.957f, "2105 - 2026", 8, 23, 101, 2),
                Person(4, "Leo Williams", "Son", 315, "2 hours ago", 0.991f, "2020 - 2026", 19, 72, 243, 3),
                Person(5, "Sophia Williams", "Mother", 412, "1 hour ago", 0.996f, "2000 - 2026", 28, 91, 321, 4)
            )
            db.personDao().insertPeople(people)

            val photos = listOf(
                Photo(
                    id = 1,
                    title = "Graduation Cap Toss",
                    date = "2025-05-24",
                    event = "Sarah's Graduation",
                    location = "Stanford Campus",
                    confidence = 0.986f,
                    isFavorite = true,
                    brushIndex = 0,
                    detectedPeople = "Abby Williams, Daniel Williams",
                    tags = "Graduation, Celebration, Outdoors, Family, Caps"
                ),
                Photo(
                    id = 2,
                    title = "Campfire Stories Under Stars",
                    date = "2025-07-12",
                    event = "Tahoe Camping",
                    location = "Emerald Bay, CA",
                    confidence = 0.974f,
                    isFavorite = false,
                    brushIndex = 1,
                    detectedPeople = "Daniel Williams, Leo Williams, Sophia Williams",
                    tags = "Camping, Tahoe, Night, Outdoors, Campfire, Cozy"
                ),
                Photo(
                    id = 3,
                    title = "Golden Gate Sunset Smiles",
                    date = "2025-10-05",
                    event = "San Francisco Weekend",
                    location = "Marina Green",
                    confidence = 0.992f,
                    isFavorite = true,
                    brushIndex = 2,
                    detectedPeople = "Sophia Williams, Abby Williams",
                    tags = "San Francisco, Sunset, Ocean, Scenic, Happy"
                ),
                Photo(
                    id = 4,
                    title = "Secret Recipe Oatmeal Cookies",
                    date = "2025-12-24",
                    event = "Christmas Eve",
                    location = "Family Kitchen",
                    confidence = 0.965f,
                    isFavorite = false,
                    brushIndex = 3,
                    detectedPeople = "Nora Chen, Leo Williams",
                    tags = "Baking, Holiday, Cozy, Indoor, Secret Recipe, Christmas"
                ),
                Photo(
                    id = 5,
                    title = "Birthday Candle Sparkler",
                    date = "2026-01-18",
                    event = "Leo's 6th Birthday",
                    location = "Home Living Room",
                    confidence = 0.997f,
                    isFavorite = true,
                    brushIndex = 4,
                    detectedPeople = "Leo Williams, Abby Williams, Sophia Williams, Daniel Williams",
                    tags = "Birthday, Candles, Cake, Celebration, Indoors, Sparkler"
                ),
                Photo(
                    id = 6,
                    title = "Cabin Snowball Battle",
                    date = "2026-02-02",
                    event = "Tahoe Cabin Winter",
                    location = "Truckee, CA",
                    confidence = 0.941f,
                    isFavorite = false,
                    brushIndex = 5,
                    detectedPeople = "Abby Williams, Leo Williams",
                    tags = "Snow, Tahoe, Winter, Action, Playful, Outdoors"
                ),
                Photo(
                    id = 7,
                    title = "First Day porch Morning",
                    date = "2024-09-05",
                    event = "School Starts",
                    location = "Front Porch",
                    confidence = 0.989f,
                    isFavorite = false,
                    brushIndex = 6,
                    detectedPeople = "Abby Williams, Sophia Williams",
                    tags = "School, Morning, Autum, Outdoors, Backpack"
                ),
                Photo(
                    id = 8,
                    title = "Water Balloon Explosion SPLASH",
                    date = "2025-08-15",
                    event = "Summer Backyard",
                    location = "Home Backyard",
                    confidence = 0.953f,
                    isFavorite = false,
                    brushIndex = 7,
                    detectedPeople = "Leo Williams, Daniel Williams",
                    tags = "Splash, Summer, Water, Playful, Sun, Backyard"
                ),
                Photo(
                    id = 9,
                    title = "Thanksgiving Golden Feast",
                    date = "2025-11-27",
                    event = "Thanksgiving Dinner",
                    location = "Dining Room",
                    confidence = 0.978f,
                    isFavorite = true,
                    brushIndex = 8,
                    detectedPeople = "Nora Chen, Daniel Williams, Sophia Williams, Abby Williams, Leo Williams",
                    tags = "Thanksgiving, Dinner, Cozy, Indoor, Turkey, Autumn"
                )
            )
            db.photoDao().insertPhotos(photos)

            val clusters = listOf(
                FaceCluster(1, 18, 0.874f, 0, "Outdoor, Hiking, Sunglasses, Tahoe"),
                FaceCluster(2, 9, 0.821f, 1, "Indoor, Wedding, Formal Wear, Glassware"),
                FaceCluster(3, 4, 0.795f, 2, "Outdoors, Running, Park, Blur background")
            )
            db.faceClusterDao().insertClusters(clusters)

            val histories = listOf(
                SearchHistory(1, System.currentTimeMillis() - 3600000, 3, 0.985f, 2, "My Face Scan")
            )
            db.searchHistoryDao().insertHistory(histories[0])
        }
    }
}
