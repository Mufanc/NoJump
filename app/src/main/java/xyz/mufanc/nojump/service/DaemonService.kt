package xyz.mufanc.nojump.service

import android.app.IProcessObserver
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.ServiceManager
import android.util.Log
import androidx.core.os.postDelayed
import org.joor.Reflect
import rikka.hidden.compat.ActivityManagerApis
import rikka.hidden.compat.PackageManagerApis
import xyz.mufanc.nojump.App
import xyz.mufanc.nojump.IDaemonService
import kotlin.system.exitProcess

class DaemonService : IDaemonService.Stub() {

    companion object {
        private const val TAG = "${App.TAG} (daemon)"
        private const val PER_USER_RANGE = 100000
    }

    private val controller = SensorController()

//    class ActivityMonitor : IActivityController.Stub() {
//        override fun activityStarting(intent: Intent?, pkg: String?): Boolean {
//            Log.i(TAG, "[${Binder.getCallingUid()} - ${Binder.getCallingPid()}] $pkg $intent")
//            return true
//        }
//
//        override fun activityResuming(pkg: String?): Boolean = true
//        override fun appCrashed(processName: String?, pid: Int, shortMsg: String?, longMsg: String?, timeMillis: Long, stackTrace: String?): Boolean = true
//        override fun appEarlyNotResponding(processName: String?, pid: Int, annotation: String?): Int = 0
//        override fun appNotResponding(processName: String?, pid: Int, processStats: String?): Int = 0
//        override fun systemNotResponding(msg: String?): Int = -1
//    }

    private val observer = object : IProcessObserver.Stub() {

        override fun onForegroundActivitiesChanged(pid: Int, uid: Int, foreground: Boolean) {
            if (!foreground) return
            if (uid < Process.FIRST_APPLICATION_UID) return

            val packages = PackageManagerApis.getPackagesForUid(uid) ?: return
            Log.i(TAG, "Foreground changed: pid=$pid uid=$uid packages=${packages.contentToString()}")

            if (packages.isEmpty()) return
            controller.handleForegroundActivity(packages.first(), uid / PER_USER_RANGE)
        }

        override fun onProcessDied(pid: Int, uid: Int) = Unit
        override fun onProcessStateChanged(pid: Int, uid: Int, procState: Int) = Unit
        override fun onForegroundServicesChanged(pid: Int, uid: Int, serviceTypes: Int) = Unit
    }

    override fun run() {
        try {
            ActivityManagerApis.registerProcessObserver(observer)
        } catch (err: Throwable) {
            Log.e(TAG, "", err)
        }
    }

    override fun destory() {
        exitProcess(0)
    }
}
