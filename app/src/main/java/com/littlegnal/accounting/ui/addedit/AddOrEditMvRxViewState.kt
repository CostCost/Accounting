package com.littlegnal.accounting.ui.addedit

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState

data class AddOrEditMvRxViewState(
    val isLoading: Boolean,
    val error: Throwable?,
    @PersistState val amount: String? = null,
    @PersistState val tagName: String? = null,
    @PersistState val dateTime: String? = null,
    @PersistState val remarks: String? = null
) : MvRxState