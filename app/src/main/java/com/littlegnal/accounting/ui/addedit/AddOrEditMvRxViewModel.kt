package com.littlegnal.accounting.ui.addedit

import android.annotation.SuppressLint
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.littlegnal.accounting.BuildConfig
import com.littlegnal.accounting.db.Accounting
import com.littlegnal.accounting.db.AccountingDao
import com.littlegnal.accounting.ui.main.NewMainActivity
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class AddOrEditMvRxViewModel @AssistedInject constructor(
  @Assisted initialState: AddOrEditMvRxViewState,
  private val accountingDao: AccountingDao
) : BaseMvRxViewModel<AddOrEditMvRxViewState>(initialState, BuildConfig.DEBUG) {

  @AssistedInject.Factory
  interface Factory {
    fun create(initialState: AddOrEditMvRxViewState): AddOrEditMvRxViewModel
  }

  @SuppressLint("SimpleDateFormat")
  private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

  fun loadAccounting(id: Int = -1) {
    if (id == -1) return
    accountingDao.getAccountingById(id)
        .toObservable()
        .subscribeOn(Schedulers.io())
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

  companion object : MvRxViewModelFactory<AddOrEditMvRxViewState> {

    @JvmStatic override fun create(
      activity: FragmentActivity,
      state: AddOrEditMvRxViewState
    ): BaseMvRxViewModel<AddOrEditMvRxViewState> {
      return (activity as NewMainActivity).addOrEditMvRxViewModelFactory.create(state)
    }
  }
}
