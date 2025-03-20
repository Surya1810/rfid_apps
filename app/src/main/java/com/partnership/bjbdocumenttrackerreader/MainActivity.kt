package com.partnership.bjbdocumenttrackerreader

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.partnership.bjbdocumenttrackerreader.reader.ReaderKeyEventHandler
import com.partnership.bjbdocumenttrackerreader.ui.scan.RFIDViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel : RFIDViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getCurrentFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.childFragmentManager?.primaryNavigationFragment
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val triggerKeys = listOf(139, 280, 291, 293, 294, 311, 312, 313, 315)
        if (keyCode in triggerKeys) {
            if (event?.repeatCount == 0) {
                (getCurrentFragment() as? ReaderKeyEventHandler)?.myOnKeyDown()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val triggerKeys = listOf(139, 280, 291, 293, 294, 311, 312, 313, 315)
        if (keyCode in triggerKeys) {
            (getCurrentFragment() as? ReaderKeyEventHandler)?.myOnKeyUp()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopReadTag()
        viewModel.releaseReader()
    }
}
