package com.churchscan.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.churchscan.app.R
import android.util.Log

class SearchHistoryAdapter(
    private val historyList: MutableList<String>,
    private val onDelete: (String) -> Unit,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSearchItem: TextView = view.findViewById(R.id.tvSearchItem)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(item: String) {
            tvSearchItem.text = item

            // ✅ 검색어 텍스트 클릭 시 호출
            tvSearchItem.setOnClickListener {
                onItemClick(item)
            }

            // ✅ X 버튼 클릭 시 삭제 호출
            btnDelete.setOnClickListener {
                onDelete(item)
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

    // ✅ 특정 항목 삭제
    fun removeItem(item: String) {
        historyList.remove(item)
        notifyDataSetChanged()
    }

    // ✅ 전체 삭제
    fun clearAll() {
        historyList.clear()
        notifyDataSetChanged()
    }
}
