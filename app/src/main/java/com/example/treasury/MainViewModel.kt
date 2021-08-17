package com.example.treasury

import androidx.lifecycle.*
import com.example.treasury.form.Form
import com.example.treasury.form.FormRepository
import kotlinx.coroutines.launch

class MainViewModel(private val formRepository: FormRepository) : ViewModel() {
    var currentData : LiveData<Array<Form>>
    var currentYearMonth = -1

    init {
        currentYearMonth = MyApplication.current
        currentData = formRepository.formListFlow.asLiveData()
        fetchData()
    }

    fun updateYear(year: Int){
        val month = currentYearMonth % 12
        currentYearMonth = year * 12 + month
        fetchData()
    }

    fun updateMonth(month: Int){
        val year = currentYearMonth / 12
        currentYearMonth = year * 12 + month
        fetchData()
    }

    private fun fetchData(){
        viewModelScope.launch {
            formRepository.fetchData(currentYearMonth)
        }
    }
}

class MainViewModelFactory(private val formRepository: FormRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(formRepository) as T
    }
}