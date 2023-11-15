package xyz.mufanc.nojump

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import rikka.shizuku.Shizuku
import rikka.sui.Sui
import xyz.mufanc.nojump.service.DaemonService

class App : Application() {

    companion object {
        const val TAG = "NoJump"

        var isSui: Boolean = Sui.init(BuildConfig.APPLICATION_ID)
    }

    private val args = Shizuku.UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, DaemonService::class.java.name))
        .daemon(true)
        .processNameSuffix("daemon")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    private val connection = object : ServiceConnection {
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

    override fun onCreate() {
        super.onCreate()

        val version = Shizuku.peekUserService(args, connection)
        if (version == -1) {
            Log.d(TAG, "service is not running, just start it")
            Shizuku.bindUserService(args, connection)
        } else if (BuildConfig.VERSION_CODE > version || BuildConfig.DEBUG) {
            Log.d(TAG, "restart the service")
            Shizuku.unbindUserService(args, null, true)
            Handler(Looper.myLooper()!!).postDelayed(1000) {
                Shizuku.bindUserService(args, connection)
            }
        }
    }
}
