package com.example.treasury

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.treasury.date.Date
import com.example.treasury.edit.EditActivity
import com.example.treasury.form.Form
import com.example.treasury.form.FormArrayParser

class MainActivity : AppCompatActivity() {

    private var formArrayParser = FormArrayParser(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // viewModel
        val formRepository = (application as MyApplication).formRepository
        val dateRepository = (application as MyApplication).dateRepository
        val mainViewModel = ViewModelProvider(this, MainViewModelFactory(formRepository, dateRepository))
            .get(MainViewModel::class.java)

        // current yearMonth
        val currentYearMonth = MyApplication.current

        // set year spinners
        val spinnerYear = findViewById<Spinner>(R.id.spinner_year)
        spinnerYear.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.year_array,
            R.layout.spinner_item)
        spinnerYear.setSelection(currentYearMonth / 12 - MyApplication.start / 12)
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val year = MyApplication.start / 12 + position
                mainViewModel.updateYear(year)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        // set month spinners
        val spinnerMonth = findViewById<Spinner>(R.id.spinner_month)
        spinnerMonth.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.month_array,
            R.layout.spinner_item)
        spinnerMonth.setSelection(currentYearMonth % 12)
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // position := month
                mainViewModel.updateMonth(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // set observer for current data
        mainViewModel.currentData.observe(this, {
            for (form in it) println(form)
            formArrayParser = FormArrayParser(it.toCollection(arrayListOf()))
            val pageLayout = findViewById<LinearLayout>(R.id.page)
            val totalLayout = findViewById<LinearLayout>(R.id.total)
            pageLayout.removeAllViews()
            totalLayout.removeAllViews()
            renderForm(-1, pageLayout)
            if (it.isNotEmpty()) {
                totalLayout.addView(totalShow(totalLayout))
            }
        })

        // set observer for current data
        mainViewModel.currentDate.observe(this, {
            val rootLayout = findViewById<LinearLayout>(R.id.date)
            rootLayout.removeAllViews()
            if(it.year != "" || it.month != "" || it.day != "") {
                rootLayout.setPadding(100, 10, 0, 0)
                rootLayout.addView(dateShow(it, rootLayout))
            }
        })

        // button to go to edit page
        val goEdit = findViewById<Button>(R.id.go_edit_button)
        goEdit.setOnClickListener{
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("yearMonth", mainViewModel.currentYearMonth)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderForm(formId: Int, currentLayout: LinearLayout){
        val theForm = formArrayParser.getTheForm(formId)
        theForm?.let {
            val formView = formShow(it, currentLayout)
            currentLayout.addView(formView)
        }

        val childrenArray = formArrayParser.getChildren(formId)
        for (child in childrenArray){
            val childLayout = LinearLayout(this)
            childLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            childLayout.orientation = LinearLayout.VERTICAL
            childLayout.setPadding(100, 10, 0, 0)
            currentLayout.addView(childLayout)
            renderForm(child.id, childLayout)
        }
    }

    private fun formShow(form: Form, root: ViewGroup?): View{
        val formView = LayoutInflater
            .from(this)
            .inflate(R.layout.form_item_show, root, false)
        formView.findViewById<TextView>(R.id.title_show)
            .text = "${form.name}："
        formView.findViewById<TextView>(R.id.number_show)
            .text = form.value
        if(form.type == Form.type_USD){
            formView.findViewById<TextView>(R.id.usd_number_show)
                .text = form.weight
        }else{
            formView.findViewById<LinearLayout>(R.id.usd)
                .visibility = View.GONE
        }
        if (form.note != ""){
            formView.findViewById<TextView>(R.id.note_show)
                .text = form.note
        }else{
            formView.findViewById<LinearLayout>(R.id.note)
                .visibility = View.GONE
        }
        return formView
    }

    private fun dateShow(date: Date, root: ViewGroup?): View{
        val dateView = LayoutInflater
            .from(this)
            .inflate(R.layout.date_show, root, false)
        dateView.findViewById<TextView>(R.id.year_show)
            .text = date.year
        dateView.findViewById<TextView>(R.id.month_show)
            .text = date.month
        dateView.findViewById<TextView>(R.id.day_show)
            .text = date.day
        return dateView
    }

    private fun totalShow(root: ViewGroup?): View{
        val totalView = LayoutInflater
            .from(this)
            .inflate(R.layout.total_show, root, false)
        totalView.findViewById<TextView>(R.id.total_without_usd_show)
            .text = formArrayParser.calculateTotal(true)
        totalView.findViewById<TextView>(R.id.total_show)
            .text = formArrayParser.calculateTotal(false)
        totalView.setPadding(100, 0, 0, 0)
        return totalView
    }
}