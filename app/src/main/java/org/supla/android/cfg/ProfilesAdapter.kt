package org.supla.android.cfg

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.supla.android.R

class ProfilesAdapter(private val profilesVM: ProfilesViewModel) : BaseAdapter() {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Any {
        when(position) {
            0 -> return "Default"
            1 -> return "Summer cabin"
            2 -> return "Office"
        }
        return "wtf"
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val res: View
        if(convertView == null) {
           val inflater = parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
           res = inflater.inflate(R.layout.profile_list_item, parent, false)
        } else {
            res = convertView
        }

        val textView = res.findViewById<TextView>(R.id.profileLabel)
        val labelText = getItem(position) as String
        if(profilesVM.activeProfile.value == labelText) {
            textView.text = "✔ " + labelText
        } else {
            textView.text = labelText
        }

        return res
    }

}