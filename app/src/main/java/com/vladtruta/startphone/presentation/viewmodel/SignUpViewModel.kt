package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import org.joda.time.DateTime

class SignUpViewModel : ViewModel() {
    companion object {
        enum class GENDER(val value: Char) {
            MALE('M'), FEMALE('F'), OTHER('O')
        }
    }

    fun getGenderFromId(id: Int): Char {
        return when (id) {
            1 -> GENDER.MALE.value
            2 -> GENDER.FEMALE.value
            else -> GENDER.OTHER.value
        }
    }

    fun getDateTimeFromParameters(year: Int, month: Int, dayOfMonth: Int): DateTime {
        return DateTime(year, month, dayOfMonth, 0, 0)
    }
}