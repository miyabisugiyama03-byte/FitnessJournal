package com.example.fitnessjournalapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cardio_master_exercises")
data class CardioMasterExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
