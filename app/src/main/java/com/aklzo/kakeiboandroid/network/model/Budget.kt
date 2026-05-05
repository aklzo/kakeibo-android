package com.aklzo.kakeiboandroid.network.model

data class BudgetData(
    val id: Int,
    val month: String?,
    val category: String?,
    val amount: Int
)

data class BudgetsListData(val budgets: List<BudgetData>)
data class BudgetsResponse(val data: BudgetsListData)
data class BudgetItemResponse(val data: BudgetData)

data class CreateBudgetRequest(
    val month: String?,
    val category: String?,
    val amount: Int
)
