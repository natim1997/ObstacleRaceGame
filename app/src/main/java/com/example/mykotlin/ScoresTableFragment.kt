package com.example.mykotlin

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScoresTableFragment : Fragment() {

    interface OnScoreClickListener {
        fun onScoreSelected(entry: ScoreEntry)
    }

    private var listener: OnScoreClickListener? = null
    private lateinit var recycler: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnScoreClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_scores_table, container, false).also {
        recycler = it.findViewById(R.id.scoresRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val scores = ScoresRepository.loadAll(requireContext())
        recycler.adapter = ScoresAdapter(scores) { entry ->
            listener?.onScoreSelected(entry)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
