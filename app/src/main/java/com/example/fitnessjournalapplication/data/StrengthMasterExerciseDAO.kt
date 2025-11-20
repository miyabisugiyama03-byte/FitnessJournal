package com.example.fitnessjournalapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StrengthMasterExerciseDao {


    @Query("SELECT * FROM strength_master_exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<StrengthMasterExercise>>

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: StrengthMasterExercise)

    @Update
    suspend fun update(exercise: StrengthMasterExercise)

    @Delete
    suspend fun delete(exercise: StrengthMasterExercise)
}
