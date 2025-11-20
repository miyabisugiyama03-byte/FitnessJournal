package com.example.fitnessjournalapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strength_master_exercises")
data class StrengthMasterExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

