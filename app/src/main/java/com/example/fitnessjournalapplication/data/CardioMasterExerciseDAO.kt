package com.example.fitnessjournalapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardioMasterExerciseDao {

    @Query("SELECT * FROM cardio_master_exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<CardioMasterExercise>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: CardioMasterExercise)

    @Delete
    suspend fun delete(exercise: CardioMasterExercise)


}
