package com.sgpublic.aidescit.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.data.LicenseListData

class LicenseListAdapter(contexts: Context, private val resource: Int, objects: List<LicenseListData>) :
    ArrayAdapter<LicenseListData>(contexts, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item: LicenseListData? = getItem(position)
        val view: View = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        item?.run {
            val itemLicenseTitle = view.findViewById<TextView>(R.id.item_license_title)
            itemLicenseTitle.text = item.projectTitle
            val itemLicenseAuthor = view.findViewById<TextView>(R.id.item_license_author)
            itemLicenseAuthor.text = item.projectAuthor
            val itemLicenseAbout = view.findViewById<TextView>(R.id.item_license_about)
            itemLicenseAbout.text = item.projectAbout
            view.findViewById<View>(R.id.item_license_base).setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(item.projectUrl)
                context.startActivity(intent)
            }
        }
        return view
    }
}
