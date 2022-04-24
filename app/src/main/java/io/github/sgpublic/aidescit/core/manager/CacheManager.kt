package io.github.sgpublic.aidescit.core.manager

import io.github.sgpublic.aidescit.Application
import org.json.JSONObject
import java.io.File

object CacheManager {
    private const val CACHE_SCHEDULE_KEY = "schedule.json"
    private const val CACHE_ACHIEVEMENT_KEY = "achievement.json"
    private const val CACHE_EXAM_KEY = "exam.json"
    private const val CACHE_HEADLINE_KEY = "headline.json"

    var CACHE_SCHEDULE: JSONObject?
    get() = read(CACHE_SCHEDULE_KEY)
    set(value) { value?.let {
        save(CACHE_SCHEDULE_KEY, it)
    } }

    var CACHE_ACHIEVEMENT: JSONObject?
    get() = read(CACHE_ACHIEVEMENT_KEY)
    set(value) { value?.let {
        save(CACHE_ACHIEVEMENT_KEY, it)
    } }

    var CACHE_EXAM: JSONObject?
    get() = read(CACHE_EXAM_KEY)
    set(value) { value?.let {
        save(CACHE_EXAM_KEY, it)
    } }

    var CACHE_HEADLINE: JSONObject?
    get() = read(CACHE_HEADLINE_KEY)
    set(value) { value?.let {
        save(CACHE_HEADLINE_KEY, it)
    } }

    private val cacheDir = Application.APPLICATION_CONTEXT.externalCacheDir?.path
    private fun save(name: String, content: JSONObject?): CacheManager {
        File(cacheDir, name).let {
            if (content != null) it.writeText(content.toString())
            else it.delete()
        }
        return this
    }
    private fun read(name: String): JSONObject? {
        return try {
            val cacheString = File(cacheDir, name).readText()
            JSONObject(cacheString)
        } catch (e: Exception){ null }
    }
}