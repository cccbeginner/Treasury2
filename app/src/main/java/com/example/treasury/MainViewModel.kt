package com.example.treasury

import androidx.lifecycle.*
import com.example.treasury.date.Date
import com.example.treasury.date.DateRepository
import com.example.treasury.form.Form
import com.example.treasury.form.FormRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val formRepository: FormRepository,
    private val dateRepository: DateRepository
) : ViewModel() {
    var currentData : LiveData<Array<Form>>
    var currentYearMonth = -1
    var currentDate: LiveData<Date>

    init {
        currentYearMonth = MyApplication.current
        currentData = formRepository.formListFlow.asLiveData()
        currentDate = dateRepository.dateFlow.asLiveData()
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
            dateRepository.fetchData(currentYearMonth)
        }
    }
}

class MainViewModelFactory(
    private val formRepository: FormRepository,
    private val dateRepository: DateRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(formRepository, dateRepository) as T
    }
}