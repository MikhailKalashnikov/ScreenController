package mikhail.kalashnikov.screencontroller;

import android.app.PendingIntent;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.RemoteViews.RemoteView;

public class ScreenControllerWidgetProvider extends AppWidgetProvider{
	public static String LOCK_BUTTON = "mikhail.kalashnikov.screencontroller.LOCK_BUTTON";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		
		for(int i=0; i<N; i++){
			int appWidgetId = appWidgetIds[i];
			Intent intent = new Intent(LOCK_BUTTON);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lock_widget);
			views.setOnClickPendingIntent(R.id.widget_btn_lock, pendingIntent );
			
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(LOCK_BUTTON.equals(intent.getAction())){
			ComponentName deviceAdmin = new ComponentName(context, DeviceAdminLockReceiver.class);
			DevicePolicyManager dpm = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			if(!dpm.isAdminActive(deviceAdmin)){
				Toast.makeText(context, R.string.grant_admin_first, Toast.LENGTH_LONG).show();
			}else{
				dpm.lockNow();
				dpm.setMaximumTimeToLock(deviceAdmin, 0);
			}
		}
	}
}
