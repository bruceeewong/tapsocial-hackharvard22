package com.hackharvard.tapsocial

import android.nfc.NdefRecord
import android.nfc.NdefRecord.TNF_ABSOLUTE_URI
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.Charset

class MyHostApduService : HostApduService() {
    private var messageCounter = 0;


    override fun onCreate() {
        super.onCreate()
        Log.i("MyHostApduService", "onCreate")
    }

    override fun processCommandApdu(apdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.i("processCommandApdu", "start")
        if (selectAidApdu(apdu)) {
            Log.i("MyHostApduService", "Application selected")
            return getAbsoluteUri()
        } else {
            Log.i("MyHostApduService", "Received: " + apdu?.let { String(it) })
            return getNextMessage()
        }
    }

    private fun getNextMessage(): ByteArray {
        val bytes = ("Message count: $messageCounter").toByteArray()
        messageCounter++;
        return bytes

    }

    private fun getAbsoluteUri(): ByteArray {
        var uri = "https://developer.android.com/index.html"
        val uriRecord = ByteArray(0).let { emptyByteArray ->
            NdefRecord(
                TNF_ABSOLUTE_URI,
                uri.toByteArray(Charset.forName("US-ASCII")),
                emptyByteArray,
                emptyByteArray
            )
        }
        Log.i("MyHostApduService", "getAbsoluteUri $uri")
        return uriRecord.toByteArray()
    }


    // PN532 NFC chip
    private fun selectAidApdu(apdu: ByteArray?): Boolean {
        if (apdu == null) return false
        var isAidApdu = apdu.size >= 2 && apdu[0] == 0.toByte() && apdu[1] == 0xa4.toByte()
        Log.i("MyHostApduService", "isAidApdu: $isAidApdu")
        return isAidApdu
    }

    override fun onDeactivated(reason: Int) {
        Log.i("MyHostApduService", "Deactivated: $reason");
    }
}