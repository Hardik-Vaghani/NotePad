package com.hardik.notepad.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hardik.notepad.databinding.ItemIndexPreviewBinding
import com.hardik.notepad.domain.model.Note

class IndexAdapter : RecyclerView.Adapter<IndexAdapter.IndexViewHolder>(), Filterable {
    inner class IndexViewHolder(val binding: ItemIndexPreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this@IndexAdapter, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndexViewHolder {
        return IndexViewHolder(
            ItemIndexPreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: IndexViewHolder, position: Int) {
        val indexItem = differ.currentList[position]

        holder.binding.itemIndexPreviewTvTitle.text = indexItem
        holder.binding.itemIndexPreviewTvTitle.setHorizontallyScrolling(true)
        holder.binding.itemIndexPreviewTvTitle.isSelected = true

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(indexItem)
        }

    }

    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    private var originalList: List<String> = emptyList()

    fun submitList(list: List<String>?) {
        originalList = list.orEmpty()
        differ.submitList(list)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.contains(constraint, true)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                differ.submitList(results?.values as List<String>?)
            }
        }
    }
}