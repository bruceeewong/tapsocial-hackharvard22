package com.hackharvard.tapsocial

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.hackharvard.tapsocial.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var mNfcAdapter: NfcAdapter? = null
    var isSupportNFC: Boolean = false

    companion object {
        private val TAG = "MainActivity:"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        detectFeatures()

        // NOTE: Try run the HEC service manually, but it doesn't work either
//        var mIntent = Intent(this, MyHostApduService::class.java)
//        startService(mIntent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent action: ${intent.action}")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processNFCData(intent)
        }
    }

    override fun onResume() {
        super.onResume();

        isMyServiceRunning(MyHostApduService::class.java)
        Log.i(TAG, "onResume ${intent.action}");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processNFCData(intent);
        }
    }


    fun detectFeatures() {
        val FEATURE_NFC = this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
        val FEATURE_NFC_BEAM = this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_BEAM)
        val FEATURE_NFC_HOST_CARD_EMULATION =
            this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
        val FEATURE_NFC_HOST_CARD_EMULATION_NFCF = this.packageManager.hasSystemFeature(
            PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF
        )
        Log.i("detectFeatures", "FEATURE_NFC: $FEATURE_NFC")
        Log.i("detectFeatures", "FEATURE_NFC_BEAM: $FEATURE_NFC_BEAM")
        Log.i("detectFeatures", "FEATURE_NFC_HOST_CARD_EMULATION: $FEATURE_NFC_HOST_CARD_EMULATION")
        Log.i(
            "detectFeatures",
            "FEATURE_NFC_HOST_CARD_EMULATION_NFCF: $FEATURE_NFC_HOST_CARD_EMULATION_NFCF"
        )
        Log.i(
            "detectFeatures", "FEATURE_NFC_OFF_HOST_CARD_EMULATION_ESE: ${
                this.packageManager.hasSystemFeature(
                    PackageManager.FEATURE_NFC_OFF_HOST_CARD_EMULATION_ESE
                )
            }"
        )
        Log.i(
            "detectFeatures", "FEATURE_NFC_OFF_HOST_CARD_EMULATION_UICC: ${
                this.packageManager.hasSystemFeature(
                    PackageManager.FEATURE_NFC_OFF_HOST_CARD_EMULATION_UICC
                )
            }"
        )
        isSupportNFC =
            FEATURE_NFC and FEATURE_NFC_HOST_CARD_EMULATION and FEATURE_NFC_HOST_CARD_EMULATION_NFCF
        Toast.makeText(
            this,
            "The device DOES ${if (isSupportNFC) "SUPPORT" else "NOT SUPPORT"} NFC TapSocial",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val res = manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
        Toast.makeText(
            this,
            "Service is ${if (res) "RUNNING" else "NOT RUNNING"}",
            Toast.LENGTH_SHORT
        ).show()
        return res
    }

    private fun processNFCData(inputIntent: Intent) {
        Log.i(TAG, "processNFCData")
        val rawMessages = inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null && rawMessages.size > 0) {
            val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
            for (i in rawMessages.indices) {
                messages[i] = rawMessages[i] as NdefMessage
            }
            Log.i(TAG, "message size = " + messages.size)

            // record 0 contains the MIME type, record 1 is the AAR
            val msg = rawMessages[0] as NdefMessage
            val base = String(msg.records[0].payload)
            val str = String.format(
                Locale.getDefault(),
                "Message entries=%d. Base message is %s",
                rawMessages.size,
                base
            )
            Log.i(TAG, "Record 0: MIME type: $base")
            Log.i(TAG, "Parsed Str: $str")
            // TODO: parse the nfc response Uri and openWebsite
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}