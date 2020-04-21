package com.codepipes.tingadmin.adapters.tableview.models

import com.codepipes.tingadmin.tableview.sort.ISortableModel

class CellModel(
    private val mId: String,
    val data: String
) : ISortableModel {

    override fun getId(): String {
        return mId
    }

    override fun getContent(): String {
        return data
    }
}