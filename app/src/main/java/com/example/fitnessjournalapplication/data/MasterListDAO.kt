package com.example.fitnessjournalapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterExerciseDao {


    @Query("SELECT * FROM master_exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<MasterExercise>>

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: MasterExercise)

    @Update
    suspend fun update(exercise: MasterExercise)

    @Delete
    suspend fun delete(exercise: MasterExercise)
}
