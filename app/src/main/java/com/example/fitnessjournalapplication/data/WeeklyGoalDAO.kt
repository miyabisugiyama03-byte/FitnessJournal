package com.example.fitnessjournalapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyGoalDao {

    @Query("SELECT * FROM weekly_goal WHERE id = 0 LIMIT 1")
    fun getWeeklyGoal(): Flow<WeeklyGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: WeeklyGoal)
}
