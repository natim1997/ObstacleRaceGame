package com.example.mykotlin

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoresRepository {
    private const val PREFS = "high_scores"
    private const val KEY   = "scores_list"

    fun load(context: Context): MutableList<ScoreEntry> {
        val prefsJson = context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)
            ?: return mutableListOf()

        val type = object : TypeToken<MutableList<ScoreEntry>>() {}.type
        return Gson().fromJson(prefsJson, type)
    }

    fun save(context: Context, newEntry: ScoreEntry) {
        val list = load(context)
        list.add(newEntry)
        list.sortByDescending { it.score }
        if (list.size > 10) list.removeAt(list.lastIndex)

        val json = Gson().toJson(list)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, json)
            .apply()
    }
}
