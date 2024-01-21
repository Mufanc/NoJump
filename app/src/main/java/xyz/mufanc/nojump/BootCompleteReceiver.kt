package xyz.mufanc.nojump

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import rikka.shizuku.Shizuku
import xyz.mufanc.nojump.service.DaemonService

class BootCompleteReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = App.TAG

        private val sServiceArgs = Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, DaemonService::class.java.name)
        )
            .daemon(true)
            .processNameSuffix("daemon")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)

        private val sServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                Log.d(TAG, "service: $binder")

                if (binder != null && binder.pingBinder()) {
                    val service = IDaemonService.Stub.asInterface(binder)
                    service.run()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "service disconnected!")
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        Log.i(TAG, "boot complete received")

        if (Shizuku.pingBinder()) {
            val version = Shizuku.peekUserService(sServiceArgs, sServiceConnection)
            if (version == -1) {
                Log.d(TAG, "service is not running, just start it")

                Shizuku.bindUserService(sServiceArgs, sServiceConnection)
            } else if (BuildConfig.VERSION_CODE > version || BuildConfig.DEBUG) {
                Log.d(TAG, "restart the service")

                Shizuku.unbindUserService(sServiceArgs, null, true)
                Handler(Looper.myLooper()!!).postDelayed(1000) {
                    Shizuku.bindUserService(sServiceArgs, sServiceConnection)
                }
            }
        }
    }
}
