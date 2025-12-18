package com.partnership.bjbdocumenttrackerreader.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.partnership.bjbdocumenttrackerreader.ui.scan.histories.AgunanFragment
import com.partnership.bjbdocumenttrackerreader.ui.scan.histories.ContractDocumentFragment

class HistoriesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ContractDocumentFragment()
            else -> AgunanFragment()
        }
    }
}
