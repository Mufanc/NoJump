package xyz.mufanc.nojump.service

import android.os.Handler
import android.os.HandlerThread
import android.os.ServiceManager
import java.util.concurrent.ConcurrentHashMap

class SensorController {

    companion object {
        private const val SENSOR_SERVICE = "sensorservice"
        private const val SENSOR_BLOCK_DURATION = 5000L
    }

    private val ss = ServiceManager.getService(SENSOR_SERVICE)

    private val worker = HandlerThread("controller").apply { start() }
    private val handler = Handler(worker.looper)

    private val pendingTasks = ConcurrentHashMap<String, Long>()

    inner class ResetUidTask(
        private val pkg: String, private val user: Int,
        private val timestamp: Long
    ) : Runnable {

        private var stage = 0

        override fun run() {
            if (pendingTasks[pkg] != timestamp) return
            if (stage == 0) {
                ShellCommand(ss).exec("set-uid-state", pkg, "idle", "--user", "$user")
                stage += 1
                handler.postDelayed(this, SENSOR_BLOCK_DURATION)
            } else {
                ShellCommand(ss).exec("reset-uid-state", pkg, "--user", "$user")
            }
        }
    }

    @Synchronized
    fun handleForegroundActivity(pkg: String, user: Int) {
        val now = System.currentTimeMillis()
        pendingTasks[pkg] = now
        handler.post(ResetUidTask(pkg, user, now))
    }
}
