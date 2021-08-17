package com.example.treasury.form

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FormRepository (private val formDao: FormDao) {

    /*
     * Here, I store in lists even there are
     *  some type with only one data.
     *
     * mutable map <form array>
     */
    val formListFlow = MutableStateFlow<Array<Form>>(arrayOf())
    private var idMax = -1

    init {
        GlobalScope.launch {
            idMax = formDao.getMaxId()
        }
    }

    suspend fun fetchData(yearMonth: Int){
        val formArr = formDao.getByYearMonth(yearMonth)
        formListFlow.emit(formArr)
    }

    suspend fun insertMany(formArrayList: ArrayList<Form>){
        formDao.insertMany(formArrayList.toTypedArray())
        idMax = formDao.getMaxId()
    }

    suspend fun deleteByCurrentYearMonth(yearMonth: Int){
        formDao.deleteByYearMonth(yearMonth)
    }

    fun assignId(): Int{
        idMax += 1
        return idMax
    }
}