package com.example.treasury.form

import androidx.room.*

@Dao
interface FormDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(formArray : Array<Form>)

    @Query("SELECT * FROM tbl_form WHERE year_month = :yearMonth ORDER BY _id ASC")
    suspend fun getByYearMonth(yearMonth: Int): Array<Form>

    @Query("DELETE FROM tbl_form WHERE year_month = :yearMonth")
    suspend fun deleteByYearMonth(yearMonth: Int)

    @Query("SELECT MAX(_id) from tbl_form")
    suspend fun getMaxId(): Int
}