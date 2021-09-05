package com.example.treasury.edit

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.treasury.MyApplication
import com.example.treasury.R
import com.example.treasury.date.Date
import com.example.treasury.date.DateRepository
import com.example.treasury.form.Form
import com.example.treasury.form.FormArrayParser
import com.example.treasury.form.FormRepository
import java.lang.Math.min

class EditActivity : AppCompatActivity() {

    private var currentYearMonth = -1
    private lateinit var formRepository: FormRepository
    private lateinit var dateRepository: DateRepository
    private lateinit var editViewModel: EditViewModel
    private var formArrayParser = FormArrayParser(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // viewModel
        currentYearMonth = intent.getIntExtra("yearMonth", -1)
        assert(currentYearMonth != -1)
        formRepository = (application as MyApplication).formRepository
        dateRepository = (application as MyApplication).dateRepository
        editViewModel = ViewModelProvider(this, EditViewModelFactory(formRepository, dateRepository, currentYearMonth))
            .get(EditViewModel::class.java)

        editViewModel.currentData.observe(this, {
            formArrayParser = FormArrayParser(it)
            val pageLayout = findViewById<LinearLayout>(R.id.page)
            val totalLayout = findViewById<LinearLayout>(R.id.total)
            pageLayout.removeAllViews()
            totalLayout.removeAllViews()
            renderForm(-1, pageLayout)
            if (it.isNotEmpty()) {
                totalLayout.addView(totalShow(totalLayout))
            }
        })

        editViewModel.currentDate.observe(this, {
            val rootLayout = findViewById<LinearLayout>(R.id.date)
            rootLayout.removeAllViews()
            rootLayout.setPadding(100, 10, 0, 0)
            rootLayout.addView(dateEdit(it, rootLayout))
        })

        findViewById<Button>(R.id.save_button)
            .setOnClickListener {
                editViewModel.saveData()
                finish()
            }
        findViewById<Button>(R.id.cancel_button)
            .setOnClickListener {
                finish()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun renderForm(formId: Int, currentLayout: LinearLayout){
        val theForm = formArrayParser.getTheForm(formId)
        val childrenArray = formArrayParser.getChildren(formId)

        theForm?.let {
            if (childrenArray.isNotEmpty()) {
                val formView = formEdit(it, currentLayout, true)
                currentLayout.addView(formView)
            }else{
                val formView = formEdit(it, currentLayout, false)
                currentLayout.addView(formView)
            }
        }

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

    private fun formEdit(form: Form, root: ViewGroup?, haveChild: Boolean): View{
        val formView = LayoutInflater
            .from(this)
            .inflate(R.layout.form_item_edit, root, false)
        formView.findViewById<TextView>(R.id.title_show)
            .text = "${form.name}："
        val numberShow = formView.findViewById<TextView>(R.id.number_show)
        val numberEdit = formView.findViewById<EditText>(R.id.number_edit)
        if (haveChild){
            numberEdit.visibility = View.GONE
            numberShow.text = form.value
        }else{
            numberShow.visibility = View.GONE
            numberEdit.setText(form.value)
            numberEdit.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    editViewModel.updateFormValue(form.id, s.toString())
                }
            })
        }
        if(form.type == Form.type_USD){
            val usdEdit = formView.findViewById<EditText>(R.id.usd_number_edit)
            usdEdit.setText(form.weight)
            usdEdit.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    editViewModel.updateFormWeight(form.id, s.toString())
                }
            })
        }else{
            formView.findViewById<LinearLayout>(R.id.usd)
                .visibility = View.GONE
        }
        val noteEdit = formView.findViewById<EditText>(R.id.note_edit)
        noteEdit.setText(form.note)
        noteEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                editViewModel.updateFormNote(form.id, s.toString())
            }
        })
        val deleteButton = formView.findViewById<ImageButton>(R.id.delete_button)
        val addButton = formView.findViewById<Button>(R.id.add_button)
        val updateNameButton = formView.findViewById<Button>(R.id.update_name_button)
        if (form.canBeModify){
            deleteButton.setOnClickListener {
                deleteDialog(form.id, !haveChild)
            }
            updateNameButton.setOnClickListener {
                updateNameDialog(form.id, form.name)
            }
        }else {
            deleteButton.visibility = View.GONE
            updateNameButton.visibility = View.GONE
        }
        if (form.canBeAList) {
            addButton.setOnClickListener {
                insertDialog(form.id)
            }
        }else{
            addButton.visibility = View.GONE
        }
        return formView
    }

    private fun dateEdit(date: Date, root: ViewGroup?): View{
        val dateView = LayoutInflater
            .from(this)
            .inflate(R.layout.date_edit, root, false)
        val yearEdit = dateView.findViewById<EditText>(R.id.year_edit)
        val monthEdit = dateView.findViewById<EditText>(R.id.month_edit)
        val dayEdit = dateView.findViewById<EditText>(R.id.day_edit)
        yearEdit.setText(date.year)
        monthEdit.setText(date.month)
        dayEdit.setText(date.day)
        yearEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                editViewModel.updateDateYear(s.toString())
            }
        })
        monthEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                editViewModel.updateDateMonth(s.toString())
            }
        })
        dayEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                editViewModel.updateDateDay(s.toString())
            }
        })
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

    private fun insertDialog(parentId: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editText = EditText(this) //final一個editText
        builder.setView(editText)
        builder.setTitle("輸入名稱")
        builder.setPositiveButton("確定") { _, _ ->
            val title = editText.text.toString().replace("\\s+".toRegex(), " ")
            if (title != "" && title != " "){
                val newForm = Form(editViewModel.assignId(), parentId, currentYearMonth, Form.type_normal, title, false, true)
                editViewModel.insertForm(newForm)
            }
        }
        builder.setNegativeButton("取消") { _, _ ->}
        builder.create().show()
    }
    private fun updateNameDialog(id: Int, oldName: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editText = EditText(this) //final一個editText
        editText.setText(oldName)
        builder.setView(editText)
        builder.setTitle("輸入新名稱")
        builder.setPositiveButton("確定") { _, _ ->
            val title = editText.text.toString().replace("\\s+".toRegex(), " ")
            if (title != "" && title != " "){
                editViewModel.updateFormName(id, title)
            }
        }
        builder.setNegativeButton("取消") { _, _ ->}
        builder.create().show()
    }
    private fun deleteDialog(id: Int, onlyOne: Boolean) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        if (onlyOne){
            builder.setTitle("確認刪除？")
        }else{
            builder.setTitle("確認刪除？將會連同其底下包含的資料一起刪除。")
        }
        builder.setPositiveButton("確定") { _, _ ->
          editViewModel.deleteForm(id)
        }
        builder.setNegativeButton("取消") { _, _ ->}
        builder.create().show()
    }
}