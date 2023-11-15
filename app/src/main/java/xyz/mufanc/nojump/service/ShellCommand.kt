package xyz.mufanc.nojump.service

import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.ResultReceiver
import org.joor.Reflect
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class ShellCommand(private val service: IBinder) {

    class MyReceiver : ResultReceiver(null) {

        private val semaphore = Semaphore(0)

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            semaphore.release()
        }

        fun acquire() {
            semaphore.tryAcquire(1, TimeUnit.SECONDS)
        }
    }

    fun exec(vararg args: String) {
        val receiver = MyReceiver()
        // We doesn't care about inputs and outputs, just make api happy
        val (rx, tx) = ParcelFileDescriptor.createPipe().map(ParcelFileDescriptor::getFileDescriptor)
        Reflect.on(service).call("shellCommand", rx, tx, tx, args, null, receiver)
        receiver.acquire()
    }
}
