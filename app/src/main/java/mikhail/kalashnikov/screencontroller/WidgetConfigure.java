package mikhail.kalashnikov.screencontroller;

import android.app.Activity;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class WidgetConfigure extends Activity{

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {    
			mAppWidgetId = extras.getInt(            
					AppWidgetManager.EXTRA_APPWIDGET_ID,             
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
	    	finish();
	    }
		
		ComponentName deviceAdmin = new ComponentName(this, DeviceAdminLockReceiver.class);
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		if(!devicePolicyManager.isAdminActive(deviceAdmin)){
			Intent intentDA = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intentDA.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
			intentDA.putExtra("force-lock", DeviceAdminInfo.USES_POLICY_FORCE_LOCK);
            startActivity(intentDA);
		}
		
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		
		setResult(RESULT_OK, resultValue);
		finish();
	}
}
