package com.partnership.bjbdocumenttrackerreader.reader

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.partnership.bjbdocumenttrackerreader.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BeepSoundManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()
    private val soundId: Int = soundPool.load(context, R.raw.beep_sound, 1)

    fun playBeep() {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }
}
