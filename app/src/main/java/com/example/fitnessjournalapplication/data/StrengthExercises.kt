package com.example.fitnessjournalapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "strength_exercises")
data class StrengthExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val exercise: String,
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Float = 0f
)

