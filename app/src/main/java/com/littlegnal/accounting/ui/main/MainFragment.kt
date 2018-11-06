package com.littlegnal.accounting.ui.main

import android.content.DialogInterface
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.DefaultItemDecoration
import com.littlegnal.accounting.base.util.plusAssign
import com.littlegnal.accounting.base.util.toast
import com.littlegnal.accounting.ui.addedit.AddOrEditActivity
import com.littlegnal.accounting.ui.addedit.AddOrEditFragment
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailController
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeaderModel
import com.littlegnal.accounting.ui.summary.SummaryFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.tv_main_accounting_no_data
import kotlinx.android.synthetic.main.fragment_main.fab_main_add_accounting
import kotlinx.android.synthetic.main.fragment_main.rv_main_detail
import java.util.concurrent.TimeUnit

class MainFragment : BaseMvRxFragment() {

  private val mainMvRxViewModel: MainMvRxViewModel by activityViewModel()

  private val accountingDetailController: MainAccountingDetailController by lazy {
    MainAccountingDetailController()
  }

  private lateinit var layoutManager: LinearLayoutManager

  private val disposables = CompositeDisposable()

  private val deleteItemPublisher: PublishSubject<Int> = PublishSubject.create()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_main, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setHasOptionsMenu(true)
    super.onViewCreated(view, savedInstanceState)

    val adapter = accountingDetailController.adapter
    rv_main_detail.adapter = adapter
    layoutManager = LinearLayoutManager(activity)
    rv_main_detail.layoutManager = layoutManager
    rv_main_detail.addItemDecoration(
        DefaultItemDecoration(adapter) { it !is MainAccountingDetailHeaderModel })

    val toolbar = activity?.findViewById<Toolbar>(R.id.base_toolbar)
    toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
    (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
    toolbar?.setOnMenuItemClickListener {
      if (it?.itemId == R.id.menu_summary) {
//        SummaryActivity.go(this)

        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.content, SummaryFragment(), "summary_fragment")
            ?.addToBackStack("back_stack")
            ?.commit()

        return@setOnMenuItemClickListener true
      }
      return@setOnMenuItemClickListener false
    }

    fab_main_add_accounting.setOnClickListener {
      activity?.supportFragmentManager?.beginTransaction()
          ?.add(
              R.id.content,
              AddOrEditFragment().apply {
                arguments = Bundle().apply { putInt(MvRx.KEY_ARG, -1) }
              },
              "add_or_edit_fragment")
          ?.addToBackStack("back_stack")
          ?.commit()
    }

    disposables += accountingDetailController.getItemClickObservable()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          activity?.supportFragmentManager?.beginTransaction()
              ?.add(
                  R.id.content,
                  AddOrEditFragment().apply {
                    arguments = Bundle().apply { putInt(MvRx.KEY_ARG, it) }
                  },
                  "add_or_edit_fragment")
              ?.addToBackStack("back_stack")
              ?.commit()
        }

    disposables += accountingDetailController.getItemLongClickObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { id ->
          val tag = "DeleteConfirmDialog"
          val dialog: DialogFragment = DeleteConfirmDialog().apply {
            okClickListener = DialogInterface.OnClickListener { _, _ ->
              withState(mainMvRxViewModel) {
                mainMvRxViewModel.deleteAccounting(it.accountingDetailList, id)
              }
            }
          }
          val ft = activity?.supportFragmentManager?.beginTransaction()
          val preF = activity?.supportFragmentManager?.findFragmentByTag(tag)
          if (preF != null) {
            ft?.remove(preF)
          }
          ft?.addToBackStack(null)
          ft?.commit()

          dialog.show(activity?.supportFragmentManager, tag)
        }
  }

  override fun onPrepareOptionsMenu(menu: Menu?) {
    super.onPrepareOptionsMenu(menu)

    val menuItem: MenuItem? = menu?.findItem(R.id.menu_summary)
    withState(mainMvRxViewModel) {
      val isMenuEnabled = it.accountingDetailList.isNotEmpty()
      if (menuItem?.isEnabled != isMenuEnabled) {
        val resIcon: Drawable? = resources.getDrawable(R.drawable.ic_show_chart_black_24dp, activity?.theme)
        if (!isMenuEnabled)
          resIcon?.mutate()?.setColorFilter(0xff888888.toInt(), PorterDuff.Mode.SRC_IN)

        menuItem?.isEnabled = isMenuEnabled
        menuItem?.icon = resIcon
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    inflater?.inflate(R.menu.menu, menu)
  }


  override fun invalidate() {
    withState(mainMvRxViewModel) { state ->
      accountingDetailController.setData(state.accountingDetailList, state.isLoading)
      accountingDetailController.isNoMoreData = state.isNoMoreData

      if (state.error != null) {
        activity?.toast(state.error.message.toString())
      }

      if (state.isNoData) {
        if (tv_main_accounting_no_data.visibility == View.GONE) {
          tv_main_accounting_no_data.visibility = View.VISIBLE
        } else {

        }
      } else {
        if (tv_main_accounting_no_data.visibility == View.VISIBLE) {
          tv_main_accounting_no_data.visibility = View.GONE
        }
        activity?.invalidateOptionsMenu()
      }
    }
  }
}
