package com.example.treasury.edit

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.treasury.MyApplication
import com.example.treasury.R
import com.example.treasury.date.Date
import com.example.treasury.date.DateRepository
import com.example.treasury.form.Form
import com.example.treasury.form.FormArrayParser
import com.example.treasury.form.FormRepository

class EditActivity : AppCompatActivity() {

    private var currentYearMonth = -1
    private lateinit var formRepository: FormRepository
    private lateinit var dateRepository: DateRepository
    private lateinit var editViewModel: EditViewModel

    private lateinit var currentDate: Date
    private var formArrayParser = FormArrayParser(arrayListOf())
    private lateinit var pageLayout: LinearLayout
    private lateinit var totalLayout: LinearLayout

    // < form id, view >
    private var formViewMap = mutableMapOf<Int, View>()
    private var formLayoutMap = mutableMapOf<Int, LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // main layouts
        pageLayout = findViewById(R.id.page)
        totalLayout = findViewById(R.id.total)

        // viewModel
        currentYearMonth = intent.getIntExtra("yearMonth", -1)
        assert(currentYearMonth != -1)
        formRepository = (application as MyApplication).formRepository
        dateRepository = (application as MyApplication).dateRepository
        editViewModel = ViewModelProvider(this, EditViewModelFactory(formRepository, dateRepository, currentYearMonth))
            .get(EditViewModel::class.java)

        editViewModel.originalData.observe(this, {
            formArrayParser = FormArrayParser(it)
            pageLayout.removeAllViews()
            renderForm(-1, pageLayout)

            totalLayout.removeAllViews()
            totalLayout.addView(totalShow(totalLayout))
        })

        editViewModel.originalDate.observe(this, {
            currentDate = it
            val rootLayout = findViewById<LinearLayout>(R.id.date)
            rootLayout.removeAllViews()
            rootLayout.setPadding(100, 10, 0, 0)
            rootLayout.addView(dateEdit(it, rootLayout))
        })

        findViewById<Button>(R.id.save_button)
            .setOnClickListener {
                editViewModel.saveData(currentDate, formArrayParser.exportData())
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
        formLayoutMap[formId] = currentLayout

        theForm?.let {
            val parentLayout = formLayoutMap[theForm.parentId]!!
            currentLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            currentLayout.orientation = LinearLayout.VERTICAL
            currentLayout.setPadding(100, 10, 0, 0)
            parentLayout.addView(currentLayout)

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
            renderForm(child.id, childLayout)
        }
    }

    private fun formEdit(form: Form, root: ViewGroup?, haveChild: Boolean): View{
        val formView = LayoutInflater
            .from(this)
            .inflate(R.layout.form_item_edit, root, false)
        formViewMap[form.id] = formView
        formView.findViewById<TextView>(R.id.title_show)
            .text = "${form.name}："
        val numberShow = formView.findViewById<TextView>(R.id.number_show)
        val numberEdit = formView.findViewById<EditText>(R.id.number_edit)
        numberEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFormValue(form.id, s.toString())
            }
        })
        if (haveChild){
            numberEdit.visibility = View.GONE
            numberShow.text = form.value
        }else{
            numberShow.visibility = View.GONE
            numberEdit.setText(form.value)
        }
        if(form.type == Form.type_USD){
            val usdEdit = formView.findViewById<EditText>(R.id.usd_number_edit)
            usdEdit.setText(form.weight)
            usdEdit.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateFormWeight(form.id, s.toString())
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
                updateFormNote(form.id, s.toString())
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
                currentDate.year = s.toString()
            }
        })
        monthEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentDate.month = s.toString()
            }
        })
        dayEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentDate.day = s.toString()
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
                val newForm = Form(editViewModel.assignId(), parentId, currentYearMonth, Form.type_normal, "1", title, false, true)
                insertForm(newForm)
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
                updateFormName(id, title)
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
          deleteForm(id)
        }
        builder.setNegativeButton("取消") { _, _ ->}
        builder.create().show()
    }

    /************************* Modify Data Area **************************/

    private fun insertForm(form: Form){
        formArrayParser.insert(form)
        val layout = LinearLayout(this)
        renderForm(form.id, layout)
        updateFormValue(form.parentId, null)
    }

    // value is null if I don't wanna change value
    private fun updateFormValue(id: Int, value: String?){
        if (id == -1){
            totalLayout.removeAllViews()
            totalLayout.addView(totalShow(totalLayout))
            return
        }
        value?.let {
            formArrayParser.updateValue(id, value)
        }
        val form = formArrayParser.getTheForm(id)!!
        val haveChild = formArrayParser.getChildren(id).isNotEmpty()
        val formView = formViewMap[id]!!

        val numberShow = formView.findViewById<TextView>(R.id.number_show)
        val numberEdit = formView.findViewById<EditText>(R.id.number_edit)
        if (haveChild){
            numberEdit.visibility = View.GONE
            numberShow.visibility = View.VISIBLE
            numberShow.text = form.value
        }else{
            numberShow.visibility = View.GONE
            numberEdit.visibility = View.VISIBLE
        }
        updateFormValue(form.parentId, null)
    }
    private fun updateFormWeight(id: Int, weight: String){
        formArrayParser.updateWeight(id, weight)
        val form = formArrayParser.getTheForm(id)!!
        updateFormValue(form.id, null)
    }
    private fun updateFormName(id: Int, name: String){
        formArrayParser.updateName(id, name)
        val form = formArrayParser.getTheForm(id)!!
        val formView = formViewMap[id]!!
        val titleShow = formView.findViewById<TextView>(R.id.title_show)
        titleShow.text = form.name+"："
    }
    private fun updateFormNote(id: Int, note: String){
        formArrayParser.updateNote(id, note)
    }
    private fun deleteForm(id: Int){
        val form = formArrayParser.getTheForm(id)!!
        val layout = formLayoutMap[id]!!
        (layout.parent as ViewGroup).removeView(layout)
        formArrayParser.delete(id)
        formViewMap.remove(id)
        formLayoutMap.remove(id)
        updateFormValue(form.parentId, null)
    }
    /*********************************************************************/
}