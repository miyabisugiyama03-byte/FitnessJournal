package com.example.fitnessjournalapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "cardio_exercises")
data class CardioExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val exercise: String,
    val duration: Int,
    val distance: Float,
    val notes: String? = null
)


