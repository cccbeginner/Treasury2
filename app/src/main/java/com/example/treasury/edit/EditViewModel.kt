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
    private var originalData : LiveData<Array<Form>> = formRepository.formListFlow.asLiveData()
    private var preMonthData : LiveData<Array<Form>> = formRepository.formListFlowExtra.asLiveData()
    private var formArrayParser = FormArrayParser(arrayListOf())
    private var originalDate : LiveData<Date> = dateRepository.dateFlow.asLiveData()
    var currentData = MutableLiveData<ArrayList<Form>>()
    var currentDate = MutableLiveData<Date>()
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
                currentData.postValue(tmpParser.exportData())
            }
        }
        originalData.observeForever {
            var formArray = originalData.value!!.toCollection(ArrayList())
            if (!alreadyHaveData && formArray.isNotEmpty()){
                alreadyHaveData = true
                currentData.postValue(formArray)
                formArrayParser = FormArrayParser(formArray)
            }else if (!alreadyHaveData && !alreadyMakeUpData && formArray.isEmpty()){
                formArray = initFormArray()
                currentData.postValue(formArray)
                formArrayParser = FormArrayParser(formArray)
            }
        }
        originalDate.observeForever{
            currentDate.postValue(it)
        }
    }

    fun updateDateYear(year: String){
        val newDate = currentDate.value!!
        newDate.year = year
        currentDate.postValue(newDate)
    }
    fun updateDateMonth(month: String){
        val newDate = currentDate.value!!
        newDate.month = month
        currentDate.postValue(newDate)
    }
    fun updateDateDay(day: String){
        val newDate = currentDate.value!!
        newDate.day = day
        currentDate.postValue(newDate)
    }

    private fun initFormArray(): ArrayList<Form>{
        val ret = ArrayList<Form>()
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "一、流動現金", false, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "二、投資帳現值", false, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "三、貸款餘額" ,true, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_normal, "四、扣除",true, false))
        ret.add(Form(assignId(), -1, currentYearMonth, Form.type_USD, "五、美股",true, false))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, Form.type_normal, "a. 活存",true, false))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, Form.type_normal, "b. 現金", false, false))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, Form.type_USD, "c. 外幣",true, false))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, Form.type_normal, "a. 證券",true, false))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, Form.type_normal, "b. 基金",true, false))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, Form.type_normal, "c. 黃金",true, false))
        return ret
    }

    fun assignId(): Int{
        return formRepository.assignId()
    }

    fun insertForm(form: Form){
        formArrayParser.insert(form)
        currentData.postValue(formArrayParser.exportData())
    }

    fun updateFormValue(id: Int, value: String){
        formArrayParser.updateValue(id, value)
        currentData.postValue(formArrayParser.exportData())
    }
    fun updateFormWeight(id: Int, weight: String){
        formArrayParser.updateWeight(id, weight)
        currentData.postValue(formArrayParser.exportData())
    }
    fun updateFormName(id: Int, name: String){
        formArrayParser.updateName(id, name)
        currentData.postValue(formArrayParser.exportData())
    }
    fun updateFormNote(id: Int, note: String){
        formArrayParser.updateNote(id, note)
    }
    fun deleteForm(id: Int){
        formArrayParser.delete(id)
        currentData.postValue(formArrayParser.exportData())
    }
    fun saveData(){
        viewModelScope.launch {
            formRepository.deleteByCurrentYearMonth(currentYearMonth)
            formRepository.insertMany(formArrayParser.exportData())
            formRepository.fetchData(currentYearMonth)
            dateRepository.insert(currentDate.value!!)
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