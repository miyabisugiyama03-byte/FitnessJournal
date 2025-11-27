package com.example.fitnessjournalapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CardioExerciseDao{

    @Insert
    suspend fun  insert(exercise: CardioExercise)

    @Update
    suspend fun update(exercise: CardioExercise)

    @Delete
    suspend fun delete(exercise: CardioExercise)

    @Query("SELECT * FROM cardio_exercises WHERE date = :date")
    fun getExercisesForDate(date: LocalDate): Flow<List<CardioExercise>>

    @Query("SELECT * FROM cardio_exercises ORDER BY date ASC")
    fun getAllCardioExercises(): Flow<List<CardioExercise>>

    @Query("SELECT * FROM cardio_exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Int): CardioExercise?

}