package io.gripxtech.odoojsonrpcclient.core.persistence

import timber.log.Timber
import java.util.*

interface OdooModel {

    companion object {
        const val LocalId = "_id"
        const val Id = "id"
        const val CreateDate = "create_date"
        const val WriteDate = "write_date"
        const val LocalWriteDate = "_write_date"
        const val LocalDirty = "_dirty"
        const val LocalActive = "_active"
    }

    // Following properties must override in constructor
    var localId: Long
    var id: Long
    var createDate: String
    var writeDate: String
    var localWriteDate: String
    var localDirty: Boolean
    var localActive: Boolean

    fun getFields(): List<String> {
        val fields = arrayListOf<String>()

        val clazz = this::class.java
        val declaredFields = clazz.declaredFields
        for (declaredField in declaredFields) {
            val annotations = declaredField.annotations
            Timber.i("annotations: ${Arrays.toString(annotations)}")
        }
        return fields
    }
}