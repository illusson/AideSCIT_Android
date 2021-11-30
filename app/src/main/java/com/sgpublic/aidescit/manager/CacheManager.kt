package com.sgpublic.aidescit.manager

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

class CacheManager (context: Context) {
    companion object{
        const val CACHE_SCHEDULE = "schedule.json"
        const val CACHE_ACHIEVEMENT = "achievement.json"
        const val CACHE_EXAM = "exam.json"
        const val CACHE_HEADLINE = "headline.json"
    }

    private val cacheDir = context.applicationContext.externalCacheDir?.path

    fun save(name: String, content: String): CacheManager {
        File(cacheDir, name).writeText(content)
        return this
    }

    fun read(name: String): JSONObject? {
        return try {
            val cacheString = File(cacheDir, name).readText()
            JSONObject(cacheString)
        } catch (e: FileNotFoundException){
            null
        } catch (e: JSONException){
            null
        }
    }
}