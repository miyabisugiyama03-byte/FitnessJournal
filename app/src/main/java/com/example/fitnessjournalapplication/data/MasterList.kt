package com.example.fitnessjournalapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_exercises")
data class MasterExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
)

