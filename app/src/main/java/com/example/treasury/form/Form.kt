package com.example.treasury.form

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "tbl_form", indices = [
    Index(value = ["parent_id","year_month", "name"], unique = true)
])
data class Form(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Int,
    @ColumnInfo(name = "parent_id")
    var parentId: Int,
    @ColumnInfo(name = "year_month")
    val yearMonth: Int,
    var type: Int, // TWD or USD
    var weight: String,
    var name: String,
    var value: String,
    var note: String,
    @ColumnInfo(name = "can_be_a_list")
    val canBeAList: Boolean,
    @ColumnInfo(name = "can_be_modify")
    val canBeModify: Boolean,
) {
    constructor(id: Int, parentId: Int, yearMonth: Int, type: Int, weight: String, name: String, canAddItem: Boolean, canModify: Boolean)
        : this(id, parentId, yearMonth, type, weight, name, "", "", canAddItem, canModify)

    fun valueDecimal(): BigDecimal{
        if(value == "")return BigDecimal.ZERO
        else return BigDecimal(value)
    }

    fun weightDecimal(): BigDecimal{
        if(weight == "")return BigDecimal.ZERO
        else return BigDecimal(weight)
    }

    companion object{
        const val type_normal = 1
        const val type_USD = 2
    }
}