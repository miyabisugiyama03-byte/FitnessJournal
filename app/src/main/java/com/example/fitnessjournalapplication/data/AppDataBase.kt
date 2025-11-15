package com.example.fitnessjournalapplication.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [StrengthExercise::class, MasterExercise::class, CardioExercise::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun strengthExerciseDao(): StrengthExerciseDao
    abstract fun masterExerciseDao(): MasterExerciseDao
    abstract fun cardioExerciseDao(): CardioExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_journal_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.masterExerciseDao()
                                    val exercises = listOf(
                                        "Bench Press", "Squat", "Deadlift", "Overhead Press",
                                        "Barbell Row", "Lat Pulldown", "Bicep Curl", "Tricep Pushdown"
                                    )
                                    exercises.forEach { name ->
                                        dao.insert(MasterExercise(name = name))
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
