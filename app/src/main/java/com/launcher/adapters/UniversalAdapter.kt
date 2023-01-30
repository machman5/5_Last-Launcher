package com.launcher.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.R
import com.launcher.model.Apps

// this adapter class is used for Frozen app list and Hidden app list
class UniversalAdapter(
    mContext: Context, private val list: ArrayList<Apps>
) : ArrayAdapter<Apps>(
    mContext, R.layout.v_list_item, list
) {

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row = inflater.inflate(R.layout.v_list_item, parent, false)
        row.tag = position
        val tvAppLabel = row.findViewById<TextView>(R.id.app_label)
        tvAppLabel.text = list[position].getAppName()
        tvAppLabel.tag = position

        row.setOnClickListener { view: View ->
            listener?.onClick(
                apps = getItem(view.tag as Int), view = view
            )
        }
        tvAppLabel.setOnClickListener { view: View ->
            listener?.onClick(
                apps = getItem(view.tag as Int), view = view
            )
        }
        return row
    }

    interface OnClickListener {
        fun onClick(apps: Apps?, view: View)
    }
}
