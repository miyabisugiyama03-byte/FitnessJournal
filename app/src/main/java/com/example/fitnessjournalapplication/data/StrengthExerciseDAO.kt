package com.example.fitnessjournalapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy

import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StrengthExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: StrengthExercise)

    @Update
    suspend fun update(exercise: StrengthExercise)

    @Delete
    suspend fun delete(exercise: StrengthExercise)

    @Query("SELECT * FROM strength_exercises WHERE date = :date")
    fun getExercisesForDate(date: LocalDate): Flow<List<StrengthExercise>>

    @Query("SELECT * FROM strength_exercises ORDER BY date ASC")
    fun getAllStrengthExercises(): Flow<List<StrengthExercise>>

}
