package mikhail.kalashnikov.screencontroller;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
	implements OnSharedPreferenceChangeListener{
	public final static String PREF_SENSOR_MODE = "pref_sensor_mode"; 
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ListPreference prefSensorModes = (ListPreference) findPreference(PREF_SENSOR_MODE);
		prefSensorModes.setSummary(prefSensorModes.getEntry());
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(PREF_SENSOR_MODE)){
			@SuppressWarnings("deprecation")
			ListPreference prefSensorModes = (ListPreference) findPreference(PREF_SENSOR_MODE);
			prefSensorModes.setSummary(prefSensorModes.getEntry());
		}
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
}
