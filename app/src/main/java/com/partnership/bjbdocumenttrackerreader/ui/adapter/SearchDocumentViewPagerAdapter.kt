package com.partnership.bjbdocumenttrackerreader.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.partnership.bjbdocumenttrackerreader.ui.search.SearchLostDocumentFragment
import com.partnership.bjbdocumenttrackerreader.ui.search.SingleSearchDocumentFragment

class SearchDocumentViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments = listOf(SearchLostDocumentFragment(),SingleSearchDocumentFragment())
    private val fragmentTitles = listOf("Cari Dokumen Hilang", "Cari Dokumen")

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getTitle(position: Int): String {
        return fragmentTitles[position]
    }
}