package xyz.mufanc.nojump

import android.app.Application
import android.util.Log
import rikka.sui.Sui

class App : Application() {
    companion object {
        const val TAG = "NoJump"

        var isSui: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        isSui = Sui.init(BuildConfig.APPLICATION_ID)
        Log.i(TAG, "application created, isSui = $isSui")
    }
}
