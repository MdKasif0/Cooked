package com.cooked.app.core.util

sealed interface CookedResult<out T> {
    data class Success<out T>(val data: T) : CookedResult<T>
    data class Error(val throwable: Throwable) : CookedResult<Nothing>
}
