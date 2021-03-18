@file:Suppress("ConvertTryFinallyToUseCall")

package com.sgpublic.scit.tool.manager

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import org.json.JSONException
import java.util.*

class CalendarManager(val context: Context) {
    companion object{
        private const val CALENDAR_URL = "content://com.android.calendar/calendars"
        private const val CALENDAR_EVENT_URL = "content://com.android.calendar/events"
        private const val CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders"
        val EVENT_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,  // 0
            CalendarContract.Calendars.ACCOUNT_NAME,  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,  // 2
            CalendarContract.Calendars.OWNER_ACCOUNT // 3
        )

        private const val CALENDARS_NAME = "cgk_tool"
        private const val CALENDARS_ACCOUNT_NAME = "sgpublic@scit.com"
        private const val CALENDARS_ACCOUNT_TYPE = "LOCAL"
        private const val CALENDARS_DISPLAY_NAME = "工科助手日历账户"

        val CLASS_WINTER = arrayOf(
            arrayOf(
                arrayOf("/08/20", "/10/00"), arrayOf("/10/20", "/12/00"), arrayOf("/14/00", "/15/40"), arrayOf("/16/00", "/17/40"), arrayOf("/19/00", "/20/40")
            ),
            arrayOf(
                arrayOf("/08/20", "/10/00"), arrayOf("/10/20", "/12/00"), arrayOf("/13/10", "/14/50"), arrayOf("/15/00", "/16/40"), arrayOf("/19/00", "/20/40")
            )
        )
        val CLASS_SUMMER = arrayOf(
            arrayOf(
                arrayOf("/08/20", "/10/00"), arrayOf("/10/20", "/12/00"), arrayOf("/14/30", "/16/10"), arrayOf("/16/20", "/18/00"), arrayOf("/19/20", "/21/00")
            ),
            arrayOf(
                arrayOf("/08/20", "/10/00"), arrayOf("/10/20", "/12/00"), arrayOf("/13/10", "/14/50"), arrayOf("/15/00", "/16/40"), arrayOf("/19/00", "/20/40")
            )
        )
        val CLASS_DESCRIPTION = arrayOf(
            "第1 - 2节", "第3 - 4节", "第5 - 6节", "第7 - 8节", "第9 - 10节"
        )

        private val selectionArgs = arrayOf(
            CALENDARS_ACCOUNT_NAME,
            CALENDARS_ACCOUNT_TYPE,
            CALENDARS_ACCOUNT_NAME
        )
        private const val selection = ("((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))")
    }
    
    private var CalendarID: Long = 0L
    private var preRemindTime: Int = 15

    fun setPreRemindTime(time: Int){
        preRemindTime = time
    }

    fun checkCalendarAccount(): Long {
        val userCursor: Cursor? = context.contentResolver.query(
            Uri.parse(CALENDAR_URL), EVENT_PROJECTION, selection, selectionArgs, null
        )
        return try {
            if (userCursor == null) return -2
            val count = userCursor.count
            if (count > 0) {
                userCursor.moveToFirst()
                CalendarID = userCursor
                    .getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID))
                    .toLong()
                CalendarID
            } else {
                -1
            }
        } finally {
            userCursor?.close()
        }
    }

    fun addCalendarAccount(): Long {
        val timeZone = TimeZone.getDefault()
        val value = ContentValues()
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME)
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME)
        value.put(CalendarContract.Calendars.VISIBLE, 1)
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE)
        value.put(
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.CAL_ACCESS_OWNER
        )
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME)
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)
        var calendarUri = Uri.parse(CALENDAR_URL)
        calendarUri = calendarUri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_NAME,
                CALENDARS_ACCOUNT_NAME
            )
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CALENDARS_ACCOUNT_TYPE
            )
            .build()
        val result = context.contentResolver.insert(calendarUri, value)
        CalendarID = if (result == null) -1 else ContentUris.parseId(result)
        return CalendarID
    }

    fun addCalendarEvent(dtStart: String, dtEnd: String, title: String, description: String, location: String) {
        val event = ContentValues()
        event.put("title", title)
        event.put("description", description)
        // 插入账户
        event.put("calendar_id", CalendarID)
        event.put("eventLocation", location)
        val mCalendar = Calendar.getInstance()
        //提醒开始时间
        val startTime = dtStart.split("/").toTypedArray()
        mCalendar[Calendar.YEAR] = startTime[0].toInt() //年
        mCalendar[Calendar.MONTH] = startTime[1].toInt() - 1 //月
        mCalendar[Calendar.DAY_OF_MONTH] = startTime[2].toInt() //日
        mCalendar[Calendar.HOUR_OF_DAY] = startTime[3].toInt() //时
        mCalendar[Calendar.MINUTE] = startTime[4].toInt() //分
        val start = mCalendar.time.time

        //提醒结束时间
        val endTime = dtEnd.split("/").toTypedArray()
        mCalendar[Calendar.YEAR] = endTime[0].toInt() //年
        mCalendar[Calendar.MONTH] = endTime[1].toInt() - 1 //月
        mCalendar[Calendar.DAY_OF_MONTH] = endTime[2].toInt() //日
        mCalendar[Calendar.HOUR_OF_DAY] = endTime[3].toInt() //时
        mCalendar[Calendar.MINUTE] = endTime[4].toInt() //分
        val end = mCalendar.time.time
        event.put("dtstart", start)
        event.put("dtend", end)
        if (preRemindTime != 0) {
            event.put("hasAlarm", 1)
            event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Beijing")
            val newEvent = context.contentResolver
                .insert(Uri.parse(CALENDAR_EVENT_URL), event)
            val event_id = newEvent!!.lastPathSegment!!.toLong()
            val values = ContentValues()
            values.put("event_id", event_id)
            values.put("minutes", preRemindTime)
            try {
                context.contentResolver
                    .insert(Uri.parse(CALENDAR_REMINDER_URL), values)
            } catch (e: SQLException) {
                Log.e("error_code", "sql异常，插入失败")
            }
        } else {
            event.put("hasAlarm", 0)
            event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Beijing")
            context.contentResolver
                .insert(Uri.parse(CALENDAR_EVENT_URL), event)
        }
    }

    fun deleteCalendarEvent() {
        val eventCursor = context.contentResolver.query(
            Uri.parse(CALENDAR_EVENT_URL),
            EVENT_PROJECTION,
            selection,
            selectionArgs,
            null
        )
        try {
            if (eventCursor == null) return
            if (eventCursor.count > 0) {
                eventCursor.moveToFirst()
                while (!eventCursor.isAfterLast) {
                    val id =
                        eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID)) //取得id
                    val deleteUri = ContentUris.withAppendedId(
                        Uri.parse(CALENDAR_EVENT_URL),
                        id.toLong()
                    )
                    val rows =
                        context.contentResolver.delete(deleteUri, null, selectionArgs)
                    if (rows == -1) {
                        return
                    }
                    eventCursor.moveToNext()
                }
            }
        } finally {
            eventCursor?.close()
        }
    }

    fun queryAtrCount(): Int {
        val uri = Uri.parse(CALENDAR_EVENT_URL)
        val cursor = context.contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)
        val insertCount = cursor!!.count
        cursor.close()
        return insertCount
    }
}