package com.littlegnal.accounting.base

import com.airbnb.mvrx.BaseMvRxFragment

abstract class BaseFragment : BaseMvRxFragment() {

  protected val epoxyController by lazy { epoxyController() }

  /**
   * Provide the EpoxyController to use when building models for this Fragment.
   * Basic usages can simply use [simpleController]
   */
  abstract fun epoxyController(): MvRxEpoxyController

  override fun onDestroyView() {
    epoxyController.cancelPendingModelBuild()
    super.onDestroyView()
  }
}
