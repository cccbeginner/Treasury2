package com.example.treasury.edit

import androidx.lifecycle.*
import com.example.treasury.form.FormArrayParser
import com.example.treasury.form.Form
import com.example.treasury.form.FormRepository
import kotlinx.coroutines.launch

class EditViewModel(
    private val formRepository: FormRepository,
    private val currentYearMonth : Int
) : ViewModel() {
    private var originalData : LiveData<Array<Form>> = formRepository.formListFlow.asLiveData()
    private var formArrayParser = FormArrayParser(arrayListOf())
    var currentData = MutableLiveData<ArrayList<Form>>()

    init {
        fetchData()
        originalData.observeForever {
            var formArray = originalData.value!!.toCollection(ArrayList())
            if (formArray.isEmpty()) {
                formArray = initFormArray()
            }
            currentData.postValue(formArray)
            formArrayParser = FormArrayParser(formArray)
        }
    }

    private fun initFormArray(): ArrayList<Form>{
        val ret = ArrayList<Form>()
        ret.add(Form(assignId(), -1, currentYearMonth, "一、流動現金", "", ""))
        ret.add(Form(assignId(), -1, currentYearMonth, "二、投資帳現值", "", ""))
        ret.add(Form(assignId(), -1, currentYearMonth, "三、貸款餘額", "", ""))
        ret.add(Form(assignId(), -1, currentYearMonth, "四、扣除", "", ""))
        ret.add(Form(assignId(), -1, currentYearMonth, "五、美股", "", ""))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, "a. 活存", "", ""))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, "b. 現金", "", ""))
        ret.add(Form(assignId(), ret[0].id, currentYearMonth, "c. 外幣", "", ""))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, "a. 證券", "", ""))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, "b. 基金", "", ""))
        ret.add(Form(assignId(), ret[1].id, currentYearMonth, "c. 黃金", "", ""))
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
    fun updateFormName(id: Int, name: String){
        formArrayParser.updateName(id, name)
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
        }
    }
    private fun fetchData(){
        viewModelScope.launch {
            formRepository.fetchData(currentYearMonth)
        }
    }
}

class EditViewModelFactory(
    private val formRepository: FormRepository,
    private val currentYearMonth : Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditViewModel(formRepository, currentYearMonth) as T
    }
}