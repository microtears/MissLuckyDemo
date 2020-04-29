package com.microtears.missluckydemo.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.microtears.missluckydemo.databinding.MissLuckyCellLayoutBinding

class MissLucky @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {


    override fun setLayoutManager(layout: LayoutManager?) {
        if (!firstSetLayoutManager) {
            throw  UnsupportedOperationException()
        }
        firstSetLayoutManager = false
        super.setLayoutManager(layout)
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        if (!firstSetAdapter) {
            throw  UnsupportedOperationException()
        }
        firstSetAdapter = false
        super.setAdapter(adapter)
    }

    data class Cell(var backgroundColor: Int, var foregroundColor: Int)

    private class ViewHolder<T : ViewBinding>(val binding: T) :
        RecyclerView.ViewHolder(binding.root)

    private class Adapter : RecyclerView.Adapter<ViewHolder<MissLuckyCellLayoutBinding>>() {
        val data: MutableList<Cell> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<MissLuckyCellLayoutBinding> =
            ViewHolder(MissLuckyCellLayoutBinding.inflate(LayoutInflater.from(parent.context)))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder<MissLuckyCellLayoutBinding>, position: Int) {
            holder.binding.apply {
                root.apply {
                    setBackgroundColor(data[position].backgroundColor)
                    if (foreground is ColorDrawable) {
                        (foreground.mutate() as ColorDrawable).color = data[position].foregroundColor
                    } else {
                        background = ColorDrawable(data[position].foregroundColor)
                    }
                }
            }
        }
    }


    private var highlightIndex = 0
    private var highlightColor = Color.BLUE
    private var direction = RIGHT
    private var spanCount = 3

    private var data: List<Cell>
        get() = (adapter as Adapter).data.toList() //不可变
        set(value) {
            // 粗暴点
            (adapter as Adapter).apply {
                data.apply {
                    clear()
                    addAll(value)
                }
                notifyDataSetChanged()
            }
        }

    private var firstSetLayoutManager = true
    private var firstSetAdapter = true

    init {
        layoutManager = GridLayoutManager(context, spanCount)
        adapter = Adapter()
        data = List(9) {
            val foregroundColor = if (it == highlightIndex) highlightColor else Color.TRANSPARENT
            Cell(Color.WHITE, foregroundColor)
        }
        val ti = 200L
        lateinit var action: Runnable
        action = Runnable {
            next()
            postDelayed(action, ti)
        }
        post(action)
    }

    private fun next() {
        val newData = data.apply {
            get(highlightIndex).foregroundColor = Color.TRANSPARENT
            nextIndex()
            get(highlightIndex).foregroundColor = highlightColor
        }
        data = newData
    }

    private fun nextIndex() {
        if (spanCount < 2) {
            throw  IllegalArgumentException()
        }
        // 计算下一个index
        val index = highlightIndex
        val total = data.size
        val getX = { i: Int -> i % spanCount }
        val getY = { i: Int -> i / spanCount }
        val rowCount = getY(total - 1) + 1
        var x = getX(index)
        var y = getY(index)
        val updateIndex = { highlightIndex = y * spanCount + x }
        when (direction) {
            RIGHT -> {
                ++x
                if (x >= spanCount) {
                    --x
                    direction = nextDirection(direction)
                    nextIndex()
                    return
                }
            }
            DOWN -> {
                ++y
                if (y >= rowCount) {
                    --y
                    direction = nextDirection(direction)
                    nextIndex()
                    return
                }
            }
            LEFT -> {
                --x
                if (x < 0) {
                    ++x
                    direction = nextDirection(direction)
                    nextIndex()
                    return
                }
            }
            TOP -> {
                --y
                if (y < 0) {
                    ++y
                    direction = nextDirection(direction)
                    nextIndex()
                    return
                }
            }
            else -> throw  IllegalArgumentException("direction=$direction")
        }
        highlightIndex = y * spanCount + x
    }

    companion object {
        private const val RIGHT = 0
        private const val DOWN = 1
        private const val LEFT = 2
        private const val TOP = 3

        // 这里控制方向
        private val directions = arrayOf(RIGHT, DOWN, LEFT, TOP)

        private fun nextDirection(direction: Int): Int {
            val i = directions.indexOf(direction)
            return directions[(i + 1) % directions.size]
        }


    }

}