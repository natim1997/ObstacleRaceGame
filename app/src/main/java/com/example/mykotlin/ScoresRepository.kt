package com.example.mykotlin

import android.content.Context
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoresRepository {
    private const val PREFS = "high_scores"
    private const val KEY   = "scores_list"

    fun loadAll(ctx: Context): MutableList<ScoreEntry> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json  = prefs.getString(KEY, null) ?: return mutableListOf()
        val type  = object : TypeToken<MutableList<ScoreEntry>>() {}.type
        return Gson().fromJson(json, type)
    }

    @RequiresApi(35)
    fun save(ctx: Context, entry: ScoreEntry) {
        val list = loadAll(ctx)
        list.add(entry)
        list.sortByDescending { it.score }
        if (list.size > 10) list.removeLast()
        val json = Gson().toJson(list)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, json)
            .apply()
    }
}
