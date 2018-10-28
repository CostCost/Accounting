/*
 * Copyright (C) 2017 littlegnal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlegnal.accounting.ui.addedit

import androidx.lifecycle.ViewModel
import com.littlegnal.accounting.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [AddOrEditModule::class])
abstract class AddOrEditBuilderModule {

  @ContributesAndroidInjector
  abstract fun contributeAddOrEditActivity(): AddOrEditActivity

  @Binds
  @IntoMap
  @ViewModelKey(AddOrEditViewModel::class)
  abstract fun bindAddOrEditModel(addOrEditViewModel: AddOrEditViewModel): ViewModel
}
