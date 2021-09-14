package com.example.treasury.edit

import androidx.lifecycle.*
import com.example.treasury.date.Date
import com.example.treasury.date.DateRepository
import com.example.treasury.form.FormArrayParser
import com.example.treasury.form.Form
import com.example.treasury.form.FormRepository
import kotlinx.coroutines.launch

class EditViewModel(
    private val formRepository: FormRepository,
    private val dateRepository: DateRepository,
    private val currentYearMonth : Int
) : ViewModel() {
    private var curMonthData : LiveData<Array<Form>> = formRepository.formListFlow.asLiveData()
    private var preMonthData : LiveData<Array<Form>> = formRepository.formListFlowExtra.asLiveData()
    var originalDate : LiveData<Date> = dateRepository.dateFlow.asLiveData()
    var originalData = MutableLiveData<ArrayList<Form>>()
    private var alreadyHaveData = false
    private var alreadyMakeUpData = false

    init {
        fetchData()
        preMonthData.observeForever{
            if (!alreadyHaveData && it.isNotEmpty()){
                alreadyMakeUpData = true
                val formData = it.toCollection(ArrayList())
                val tmpParser = FormArrayParser(formData)
                for (form in formData){
                    tmpParser.updateId(form.id, assignId())
                }
                tmpParser.clearContent()
                originalData.postValue(tmpParser.exportData())
            }
        }
        curMonthData.observeForever {
            var formArray = curMonthData.value!!.toCollection(ArrayList())
            if (!alreadyHaveData && formArray.isNotEmpty()){
                alreadyHaveData = true
                originalData.postValue(formArray)
            }else if (!alreadyHaveData && !alreadyMakeUpData && formArray.isEmpty()){
                formArray = initFormArray()
                originalData.postValue(formArray)
            }
        }
    }

    private fun initFormArray(): ArrayList<Form>{
        val ret = ArrayList<Form>()
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "1", "一、流動現金", false, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "1", "二、投資帳現值", false, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "1", "三、貸款餘額" ,true, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "1", "四、扣除",true, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_USD, "", "五、美股",true, false))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, Form.type_normal, "1", "a. 活存",true, false))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, Form.type_normal, "1", "b. 現金", false, false))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, Form.type_USD, "", "c. 外幣",true, false))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, Form.type_normal, "1", "a. 證券",true, false))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, Form.type_normal, "1", "b. 基金",true, false))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, Form.type_normal, "1", "c. 黃金",true, false))
        return ret
    }

    fun assignId(): Int{
        return formRepository.assignId()
    }

    fun saveData(currentDate: Date, currentData: ArrayList<Form>){
        viewModelScope.launch {
            formRepository.deleteByCurrentYearMonth(currentYearMonth)
            formRepository.insertMany(currentData)
            formRepository.fetchData(currentYearMonth)
            dateRepository.insert(currentDate)
            dateRepository.fetchData(currentYearMonth)
        }
    }
    private fun fetchData(){
        viewModelScope.launch {
            formRepository.fetchData(currentYearMonth)
            formRepository.fetchDataExtra(currentYearMonth-1)
        }
    }
}

class EditViewModelFactory(
    private val formRepository: FormRepository,
    private val dateRepository: DateRepository,
    private val currentYearMonth : Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditViewModel(formRepository, dateRepository, currentYearMonth) as T
    }
}