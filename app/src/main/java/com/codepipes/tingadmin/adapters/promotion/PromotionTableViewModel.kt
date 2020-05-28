package com.codepipes.tingadmin.adapters.promotion

import android.annotation.SuppressLint
import com.codepipes.tingadmin.adapters.tableview.models.CellModel
import com.codepipes.tingadmin.adapters.tableview.models.ColumnHeaderModel
import com.codepipes.tingadmin.adapters.tableview.models.RowHeaderModel
import com.codepipes.tingadmin.models.MenuPromotion
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions

class PromotionTableViewModel {

    var columnHeaderModeList: List<ColumnHeaderModel>? = null
        private set
    var rowHeaderModelList: List<RowHeaderModel>? = null
        private set
    var cellModelList: List<List<CellModel>>? = null
        private set

    private fun createColumnHeaderModelList(): List<ColumnHeaderModel> {

        val list: MutableList<ColumnHeaderModel> = arrayListOf<ColumnHeaderModel>()

        list.add(ColumnHeaderModel("No"))
        list.add(ColumnHeaderModel("Occasion"))
        list.add(ColumnHeaderModel("Menu"))
        list.add(ColumnHeaderModel("Day/Date"))
        list.add(ColumnHeaderModel("Reduction"))
        list.add(ColumnHeaderModel("Supplement"))
        list.add(ColumnHeaderModel("Is On"))
        list.add(ColumnHeaderModel("Actions"))

        return list
    }

    @SuppressLint("DefaultLocale")
    private fun createCellModelList(promotions: List<MenuPromotion>): List<List<CellModel>> {

        val lists: MutableList<List<CellModel>> = ArrayList()

        for (i in promotions.indices) {
            val promotion = promotions[i]
            val list: MutableList<CellModel> = ArrayList()

            val promotionMenu = when(promotion.promotionItem.type.id) {
                4 -> "${promotion.promotionItem.menu?.menu?.images!!.images[0].image}::${promotion.promotionItem.menu.menu.name}"
                5 -> "${promotion.promotionItem.category?.image}::${promotion.promotionItem.category?.name}"
                else -> promotion.promotionItem.type.name
            }

            val promotionPeriod = if(promotion.period.isSpecial) {
                "${UtilsFunctions.formatDate(promotion.period.startDate!!, false)} - ${UtilsFunctions.formatDate(promotion.period.endDate!!, false)}"
            } else { promotion.period.periods.map { Constants.PROMOTION_PERIOD[it] }.joinToString(", ") }

            val promotionSupplement = if(promotion.supplement.hasSupplement) {
                if(promotion.supplement.isSame) { "${promotion.supplement.quantity}, Same Menu" }
                else { "${promotion.supplement.supplement?.menu?.images!!.images[0].image}::${promotion.supplement.quantity} ${promotion.supplement.supplement.menu.name}" }
            } else { "None" }

            list.add(CellModel("1-promo-$i", "${i + 1}"))
            list.add(CellModel("2-promo-$i", promotion.occasionEvent))
            list.add(CellModel("3-promo-$i", promotionMenu))
            list.add(CellModel("4-promo-$i", promotionPeriod))
            list.add(CellModel("5-promo-$i", if(promotion.reduction.hasReduction){ "${promotion.reduction.amount} ${promotion.reduction.reductionType}" } else { "None" }))
            list.add(CellModel("6-promo-$i", promotionSupplement))
            list.add(CellModel("7-promo-$i", if(promotion.isOn){ "YES" } else { "NO" }))
            list.add(CellModel("8-promo-$i", promotion.id.toString()))

            lists.add(list)
        }
        return lists
    }

    private fun createRowHeaderList(size: Int): List<RowHeaderModel> {
        val list: MutableList<RowHeaderModel> = ArrayList()
        for (i in 0 until size) { list.add(RowHeaderModel((i + 1).toString())) }
        return list
    }

    public fun generateListForTableView(promotions: List<MenuPromotion>) {
        columnHeaderModeList = createColumnHeaderModelList()
        cellModelList = createCellModelList(promotions)
        rowHeaderModelList = createRowHeaderList(promotions.size)
    }

    companion object {
        public fun getCellItemViewType(column: Int): Int {
            return when (column) {
                2 -> Constants.PROMOTION_TABLE_VIEW_CELL
                5 -> Constants.PROMOTION_TABLE_VIEW_CELL
                7 -> Constants.ACTIONS_TABLE_VIEW_CELL
                else -> Constants.NORMAL_TABLE_VIEW_CELL
            }
        }
    }
}