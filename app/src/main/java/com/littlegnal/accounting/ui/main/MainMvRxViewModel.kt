package com.littlegnal.accounting.ui.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.littlegnal.accounting.BuildConfig
import com.littlegnal.accounting.R
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetail
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailContent
import com.littlegnal.accounting.ui.main.adapter.MainAccountingDetailHeader
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainMvRxViewModel @AssistedInject constructor(
  @Assisted initialState: MainMvRxState,
  private val accountingDao: AccountingDao,
  private val applicationContext: Context
) : BaseMvRxViewModel<MainMvRxState>(initialState, BuildConfig.DEBUG) {

  @AssistedInject.Factory
  interface Factory {
    fun create(initialState: MainMvRxState): MainMvRxViewModel
  }

  @SuppressLint("SimpleDateFormat")
  private val dateNumFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
  @SuppressLint("SimpleDateFormat")
  private val oneDayFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
  @SuppressLint("SimpleDateFormat")
  private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm")

  init {
      loadList(lastDate = Calendar.getInstance().time)
  }

  fun loadList(
    accountingDetailList: List<MainAccountingDetail> = emptyList(),
    lastDate: Date
  ) {
    accountingDao.queryPreviousAccounting(lastDate, ONE_PAGE_SIZE.toLong())
        .toObservable()
        .map { accountingList ->
          val newAdapterList = accountingDetailList.toMutableList()

          val isFirstPage = newAdapterList.isEmpty()
          if (isFirstPage) {
            newAdapterList.addAll(createFirstPageList(lastDate, accountingList))
//            copy(
//                error = null,
//                isLoading = false,
//                accountingDetailList = newAdapterList,
//                isNoMoreData = accountingList.size < ONE_PAGE_SIZE,
//                isNoData = accountingList.isEmpty()
//            )
          } else {
            newAdapterList.addAll(createAccountingDetailList(lastDate, accountingList))

            newAdapterList.distinctBy {
              if (it is MainAccountingDetailContent) {
                return@distinctBy it.id.toString()
              }

              if ((it is MainAccountingDetailHeader)) {
                return@distinctBy it.title
              }

              ""
            }


//                .let {
//                  previousState.copy(
//                      error = null,
//                      isLoading = false,
//                      accountingDetailList = it,
//                      isNoMoreData = accountingList.size < ONE_PAGE_SIZE,
//                      isNoData = false
//                  )
//                }
          }
          newAdapterList
        }
        .subscribeOn(Schedulers.io())
        .execute {
          when (it) {
            is Success -> {
              val list = it()!!
              copy(
                error = null,
                isLoading = false,
                accountingDetailList = list,
                isNoData = list.isEmpty(),
                isNoMoreData = list.size < ONE_PAGE_SIZE)
            }
            is Fail -> {
              copy(error = it.error, isLoading = false)
            }
            is Loading -> {
              copy(isLoading = true)
            }
            else -> {
              copy()
            }
          }
        }
  }

  private fun createAccountingDetailList(
      lastDate: Date,
      accountingList: List<Accounting>
  ): MutableList<MainAccountingDetail> {
    var lastDateNum: Int = dateNumFormat.format(lastDate)
        .toInt()

    val detailList: MutableList<MainAccountingDetail> = mutableListOf()

    for (accounting in accountingList) {
      val createTime = accounting.createTime
      val accountingDateNum: Int = dateNumFormat.format(createTime)
          .toInt()
      if (accountingDateNum != lastDateNum) {
        detailList.add(createHeader(createTime))
        lastDateNum = accountingDateNum
      }

      detailList.add(createDetailContent(accounting))
    }

    return detailList
  }

  private fun createFirstPageList(
      lastDate: Date,
      accountingList: List<Accounting>
  ): List<MainAccountingDetail> {
    if (accountingList.isEmpty()) return listOf()
    val accountingDetailList = createAccountingDetailList(lastDate, accountingList)
    val maybeHeader: MainAccountingDetail = accountingDetailList[0]
    if (maybeHeader !is MainAccountingDetailHeader) {
      accountingDetailList.add(0, createHeader(lastDate))
    }

    return accountingDetailList
  }

  private fun createHeaderTitle(createTime: Date): String = oneDayFormat.format(createTime)

  private fun createHeader(createTime: Date): MainAccountingDetailHeader {
    val title: String = createHeaderTitle(createTime)
    val sum = accountingDao.sumOfDay(oneDayFormat.format(createTime))
    val sumString: String = applicationContext.getString(
        R.string.main_accounting_detail_header_sum, sum
    )
    return MainAccountingDetailHeader(title, sumString)
  }

  private fun createDetailContent(accounting: Accounting): MainAccountingDetailContent {
    return MainAccountingDetailContent(
        accounting.id,
        applicationContext.getString(R.string.amount_format, accounting.amount),
        accounting.tagName,
        accounting.remarks,
        timeFormat.format(accounting.createTime),
        accounting.createTime
    )
  }

  fun deleteAccounting(accountingDetailList: List<MainAccountingDetail>, deletedId: Int) {
    Observable.fromCallable {
      accountingDao.deleteAccountingById(deletedId)

      val newAccountingList = accountingDetailList.toMutableList()

      val deleteContentIndex: Int = newAccountingList.indexOfLast {
        it is MainAccountingDetailContent && it.id == deletedId
      }

      // 当天只有一条数据的时候把头部也删掉
      var headerIndex = -1
      if (deleteContentIndex > 0 &&
          newAccountingList[deleteContentIndex - 1] is MainAccountingDetailHeader
      ) {
        headerIndex = deleteContentIndex - 1
      }

      var nextHeaderIndex = -1
      if (deleteContentIndex + 1 <= newAccountingList.size - 1 &&
          newAccountingList[deleteContentIndex + 1] is MainAccountingDetailHeader
      ) {
        nextHeaderIndex = deleteContentIndex + 1
      }

      if (((nextHeaderIndex == -1 && deleteContentIndex == newAccountingList.size - 1) &&
              headerIndex != -1) ||
          (headerIndex != -1 && nextHeaderIndex != -1)
      ) {
        newAccountingList.removeAt(deleteContentIndex)
        newAccountingList.removeAt(headerIndex)
      } else {
        findAndUpdateHeader(newAccountingList, deleteContentIndex)
        newAccountingList.removeAt(deleteContentIndex)
      }

      newAccountingList
    }
    .subscribeOn(Schedulers.io())
    .execute {
      when (it) {
        is Loading -> {
          copy(error = null, isLoading = true)
        }
        is Success -> {
          copy(error = null, isLoading = false, accountingDetailList = it()!!)
        }
        is Fail -> {
          copy(error = it.error, isLoading = false)
        }
        else -> { copy() }
      }
    }
  }

  private fun findAndUpdateHeader(
      list: MutableList<MainAccountingDetail>,
      addOrUpdateIndex: Int
  ) {
    list.indexOfLast { it is MainAccountingDetailHeader && list.indexOf(it) < addOrUpdateIndex }
        .apply {
          val createTime = (list[addOrUpdateIndex] as MainAccountingDetailContent).createTime
          val sum = accountingDao.sumOfDay(oneDayFormat.format(createTime))
          val sumString: String = applicationContext.getString(
              R.string.main_accounting_detail_header_sum, sum
          )
          list[this] = (list[this] as MainAccountingDetailHeader).copy(total = sumString)
        }
  }

  companion object : MvRxViewModelFactory<MainMvRxState> {

    const val ONE_PAGE_SIZE = 15

    @JvmStatic override fun create(
      activity: FragmentActivity,
      state: MainMvRxState
    ): BaseMvRxViewModel<MainMvRxState> {
      return (activity as NewMainActivity).mainMvRxViewModelFactory.create(state)
    }


  }
}
