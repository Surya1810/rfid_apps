package com.partnership.bjbdocumenttrackerreader.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.partnership.bjbdocumenttrackerreader.ui.scan.MissingFragment
import com.partnership.bjbdocumenttrackerreader.ui.scan.ScannedFragment

class EpcViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3 // Jumlah tab yang ingin ditampilkan
    }

    override fun createFragment(position: Int): Fragment {
        // Sesuaikan dengan fragment yang ingin ditampilkan di setiap tab
        return when (position) {
            0 -> ScannedFragment()
            1 -> MissingFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}