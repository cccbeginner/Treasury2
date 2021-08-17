package com.example.treasury.form

import java.math.BigDecimal

class FormArrayParser (initialFormArray: ArrayList<Form>) {

    // idMap <id, form>
    private val idMap = mutableMapOf<Int, Form>()

    // childrenMap <parentId, forms>
    private val childrenMap = mutableMapOf<Int, ArrayList<Form>>()

    init {
        // formArray => maps
        for (form in initialFormArray){
            insert(form)
        }
        recalculate()
    }

    fun getTheForm(id : Int): Form?{
        return idMap[id]
    }
    fun getChildren(parentId : Int): ArrayList<Form>{
        return if (childrenMap[parentId] != null){
            childrenMap[parentId]!!
        }else{
            arrayListOf()
        }
    }

    private fun recalculate(){
        for (kv in idMap){
            if(childrenMap[kv.key] == null){
                val form = kv.value
                updateValue(form.id, form.value)
            }
        }
    }
    fun noRepeat(form: Form): Boolean{
        childrenMap[form.parentId]?.let {
            for (theForm in childrenMap[form.parentId]!!){
                if (form.name == theForm.name){
                    return false
                }
            }
        }
        return true
    }
    fun insert(form: Form){
        if (!noRepeat(form))return
        idMap[form.id] = form
        if(childrenMap[form.parentId] != null){
            childrenMap[form.parentId]!!.add(form)
        }else{
            childrenMap[form.parentId] = arrayListOf(form)
        }
        updateValue(form.id, form.value)
    }
    fun updateValue(id : Int, value : String){
        val currentForm = idMap[id]
        currentForm?.let{
            it.value = value
            val formList = childrenMap[currentForm.parentId]
            var sum = BigDecimal("0")
            if (formList != null) {
                for (form in formList){
                    if(form.value != "") {
                        sum += BigDecimal(form.value)
                    }
                }
            }
            updateValue(currentForm.parentId, sum.toString())
        }
    }
    fun updateName(id : Int, newName : String){
        val currentForm = idMap[id]
        currentForm?.let {
            val oldName = currentForm.name
            currentForm.name = newName
            if (!noRepeat(currentForm)){
                currentForm.name = oldName
                return
            }
            for (form in childrenMap[currentForm.parentId]!!){
                if(form.id == currentForm.id){
                    form.name = newName
                    break
                }
            }
        }
    }
    fun updateNote(id : Int, newNote : String){
        val currentForm = idMap[id]
        currentForm?.let {
            currentForm.note = newNote
            for (form in childrenMap[currentForm.parentId]!!){
                if(form.id == currentForm.id){
                    form.note = newNote
                    break
                }
            }
        }
    }
    fun delete(id: Int){
        updateValue(id, "0")
        val form = idMap[id]
        form?.let{
            for (child in getChildren(form.id)){
                delete(child.id)
            }
            childrenMap[form.parentId]!!.remove(form)
            if (childrenMap[form.parentId]!!.isEmpty()){
                childrenMap.remove(form.parentId)
            }
        }
        idMap.remove(id)
    }
    fun updateId(preId: Int, curId: Int){
        assert(idMap[preId] != null)
        assert(idMap[curId] == null)
        idMap[preId]!!.id = curId
        idMap[curId] = idMap[preId]!!
        idMap.remove(preId)
        val theForm = idMap[curId]!!
        for (form in childrenMap[theForm.parentId]!!) {
            if (form.id == preId)form.id = curId
        }
        if (childrenMap[preId] != null) {
            for (form in childrenMap[preId]!!) {
                form.parentId = curId
                idMap[form.id]!!.parentId = curId
            }
            childrenMap[curId] = childrenMap[preId]!!
            childrenMap.remove(preId)
        }
    }
    fun clearContent(){
        for (kv in idMap){
            kv.value.note = ""
            kv.value.value = ""
        }
        for (kv in childrenMap){
            for (form in kv.value){
                form.note = ""
                form.value = ""
            }
        }
    }
    fun exportData(): ArrayList<Form>{
        val ret = ArrayList<Form>()
        for (kv in idMap){
            ret.add(kv.value)
        }
        return ret
    }
}