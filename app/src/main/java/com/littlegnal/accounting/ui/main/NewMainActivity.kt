package com.littlegnal.accounting.ui.main

import android.os.Bundle
import com.airbnb.mvrx.BaseMvRxActivity
import com.littlegnal.accounting.R
import com.littlegnal.accounting.base.BaseActivity
import com.littlegnal.accounting.ui.summary.SummaryMvRxViewModel
import kotlinx.android.synthetic.main.activity_newmain.view.content
import javax.inject.Inject

class NewMainActivity : BaseMvRxActivity() {

  @Inject
  lateinit var summaryMvRxViewModelFactory: SummaryMvRxViewModel.Factory
  @Inject
  lateinit var mainMvRxViewModelFactory: MainMvRxViewModel.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_newmain)

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(R.id.content, MainFragment(), "main")
          .commit()
    }
  }
}
