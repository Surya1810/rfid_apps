package com.partnership.bjbdocumenttrackerreader.ui.search.radar

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partnership.bjbdocumenttrackerreader.reader.RFIDManager
import com.rscja.deviceapi.entity.RadarLocationEntity
import com.rscja.deviceapi.interfaces.IUHFRadarLocationCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RadarViewModel @Inject constructor(private val reader: RFIDManager,@ApplicationContext private val app: Context) : ViewModel() {
    private val _radarData = MutableLiveData<List<RadarLocationEntity>>()
    val radarData : LiveData<List<RadarLocationEntity>> get() = _radarData

    private val _angle = MutableLiveData<Int>()
    val angle : LiveData<Int> get() = _angle

    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> get() = _isScanning

    private val _beep = MutableLiveData<Boolean>()
    val beep: LiveData<Boolean> get() = _beep


    fun startRadar(target: String) {
        _isScanning.value = true

        reader.startRadar(app, target, object : IUHFRadarLocationCallback {
            override fun getLocationValue(location: List<RadarLocationEntity>) {
                // === Penting: snapshot immutable supaya tidak ConcurrentModificationException ===
                // lokasi bisa LinkedList yang dimodifikasi driver; copy ke ArrayList
                val snapshot: List<RadarLocationEntity> = ArrayList(location)

                // kirim ke UI pakai snapshot
                _radarData.value = snapshot

                // ====== LOGIC BEEP ======
                // kalau target TIDAK kosong -> beep hanya jika ada item.tag == target
                // kalau target kosong -> beep untuk "ada data" (atau sesuaikan kebutuhanmu)
                val shouldTrigger = if (target.isNotEmpty()) {
                    snapshot.any { it.tag == target }
                } else {
                    snapshot.isNotEmpty()
                }

                if (shouldTrigger) {
                    _beep.postValue(true)
                }
            }

            override fun getAngleValue(angle: Int) {
                _angle.postValue(-angle)
            }
        })
    }

    fun stopRadar(){
        _isScanning.value = false
        reader.stopRadar()
    }
}