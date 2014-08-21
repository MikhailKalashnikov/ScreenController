package mikhail.kalashnikov.screencontroler;

import com.example.testsensors.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ShakeActivity extends Activity {
	protected static final String TAG = "ShakeActivity";
	private ComponentName mDeviceAdmin;
	private DevicePolicyManager mDevicePolicyManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDeviceAdmin = new ComponentName(this, DeviceAdminLockReceiver.class);
		mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
	}
	
	public void startService(View v){
		Intent i = new Intent(this, WakeUpService.class);
		startService(i);
	}
	
	public void stopService(View v){
		Intent i = new Intent(this, WakeUpService.class);
		stopService(i);
	}
	
	public void lockScreen(View v){
		
		if(!mDevicePolicyManager.isAdminActive(mDeviceAdmin)){
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra("force-lock", DeviceAdminInfo.USES_POLICY_FORCE_LOCK);
            startActivityForResult(intent, 1);
		}else{
			mDevicePolicyManager.lockNow();
			mDevicePolicyManager.setMaximumTimeToLock(mDeviceAdmin, 0);
		}
		
	}

	public void restartPhone(View v){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_restart_confirmation)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Restart");
					
				}
			});
		builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
 
}
