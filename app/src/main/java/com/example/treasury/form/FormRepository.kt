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
    val formListFlowExtra = MutableStateFlow<Array<Form>>(arrayOf())
    private var idMax = -1

    init {
        GlobalScope.launch {
            refreshIdMax()
        }
    }

    private suspend fun refreshIdMax(){
        val num = formDao.getMaxId()
        if(num == null){
            idMax = 0
        }else{
            idMax = 1
        }
    }

    suspend fun fetchData(yearMonth: Int){
        val formArr = formDao.getByYearMonth(yearMonth)
        formListFlow.emit(formArr)
    }
    suspend fun fetchDataExtra(yearMonth: Int){
        val formArr = formDao.getByYearMonth(yearMonth)
        formListFlowExtra.emit(formArr)
    }

    suspend fun insertMany(formArrayList: ArrayList<Form>){
        formDao.insertMany(formArrayList.toTypedArray())
        refreshIdMax()
    }

    suspend fun deleteByCurrentYearMonth(yearMonth: Int){
        formDao.deleteByYearMonth(yearMonth)
    }

    fun assignId(): Int{
        idMax += 1
        return idMax
    }
}