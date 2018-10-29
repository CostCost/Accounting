package com.littlegnal.accounting.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.summary.adapter.SummaryListController
import com.littlegnal.accounting.ui.summary.adapter.SummaryListItemModel
import kotlinx.android.synthetic.main.activity_summary.cv_summary_chart
import kotlinx.android.synthetic.main.activity_summary.rv_summary_list

class SummaryFragment : BaseMvRxFragment() {

  private val summaryMvRxViewModel: SummaryMvRxViewModel by fragmentViewModel()

  private val summaryListController: SummaryListController by lazy { SummaryListController() }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.activity_summary, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    rv_summary_list.adapter = summaryListController.adapter
    rv_summary_list.layoutManager = LinearLayoutManager(activity)
    rv_summary_list.addItemDecoration(
        DefaultItemDecoration(summaryListController.adapter) { it is SummaryListItemModel })
  }

  override fun invalidate() {
    withState(summaryMvRxViewModel) { state ->
      if (state.error != null) {
        activity?.toast(state.error.message.toString())
        return@withState
      }

      summaryListController.setData(state.summaryItemList)
      cv_summary_chart.points = state.summaryChartData.points
      cv_summary_chart.months = state.summaryChartData.months
      cv_summary_chart.values = state.summaryChartData.values
      cv_summary_chart.selectedIndex = state.summaryChartData.selectedIndex
      cv_summary_chart.postInvalidate()

    }
  }

}
