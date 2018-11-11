package com.littlegnal.accounting.ui.main

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.airbnb.mvrx.BaseMvRxActivity
import com.littlegnal.accounting.R
import com.littlegnal.accounting.ui.addedit.AddOrEditMvRxViewModel
import com.littlegnal.accounting.ui.summary.SummaryMvRxViewModel
import javax.inject.Inject

class NewMainActivity : BaseMvRxActivity() {

  @Inject
  lateinit var summaryMvRxViewModelFactory: SummaryMvRxViewModel.Factory
  @Inject
  lateinit var mainMvRxViewModelFactory: MainMvRxViewModel.Factory
  @Inject
  lateinit var addOrEditMvRxViewModelFactory: AddOrEditMvRxViewModel.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_newmain)

    val toolbar = findViewById<Toolbar>(R.id.base_toolbar)
    toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
    setSupportActionBar(toolbar)
    setupActionBarWithNavController(findNavController(R.id.nav_host_fragment))

//    if (savedInstanceState == null) {
//      supportFragmentManager.beginTransaction()
//          .add(R.id.content, MainFragment(), "main")
//          .commit()
//    }
  }

  fun updateTitle(title: CharSequence) {
    findViewById<Toolbar>(R.id.base_toolbar).title = title
  }
}
