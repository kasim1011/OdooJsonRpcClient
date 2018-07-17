package io.gripxtech.odoojsonrpcclient.core.utils.tabs

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class PagerAdapter(
        fragmentManager: FragmentManager,
        val items: ArrayList<PagerItem>
) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = items[position].fragment

    override fun getCount(): Int = items.size

    override fun getPageTitle(position: Int): CharSequence = items[position].title

    fun getFragments(): ArrayList<Fragment> {
        val fragments: ArrayList<Fragment> = ArrayList()
        items.mapTo(fragments) { it.fragment }
        return fragments
    }
}
