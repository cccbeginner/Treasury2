package com.example.treasury.date

import kotlinx.coroutines.flow.MutableStateFlow

class DateRepository (private val dateDao: DateDao) {

    val dateFlow = MutableStateFlow(Date(-1))

    suspend fun fetchData(yearMonth: Int){
        val date = dateDao.getByYearMonth(yearMonth)
        if (date == null){
            dateFlow.emit(Date(yearMonth))
        }else{
            dateFlow.emit(date)
        }
    }

    suspend fun insert(date: Date){
        dateDao.insert(date)
    }

    suspend fun delete(date: Date){
        dateDao.delete(date)
    }
}