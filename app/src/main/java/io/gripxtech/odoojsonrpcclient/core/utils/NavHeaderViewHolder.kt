package io.gripxtech.odoojsonrpcclient.core.utils

import android.support.constraint.ConstraintLayout
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.GlideApp
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.OdooUser
import io.gripxtech.odoojsonrpcclient.trimFalse

class NavHeaderViewHolder(view: View) {
    val pic: CircleImageView = view.findViewById(R.id.userImage)
    val name: TextView = view.findViewById(R.id.header_name)
    val email: TextView = view.findViewById(R.id.header_details)
    val menuToggle: ConstraintLayout = view.findViewById(R.id.menuToggle)
    val menuToggleImage: ImageView = view.findViewById(R.id.ivDropdown)


    fun setUser(user: OdooUser) {
        name.text = user.name
        email.text = user.login
        if (user.imageSmall.trimFalse().isNotEmpty()) {
            val byteArray = Base64.decode(user.imageSmall, Base64.DEFAULT)
            GlideApp.with(pic.context)
                    .asBitmap()
                    .load(byteArray)
                    .into(pic)
        } else {
            GlideApp.with(pic.context)
                    .asBitmap()
                    .load((pic.context.applicationContext as App).getLetterTile(user.name))
                    .into(pic)
        }
    }
}
