package com.partnership.bjbdocumenttrackerreader.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.partnership.bjbdocumenttrackerreader.ui.search.SearchLostAgunanFragment
import com.partnership.bjbdocumenttrackerreader.ui.search.SingleSearchAgunanFragment

class SearchAgunanViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments = listOf(SearchLostAgunanFragment(), SingleSearchAgunanFragment())
    private val fragmentTitles = listOf("Cari Agunan Hilang", "Cari Agunan")

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getTitle(position: Int): String {
        return fragmentTitles[position]
    }
}