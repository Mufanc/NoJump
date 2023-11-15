package android.app;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IActivityController extends IInterface {
    boolean activityStarting(Intent intent, String pkg);
    boolean activityResuming(String pkg);
    boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace);
    int appEarlyNotResponding(String processName, int pid, String annotation);
    int appNotResponding(String processName, int pid, String processStats);
    int systemNotResponding(String msg);

    abstract class Stub extends Binder implements IActivityController {
        public IBinder asBinder() {
            throw new RuntimeException("STUB");
        }
    }
}
