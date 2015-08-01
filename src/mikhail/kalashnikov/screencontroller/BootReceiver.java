package mikhail.kalashnikov.screencontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent(context, WakeUpService.class);
			context.startService(i);
		}
	}

}