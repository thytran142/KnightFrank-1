package com.novitee.knightfrankacution;

import java.io.IOException;
import java.sql.Timestamp;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.novitee.knightfrankacution.util.Preferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class SplashScreenActivity extends Activity {
	
	Context context = this;
	
	//GCM
	String regId;
	GoogleCloudMessaging gcm;
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		if (Preferences.getRegid(context) == null) {
	            regId = getRegistrationId(getApplicationContext());
	        	//regId="1856c20b587c402c00e0b1ff901ebcd5d3d";
	            
	            gcm = GoogleCloudMessaging.getInstance(this);
	            
	            if (regId.length() == 0) {
	                new registerBackground().execute();
//	            	registerBackground();
	            } else {
	                Log.i("registration id", "registration id =====  " + regId);
	                
	                Preferences.setRegid(context, regId);
	                Intent i = new Intent();
	                i.setClass(context, MainActivity.class);
	                finish();
	                startActivity(i);
	            }
	     } else {
            Thread logoTimer = new Thread() {
                public void run() {
                    try {
                        int logoTimer = 0;
                        while (logoTimer < 3000) {
                            sleep(100);
                            logoTimer = logoTimer + 100;

                        }
                        Intent i = new Intent();
                        i.setClass(context, MainActivity.class);
                        finish();
                        startActivity(i);
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        finish();
                    }
                }
            };
            logoTimer.start();
        }
	}
	
	private class registerBackground extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }

                regId = gcm.register(com.novitee.knightfrankacution.util.CommonUtilities.SENDER_ID);
                setRegistrationId(context, regId);
                
            } catch (IOException ex) {
                ex.getMessage();
            }

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			Intent i = new Intent();
            i.setClass(context, MainActivity.class);
            finish();
            startActivity(i);
		}
		
	}
	
	/**
     * Gets the current registration id for application on GCM service.
     * <p>
     * If result is empty, the registration has failed.
     *
     * @return registration id, or empty string if the registration is not
     *         complete.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v("registration id", "Registration not found.");
            return "";
        }
        
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired()) {
            Log.v("registration id", "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }
	
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        return 6;
    }
    
    /**
     * Checks if the registration has expired.
     *
     * <p>To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to re-register
     * after REGISTRATION_EXPIRY_TIME_MS.
     *
     * @return true if the registration has expired.
     */
    private boolean isRegistrationExpired() {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * Stores the registration id, app versionCode, and expiration time in the
     * application's {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration id
     */
    private void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v("registration id", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

        Log.v("registration id", "Setting registration expiry time to " +
                new Timestamp(expirationTime));
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();

        Preferences.setRegid(context, regId);
    }
}
