package com.example.treasury.date

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_date")
data class Date(
    @PrimaryKey
    @ColumnInfo(name = "year_month")
    val yearMonth: Int,
    var year: String,
    var month: String,
    var day: String
) {
    constructor(yearMonth: Int): this(yearMonth, "", "", "")
}