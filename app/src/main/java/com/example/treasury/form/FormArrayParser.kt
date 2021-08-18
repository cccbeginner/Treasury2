package com.example.treasury.form

import java.math.BigDecimal

class FormArrayParser (initialFormArray: ArrayList<Form>) {

    // idMap <id, form>
    private val idMap = mutableMapOf<Int, Form>()

    // childrenMap <parentId, forms>
    private val childrenMap = mutableMapOf<Int, ArrayList<Form>>()

    init {
        // formArray => maps
        // insert everything and recalculate
        for (form in initialFormArray){
            insert(form)
        }
        recalculate()
    }

    /*
     * define getters
     */
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

    /*
     * recalculate all these form values
     * (by updateValues from all leaves)
     */
    private fun recalculate(){
        for (kv in idMap){
            if(childrenMap[kv.key] == null){
                val form = kv.value
                updateValue(form.id, form.value)
            }
        }
    }

    /*
     * Check if no conflict before insert & update
     */
    fun noConflict(form: Form): Boolean{
        childrenMap[form.parentId]?.let {
            for (theForm in childrenMap[form.parentId]!!){
                if (form.name == theForm.name && form.id != theForm.id){
                    return false
                }
            }
        }
        return true
    }

    fun stringToDecimal(string: String): BigDecimal{
        if (string == "")return BigDecimal.ZERO
        else return BigDecimal(string)
    }

    /*
     * insert a form and maintain the structure as well
     */
    fun insert(form: Form){
        if (!noConflict(form))return
        idMap[form.id] = form
        if(childrenMap[form.parentId] != null){
            childrenMap[form.parentId]!!.add(form)
        }else{
            childrenMap[form.parentId] = arrayListOf(form)
        }
        updateValue(form.id, form.value)
    }

    /*
     * update value for the form and its ancestors
     */
    fun updateValue(id : Int, value : String){
        idMap[id]?.let{
            it.value = (stringToDecimal(value) * it.weightDecimal()).toString()
            val formList = childrenMap[it.parentId]
            var sum = BigDecimal.ZERO
            if (formList != null) {
                for (form in formList){
                    if(form.value != "") {
                        sum += form.valueDecimal()
                    }
                }
            }
            updateValue(it.parentId, sum.toString())
        }
    }

    /*
     * Just update name, you know
     */
    fun updateName(id : Int, newName : String){
        idMap[id]?.let {
            val oldName = it.name
            it.name = newName
            if (!noConflict(it)){
                it.name = oldName
                return
            }
            for (form in childrenMap[it.parentId]!!){
                if(form.id == it.id){
                    form.name = newName
                    break
                }
            }
        }
    }
    /*
     * Just update note, you know
     */
    fun updateNote(id : Int, newNote : String){
        idMap[id]?.let {
            it.note = newNote
            for (form in childrenMap[it.parentId]!!){
                if(form.id == it.id){
                    form.note = newNote
                    break
                }
            }
        }
    }
    /*
     * Just update note, you know
     */
    fun updateWeight(id : Int, newWeight : String){
        idMap[id]?.let {
            it.weight = newWeight
            for (form in childrenMap[it.parentId]!!){
                if(form.id == it.id){
                    form.weight = newWeight
                    break
                }
            }
            updateValue(id, it.value)
        }
    }


    /*
     * Delete a form
     */
    fun delete(id: Int){
        updateValue(id, "0")
        idMap[id]?.let{
            for (child in getChildren(it.id)){
                delete(child.id)
            }
            childrenMap[it.parentId]!!.remove(it)
            if (childrenMap[it.parentId]!!.isEmpty()){
                childrenMap.remove(it.parentId)
            }
        }
        idMap.remove(id)
    }

    /*
     * Update id, this could be the most complicated part
     */
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

    /*
     * Clear forms' notes and values
     */
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

    /*
     * Calculate total value ( without usd ).
     */
    fun calculateTotal(withoutUSD: Boolean): String{
        var sum = BigDecimal.ZERO
        if (childrenMap[-1] != null) {
            for (form in childrenMap[-1]!!) {
                if (withoutUSD && form.type == Form.type_USD) {
                    continue
                }
                sum += form.valueDecimal()
            }
        }
        return sum.toString()
    }

    /*
     * Export Data as an ArrayList
     */
    fun exportData(): ArrayList<Form>{
        val ret = ArrayList<Form>()
        for (kv in idMap){
            ret.add(kv.value)
        }
        return ret
    }
}