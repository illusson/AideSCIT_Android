package io.github.sgpublic.aidescit.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelFragment<VB: ViewBinding, VM: ViewModel>(context: AppCompatActivity)
    : BaseFragment<VB>(context) {
    @Suppress("PropertyName")
    protected abstract val ViewModel: VM

    override fun beforeFragmentCreated() {
        onViewModelSetup()
    }

    protected open fun onViewModelSetup() { }
}