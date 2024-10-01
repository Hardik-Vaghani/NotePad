package com.hardik.notepad.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hardik.notepad.databinding.ItemIndexHomePreviewBinding
import com.hardik.notepad.databinding.ItemIndexPreviewBinding
import com.hardik.notepad.domain.model.Note

class IndexHomeAdapter : RecyclerView.Adapter<IndexHomeAdapter.IndexViewHolder>(), Filterable {
    inner class IndexViewHolder(val binding: ItemIndexHomePreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.title == newItem.title && oldItem.content == newItem.content
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this@IndexHomeAdapter, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndexViewHolder {
        return IndexViewHolder(
            ItemIndexHomePreviewBinding.inflate(
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

        holder.binding.itemIndexPreviewTvTitle.text = indexItem.title
        holder.binding.itemIndexPreviewTvTitle.setHorizontallyScrolling(true)
        holder.binding.itemIndexPreviewTvTitle.isSelected = true

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(indexItem)
        }

    }

    private var onItemClickListener: ((Note) -> Unit)? = null

    fun setOnItemClickListener(listener: (Note) -> Unit) {
        onItemClickListener = listener
    }

    private var originalList: List<Note> = emptyList()

    fun submitList(list: List<Note>?) {
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
                        it.title.contains(constraint, true) || it.subject.contains(constraint, true)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                differ.submitList(results?.values as List<Note>?)
            }
        }
    }
}