package com.example.treasury.date

import androidx.room.*

@Dao
interface DateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(date: Date)

    @Delete
    suspend fun delete(date: Date)

    @Query("SELECT * FROM tbl_date WHERE year_month = :yearMonth")
    suspend fun getByYearMonth(yearMonth: Int): Date?
}