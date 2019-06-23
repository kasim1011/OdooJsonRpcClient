package io.gripxtech.odoojsonrpcclient.core.persistence.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Table(

    @Expose
    @SerializedName("name")
    var name: String = "",

    @Expose
    @SerializedName("sql")
    var sql: String = "",

    @Expose
    @SerializedName("local_fields")
    var localFields: List<LocalField> = listOf(),

    @Expose
    @SerializedName("fields")
    var fields: List<Field> = listOf(),

    @Expose
    @SerializedName("dependencies")
    var dependencies: ArrayList<Table> = ArrayList(),

    @Expose
    @SerializedName("dependency_tree")
    var dependencyTree: ArrayList<Table> = ArrayList(),

    @Expose
    @SerializedName("sync_order")
    var syncOrder: Int = -1

) : Comparable<Table> {

    val fieldsName: List<String>
        get() = fields.map { it.name }

    val localFieldsName: List<String>
        get() = localFields.map { it.name }

    infix fun dependsOn(table: Table) {
        dependencies.add(table)
    }

    fun calculateDependencyTree(): ArrayList<Table> {
        val dependencies = dependencies
        return if (dependencies.isNotEmpty()) {
            val treeDependencies = ArrayList<Table>()
            for (i in 0 until dependencies.size) {
                val table = dependencies[i]
                treeDependencies.add(table)
                treeDependencies.addAll(table.calculateDependencyTree())
            }
            treeDependencies
        } else {
            arrayListOf()
        }
    }

    override fun compareTo(other: Table): Int {
        val thisHasNoDependencies = this.dependencyTree.isEmpty()
        val otherHasNoDependencies = other.dependencyTree.isEmpty()
        when {
            thisHasNoDependencies && otherHasNoDependencies -> {
                return 0
            }
            thisHasNoDependencies -> {
                return -1
            }
            otherHasNoDependencies -> {
                return 1
            }
        }

        val thisDependsOnOther = this.name in other.dependencyTree.map { it.name }
        val otherDependsOnThis = other.name in this.dependencyTree.map { it.name }
        when {
            thisDependsOnOther && otherDependsOnThis -> {
                return 0
            }
            thisDependsOnOther -> {
                return -1
            }
            otherDependsOnThis -> {
                return 1
            }
        }

        return 0
    }
}
