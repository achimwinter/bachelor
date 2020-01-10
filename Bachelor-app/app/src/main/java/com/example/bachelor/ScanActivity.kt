package com.example.bachelor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    companion object {
        const val EXTRA_SCAN_RESULT = "extraScanResult"
    }

    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA), 0 )
        }

        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        Log.v("tag", rawResult.text) // Prints scan results
        Log.v("tag", rawResult.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)

        val intent = Intent()
        intent.putExtra(EXTRA_SCAN_RESULT, rawResult.text)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}