package xyz.mufanc.nojump.service

import android.app.IProcessObserver
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.ServiceManager
import android.util.ArrayMap
import android.util.Log
import rikka.hidden.compat.ActivityManagerApis
import rikka.hidden.compat.PackageManagerApis
import xyz.mufanc.nojump.App
import xyz.mufanc.nojump.IDaemonService
import kotlin.system.exitProcess

class DaemonService : IDaemonService.Stub() {

    companion object {
        private const val TAG = "${App.TAG} (daemon)"

        private const val SENSOR_SERVICE = "sensorservice"
        private const val SENSOR_BLOCK_DURATION = 7500L
    }

    private val mSensorService = ServiceManager.getService(SENSOR_SERVICE)
    private val mWorkerThread = HandlerThread("timer").apply { start() }
    private val mHandler = Handler(mWorkerThread.looper)

    private val mBlockedTime = ArrayMap<ActivityInfo, Long>()

    private val mProcessObserver = object : IProcessObserver.Stub() {

        override fun onForegroundActivitiesChanged(pid: Int, uid: Int, foreground: Boolean) {
            if (uid < Process.FIRST_APPLICATION_UID) return
            updateUidState(ActivityInfo.fromUid(uid) ?: return, allow = false)
        }

        override fun onProcessDied(pid: Int, uid: Int) = Unit
        override fun onProcessStateChanged(pid: Int, uid: Int, procState: Int) = Unit
        override fun onForegroundServicesChanged(pid: Int, uid: Int, serviceTypes: Int) = Unit
    }

    @Synchronized
    private fun updateUidState(info: ActivityInfo, allow: Boolean) {
        val currentTime = System.currentTimeMillis()
        val command = ShellCommand(mSensorService)

        if (allow) {
            val blockedTime = mBlockedTime[info]

            if (blockedTime == null || currentTime - blockedTime >= SENSOR_BLOCK_DURATION) {
                command.exec("reset-uid-state", info.pkg, "--user", "${info.user}")
                mBlockedTime.remove(info)
                Log.i(TAG, "unblock sensor for $info")
            }
        } else {
            if (!mBlockedTime.contains(info)) {
                command.exec("set-uid-state", info.pkg, "idle", "--user", "${info.user}")
                Log.i(TAG, "block sensor for: $info")
            }

            mBlockedTime[info] = currentTime
            mHandler.postDelayed(ResetUidRunnable(info), SENSOR_BLOCK_DURATION)
        }
    }

    override fun run() {
        try {
            ActivityManagerApis.registerProcessObserver(mProcessObserver)
            Log.i(TAG, "service started")
        } catch (err: Throwable) {
            Log.e(TAG, "", err)
        }
    }

    override fun destory() {
        exitProcess(0)
    }

    data class ActivityInfo(val pkg: String, val user: Int) {
        companion object {

            private const val PER_USER_RANGE = 100000

            fun fromUid(uid: Int): ActivityInfo? {
                val pkg = PackageManagerApis.getPackagesForUid(uid)?.firstOrNull() ?: return null
                val user = uid / PER_USER_RANGE

                return ActivityInfo(pkg, user)
            }
        }

        override fun toString(): String {
            return "ActivityInfo { pkg = $pkg, user = $user }"
        }
    }

    inner class ResetUidRunnable(
        private val info: ActivityInfo
    ) : Runnable {
        override fun run() {
            updateUidState(info, allow = true)
        }
    }
}
