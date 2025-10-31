package com.example.fitnessjournalapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StrengthExerciseDao {

    @Insert
    suspend fun insert(exercise: StrengthExercise)

    @Query("SELECT * FROM strength_log WHERE date = :date")
    fun getExercisesForDate(date: LocalDate): Flow<List<StrengthExercise>>

    @Query("SELECT * FROM strength_log ORDER BY date DESC")
    fun getAllExercises(): Flow<List<StrengthExercise>>
}
