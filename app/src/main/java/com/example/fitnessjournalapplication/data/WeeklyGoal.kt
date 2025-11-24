package com.example.fitnessjournalapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_goal")
data class WeeklyGoal(
    @PrimaryKey val id: Int = 0,   // always 0, single row
    val goalPerWeek: Int = 3       // default = 3 workouts per week
)

