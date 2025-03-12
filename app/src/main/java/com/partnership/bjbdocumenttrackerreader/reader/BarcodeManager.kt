package com.partnership.bjbdocumenttrackerreader.reader

import android.content.Context
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import com.rscja.barcode.BarcodeUtility
import com.rscja.deviceapi.entity.BarcodeEntity

class BarcodeManager {
    private val barcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder

    fun startBarcode(){
        barcodeDecoder.startScan()
    }

    fun stopBarcode(){
        barcodeDecoder.stopScan()
    }

    fun openBarcode(context: Context,onScanned: (String) -> Unit){
        barcodeDecoder.open(context)
        barcodeDecoder.setDecodeCallback { barcodeEntity ->
            if (barcodeEntity.resultCode == BarcodeDecoder.DECODE_SUCCESS){
                onScanned(barcodeEntity.barcodeData)
                BarcodeUtility.getInstance().enablePlaySuccessSound(context,true)
            }else{
                onScanned("Barcode Error")
            }

        }
    }

    fun closeBarcode(){
        barcodeDecoder.close()
    }

}