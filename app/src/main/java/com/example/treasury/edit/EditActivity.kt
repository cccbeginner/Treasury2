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
import com.example.treasury.form.Form
import com.example.treasury.form.FormArrayParser
import com.example.treasury.form.FormRepository

class EditActivity : AppCompatActivity() {

    private var currentYearMonth = -1
    private lateinit var formRepository: FormRepository
    private lateinit var editViewModel: EditViewModel
    private var formArrayParser = FormArrayParser(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // viewModel
        currentYearMonth = intent.getIntExtra("yearMonth", -1)
        assert(currentYearMonth != -1)
        formRepository = (application as MyApplication).formRepository
        editViewModel = ViewModelProvider(this, EditViewModelFactory(formRepository, currentYearMonth))
            .get(EditViewModel::class.java)

        editViewModel.currentData.observe(this, {
            formArrayParser = FormArrayParser(it)
            val rootLayout = findViewById<LinearLayout>(R.id.page)
            rootLayout.removeAllViews()
            renderForm(-1, rootLayout)
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

    var cursor = -1
    //var cursorPlace = -1
    var cursorFormId = -1

    @SuppressLint("SetTextI18n")
    private fun renderForm(formId: Int, currentLayout: LinearLayout){
        val theForm = formArrayParser.getTheForm(formId)
        val childrenArray = formArrayParser.getChildren(formId)

        theForm?.let {
            if (childrenArray.isNotEmpty()) {
                val formView = formEditList(it, currentLayout)
                currentLayout.addView(formView)
            }else{
                val formView = formEdit(it, currentLayout)
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

    private fun formEdit(form: Form, root: ViewGroup?): View{
        val formView = LayoutInflater
            .from(this)
            .inflate(R.layout.form_item_edit, root, false)
        formView.findViewById<TextView>(R.id.title_edit)
            .text = "${form.name}："
        val numberEdit = formView.findViewById<EditText>(R.id.number_edit)
        numberEdit.setText(form.value)
        numberEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                editViewModel.updateFormValue(form.id, s.toString())
                cursor = numberEdit.selectionEnd
                cursorFormId = form.id
            }
        })
        if (form.id == cursorFormId){ //reset cursor
            numberEdit.isFocusable = true
            numberEdit.isFocusableInTouchMode = true
            numberEdit.requestFocus()
            numberEdit.setSelection(cursor)
            cursor = -1
            cursorFormId = -1
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
        if (form.parentId == -1){
            deleteButton.visibility = View.GONE
        }else {
            deleteButton.setOnClickListener {
                deleteDialog(form.id, true)
            }
        }
        formView.findViewById<Button>(R.id.add_button)
            .setOnClickListener {
                insertDialog(form.id)
            }
        formView.findViewById<Button>(R.id.update_name_button)
            .setOnClickListener {
                updateNameDialog(form.id)
            }
        return formView
    }

    private fun formEditList(form: Form, root: ViewGroup?): View{
        val formView = LayoutInflater
            .from(this)
            .inflate(R.layout.form_item_edit_list, root, false)
        formView.findViewById<TextView>(R.id.title_edit)
            .text = "${form.name}："
        formView.findViewById<TextView>(R.id.number_edit)
            .text = form.value
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
        if (form.parentId == -1){
            deleteButton.visibility = View.GONE
        }else {
            deleteButton.setOnClickListener {
                deleteDialog(form.id, false)
            }
        }
        formView.findViewById<Button>(R.id.add_button)
            .setOnClickListener {
                insertDialog(form.id)
            }
        formView.findViewById<Button>(R.id.update_name_button)
            .setOnClickListener {
                updateNameDialog(form.id)
            }
        return formView
    }

    private fun insertDialog(parentId: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editText = EditText(this) //final一個editText
        builder.setView(editText)
        builder.setTitle("輸入名稱")
        builder.setPositiveButton("確定") { _, _ ->
            val title = editText.text.toString().replace("\\s+".toRegex(), " ")
            if (title != "" && title != " "){
                val newForm = Form(editViewModel.assignId(), parentId, currentYearMonth, title)
                editViewModel.insertForm(newForm)
            }
        }
        builder.setNegativeButton("取消") { _, _ ->}
        builder.create().show()
    }
    private fun updateNameDialog(id: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editText = EditText(this) //final一個editText
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