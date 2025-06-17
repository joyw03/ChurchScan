package com.churchscan.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.churchscan.app.R

class SearchHistoryAdapter(
    private val historyList: MutableList<String>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSearchItem: TextView = view.findViewById(R.id.tvSearchItem)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(item: String) {
            tvSearchItem.text = item
            btnDelete.setOnClickListener {
                onDelete(item) // 외부에서 정의한 삭제 로직 호출
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = historyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    // ✅ 개별 삭제 시 호출
    fun removeItem(item: String) {
        historyList.remove(item)
        notifyDataSetChanged()
    }

    // ✅ 전체 삭제 시 호출
    fun clearAll() {
        historyList.clear()
        notifyDataSetChanged()
    }
}
