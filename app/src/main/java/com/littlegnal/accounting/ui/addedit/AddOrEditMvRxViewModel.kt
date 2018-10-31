package com.littlegnal.accounting.ui.addedit

import android.annotation.SuppressLint
import android.os.Build
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.littlegnal.accounting.BuildConfig
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import io.reactivex.Observable
import java.text.SimpleDateFormat

class AddOrEditMvRxViewModel(
  initialState: AddOrEditMvRxViewState,
  private val accountingDao: AccountingDao
) : BaseMvRxViewModel<AddOrEditMvRxViewState>(initialState, BuildConfig.DEBUG) {

  @SuppressLint("SimpleDateFormat")
  private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

  fun loadAccounting(id: Int = -1) {
    if (id == -1) return
    accountingDao.getAccountingById(id)
        .toObservable()
        .execute {
          when (it) {
            is Loading -> {
              copy(isLoading = true, error = null)
            }
            is Success -> {
              val accounting = it()!!
              copy(
                isLoading = false,
                error = null,
                amount = accounting.amount.toString(),
                tagName = accounting.tagName,
                dateTime = dateTimeFormat.format(accounting.createTime),
                remarks = accounting.remarks
              )
            }
            is Fail -> {
              copy(isLoading = false, error = it.error)
            }
            else -> { copy() }
          }
        }
  }

  fun createAccounting(
    amount: Float,
    tagName: String,
    showDate: String,
    remarks: String?
  ) {
    Observable.fromCallable {
      val accounting = Accounting(
          amount,
          dateTimeFormat.parse(showDate),
          tagName,
          remarks
      )
      val insertedId = accountingDao.insertAccounting(accounting)
      accounting.id = insertedId.toInt()
    }
  }
}
