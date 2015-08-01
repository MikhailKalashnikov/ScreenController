package mikhail.kalashnikov.screencontroller;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceAdminLockReceiver extends DeviceAdminReceiver {
	@Override
	public void onEnabled(Context context, Intent intent) {
		Log.d("DeviceAdminLockReceiver", "OnEnable");
	}
}
