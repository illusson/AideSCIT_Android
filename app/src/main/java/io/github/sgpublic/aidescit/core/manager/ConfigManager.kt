package io.github.sgpublic.aidescit.core.manager

import android.content.Context
import android.content.SharedPreferences
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.core.module.BaseAPI

object ConfigManager {
    private const val AGREEMENT_SHOWN_KEY = "agreement_shown"

    private const val LOGIN_CHECK_KEY = "is_login"
    private const val USERNAME_KEY = "username"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val TOKEN_EXPIRED_KEY = "token_expired"

    private const val HITOKOTO_SENTENCE_KEY = "hitokoto_sentence"
    private const val HITOKOTO_FROM_KEY = "hitokoto_from"

    private const val SCHOOL_YEAR_KEY = "school_year"
    private const val SEMESTER_KEY = "semester"
    private const val WEEK_KEY = "week"
    private const val SCHEDULE_INQUIRE_CHECK_KEY = "schedule_can_inquire"

    private const val USER_GRADE_KEY = "user_grade"
    private const val USER_NAME_KEY = "user_name"
    private const val USER_FACULTY_KEY = "user_faculty"
    private const val USER_SPECIALTY_KEY = "user_specialty"
    private const val USER_CLASS_KEY = "user_class"

    private const val SCHOOL_YEAR_INQUIRY_KEY = "school_year_inquiry"
    private const val SEMESTER_INQUIRY_KEY = "semester_inquiry"

    private const val EVALUATE_COUNT_KEY = "evaluate_count"

    private const val LAST_EXCEPTION_KEY = "last_exception"

    var AGREEMENT_SHOWN: Long get() = getLong(AGREEMENT_SHOWN_KEY)
        set(value) { putLong(AGREEMENT_SHOWN_KEY, value) }
    fun updateAgreementShown() {
        AGREEMENT_SHOWN = BaseAPI.TS
    }

    var IS_LOGIN: Boolean get() = getBoolean(LOGIN_CHECK_KEY)
        set(value) { putBoolean(LOGIN_CHECK_KEY, value) }
    var USERNAME: String get() = getString(USERNAME_KEY, "")
        set(value) { putString(USERNAME_KEY, value) }
    var ACCESS_TOKEN: String get() = getString(ACCESS_TOKEN_KEY)
        set(value) { putString(ACCESS_TOKEN_KEY, value) }
    var REFRESH_TOKEN: String get() = getString(REFRESH_TOKEN_KEY)
        set(value) { putString(REFRESH_TOKEN_KEY, value) }
    var TOKEN_EXPIRED: Long get() = getLong(TOKEN_EXPIRED_KEY)
        set(value) { putLong(TOKEN_EXPIRED_KEY, value) }
    fun updateToken() {
        TOKEN_EXPIRED = BaseAPI.TS + 2591990L
    }

    var HITOKOTO_SENTENCE: String get() = getString(HITOKOTO_SENTENCE_KEY, "祝你一天好心情哦~")
        set(value) { putString(HITOKOTO_SENTENCE_KEY, value) }
    var HITOKOTO_FROM: String get() = getString(HITOKOTO_FROM_KEY)
        set(value) { putString(HITOKOTO_FROM_KEY, value) }

    var SCHOOL_YEAR: String get() = getString(SCHOOL_YEAR_KEY)
        set(value) { putString(SCHOOL_YEAR_KEY, value) }
    var SEMESTER: Int get() = getInt(SEMESTER_KEY)
        set(value) { putInt(SEMESTER_KEY, value) }
    var WEEK: Int get() = getInt(WEEK_KEY)
        set(value) { putInt(WEEK_KEY, value) }
    var SCHEDULE_CAN_INQUIRE: Boolean get() = getBoolean(SCHEDULE_INQUIRE_CHECK_KEY, true)
        set(value) { putBoolean(SCHEDULE_INQUIRE_CHECK_KEY, value) }

    var USER_GRADE: Int get() = getInt(USER_GRADE_KEY)
        set(value) { putInt(USER_GRADE_KEY, value) }
    var USER_NAME: String get() = getString(USER_NAME_KEY, "此人没有留下姓名……")
        set(value) { putString(USER_NAME_KEY, value) }
    var USER_FACULTY: String get() = getString(USER_FACULTY_KEY)
        set(value) { putString(USER_FACULTY_KEY, value) }
    var USER_SPECIALTY: String get() = getString(USER_SPECIALTY_KEY)
        set(value) { putString(USER_SPECIALTY_KEY, value) }
    var USER_CLASS: String get() = getString(USER_CLASS_KEY)
        set(value) { putString(USER_CLASS_KEY, value) }

    var SCHOOL_YEAR_INQUIRY: String get() = getString(SCHOOL_YEAR_INQUIRY_KEY, SCHOOL_YEAR)
        set(value) { putString(SCHOOL_YEAR_INQUIRY_KEY, value) }
    var SEMESTER_INQUIRY: Int get() = getInt(SEMESTER_INQUIRY_KEY, SEMESTER)
        set(value) { putInt(SEMESTER_INQUIRY_KEY, value) }

    var EVALUATE_COUNT: Int get() = getInt(EVALUATE_COUNT_KEY)
        set(value) { putInt(EVALUATE_COUNT_KEY, value) }

    var LAST_EXCEPTION: String get() = getString(LAST_EXCEPTION_KEY)
        set(value) { putString(LAST_EXCEPTION_KEY, value) }

    private fun getString(key: String, defValue: String = "") =
        sharedPreferences.getString(key, defValue).toString()
    private fun getInt(key: String, defValue: Int = 0) =
        sharedPreferences.getInt(key, defValue)
    private fun getLong(key: String, defValue: Long = 0L) =
        sharedPreferences.getLong(key, defValue)
    private fun getBoolean(key: String, defValue: Boolean = false) =
        sharedPreferences.getBoolean(key, defValue)

    private fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    private fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    private fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    private fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private val sharedPreferences: SharedPreferences get() =
        Application.APPLICATION_CONTEXT.getSharedPreferences("user", Context.MODE_PRIVATE)
            ?: throw NullPointerException("SharedPreferences 'user' not found")
}