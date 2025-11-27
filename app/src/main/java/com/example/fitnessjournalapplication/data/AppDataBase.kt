package com.example.fitnessjournalapplication.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [StrengthExercise::class, StrengthMasterExercise::class,
        CardioExercise::class, CardioMasterExercise::class, WeeklyGoal::class],
    version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun strengthExerciseDao(): StrengthExerciseDao
    abstract fun strengthMasterExerciseDao(): StrengthMasterExerciseDao
    abstract fun cardioExerciseDao(): CardioExerciseDao
    abstract fun cardioMasterExerciseDao(): CardioMasterExerciseDao

    abstract fun weeklyGoalDao(): WeeklyGoalDao

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
                    .fallbackToDestructiveMigration(true) // optional: keeps DB in sync
                    .build()
                INSTANCE = instance

                // Prepopulate master exercises
                prepopulateIfEmpty(instance)

                instance
            }
        }

        private fun prepopulateIfEmpty(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val strengthDao = db.strengthMasterExerciseDao()
                val cardioDao = db.cardioMasterExerciseDao()
                val goalDao = db.weeklyGoalDao()

                // Populate Strength Master Exercises if empty
                if (strengthDao.getAllExercises().first().isEmpty()) {
                    val strengthExercises = listOf(
                        "Bench Press", "Squat", "Deadlift", "Overhead Press",
                        "Barbell Row", "Lat Pulldown", "Bicep Curl", "Tricep Extensions"
                    )
                    strengthExercises.forEach { name ->
                        strengthDao.insert(StrengthMasterExercise(name = name))
                    }
                }

                // Populate Cardio Master Exercises if empty
                if (cardioDao.getAllExercises().first().isEmpty()) {
                    val cardioExercises = listOf(
                        "Running", "Cycling", "Walking", "Rowing",
                        "Elliptical", "Swimming", "Stair Master", "Jump Rope"
                    )
                    cardioExercises.forEach { name ->
                        cardioDao.insert(CardioMasterExercise(name = name))
                    }
                }
                val existingGoals = goalDao.getWeeklyGoal().first()

                if (existingGoals == null) {
                    goalDao.upsert(
                        WeeklyGoal(
                            id = 1,
                            goalPerWeek = 3 // default weekly goal
                        )
                    )

                }
            }
        }
    }
}
