package io.github.vlsi.bugzilla.dbexport

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.vendors.currentDialect

class AnalyticFunction<T>(
    val function: Function<T>,
    val orderByExpressions: List<Pair<Expression<*>, SortOrder>> = listOf(),
) : ExpressionWithColumnType<T>() {
    override val columnType: IColumnType
        get() = function.columnType

    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        function.toQueryBuilder(queryBuilder)
        queryBuilder {
            +" OVER ("
            if (orderByExpressions.isNotEmpty()) {
                +"ORDER BY "
                orderByExpressions.appendTo { (expression, sortOrder) ->
                    currentDialect.dataTypeProvider.precessOrderByClause(this, expression, sortOrder)
                }
            }
            +")"
        }
    }
}

fun <T> Function<T>.over() =
    AnalyticFunction(this)

fun <T> AnalyticFunction<T>.orderBy(column: Expression<*>, order: SortOrder = SortOrder.ASC) =
    AnalyticFunction(this.function, this.orderByExpressions + listOf(column to order))
