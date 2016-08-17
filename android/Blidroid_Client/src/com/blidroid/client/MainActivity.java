package com.blidroid.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
	boolean requestLocation;
	boolean requestVoice;
	boolean requestStreet;
	LoadNearbyLocations loadLoc = null;

	
	// Creating JSON Parser object
		JSONParser jParser = new JSONParser();
		
		//Create list of locations
		ArrayList<HashMap<String, String>> locationsList = new ArrayList<HashMap<String, String>>();

		//Declare variables
		public String Latitude;
		public String Longitude;
		private final String TAG = "Blidroid"; 
		private GoogleApiClient mGoogleApiClient;
		private LocationRequest mLocationRequest;
		HashMap<String, String> location;
		//----------------------------
		TextToSpeech ttsobject;
		//----------------------------
		JSONArray locations = null;
		WakeLock wl;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Always_Running");
		super.onCreate(savedInstanceState);
		wl.acquire();
		
	    
		setContentView(R.layout.activity_main);
		mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
		
		
		
		//Create an instance of the Location Manager to verify if the Locations are active
		LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
				boolean location_enabled = false;
				try {
		        location_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		    	} catch (Exception ex) {
		        	Log.e("Blidroid", ex.getMessage());
		    	}
				if (! location_enabled) {
		            Dialog dialog = createDialog();
		            dialog.show();
		            //ttsobject.speak("Para utilizar o aplicativo: Ative o GPS e as redes moveis para uma melhor precisão", TextToSpeech.QUEUE_FLUSH, null);
		    	}
		
		//Text to speech code--------------
		ttsobject = new TextToSpeech(MainActivity.this,new TextToSpeech.OnInitListener() {
			
		
			@Override
			public void onInit(int status) {
				// TODO Auto-generated method stub
				if(status==TextToSpeech.SUCCESS){
					ttsobject.setLanguage(Locale.CANADA);
					//ttsobject.speak("O serviço foi iniciado! Utilize a tecla de volume para cima para requisitar Lugares próximos a você ou pressione a tecla de volume para baixo para descobrir o endereço mais próximo de você", TextToSpeech.QUEUE_FLUSH, null);
					ttsobject.speak("O serviço foi iniciado!", TextToSpeech.QUEUE_FLUSH, null);
					//ttsobject.speak("Utilize a tecla de volume para cima para descobrir Lugares próximos a você!", TextToSpeech.QUEUE_FLUSH, null);
					//ttsobject.speak("pressione a tecla de volume para baixo para descobrir o endereço mais próximo de você!", TextToSpeech.QUEUE_FLUSH, null);
				}else{
					ttsobject.speak("Característica não suportada pelo seu aparelho!", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		});
		
		showServiceNotification();
		
		
		
		if(!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			Toast.makeText(MainActivity.this, "Ative as Redes Moveis (Alta precisão) para localizações mais precisas", Toast.LENGTH_LONG).show();
			ttsobject.speak("Ative as Redes Móveis para obter localizações mais precisas", TextToSpeech.QUEUE_FLUSH, null);
		}
		
		Button btnGetAddress = (Button) findViewById(R.id.btnGetAddress);
		btnGetAddress.setOnClickListener( new OnClickListener() {
				public void onClick(View v) {
					requestStreet = true;
		            }
		        });
		Button btnGetPlaces = (Button) findViewById(R.id.btnGetPlaces);
		btnGetPlaces.setOnClickListener( new OnClickListener() {
				public void onClick(View v) {
					requestLocation = true;
			    	requestVoice = false;
			    	loadLoc = null;
		            }
		        });
		Button btnTTSOptions = (Button) findViewById(R.id.btnTTSOptions);
		btnTTSOptions.setOnClickListener( new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
		            }
		        });
		
	}
	

		 private Window wind;
		    @Override
		protected void onResume() {
		    // TODO Auto-generated method stub
		    super.onResume();
		    /******block is needed to raise the application if the lock is*********/
		    wind = this.getWindow();
		    wind.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		    wind.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		    wind.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		    /* ^^^^^^^block is needed to raise the application if the lock is*/
		}
	public Dialog createDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
               builder.setTitle("Ativação de GPS")
               .setMessage("Para utilizar o aplicativo:\n\n• Ative o GPS e as redes moveis (Alta precisão)")
               .setPositiveButton("Ativar GPS", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                       startActivity(myIntent);
                   }
               })
               .setNegativeButton("Finalizar Aplicação", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   //finish();
                	   int pid = android.os.Process.myPid();
                       android.os.Process.killProcess(pid);
                       System.exit(0);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
	}
	private void showServiceNotification(){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Blidroid")
		        .setContentText("O serviço foi Iniciado");
				
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		//mBuilder.setContentIntent(resultIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
	}
	
	private String GetAddress(Double lat, Double lon) {
		String ret = "";
		if(lat!=0 && lon !=0){
		Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.CANADA);
	    
	    List<Address> addresses = null;
	    try {
	        addresses = geocoder.getFromLocation(lat, lon, 1);
	        if (!addresses.equals(null)) {
	        /*    Address returnedAddress = addresses.get(0);
	            StringBuilder strReturnedAddress = new StringBuilder("\n");
	            for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
	                strReturnedAddress
	                        .append(returnedAddress.getAddressLine(i)).append(
	                                "\n");
	            }
	            ret = "Around: " + strReturnedAddress.toString();*/
	        	Address returnedAddress = addresses.get(0);
	        	ret = returnedAddress.getAddressLine(0);
	        } else {
	            ret = "No Address returned!";
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        
	        /*
	        ret = "Location: https://maps.google.co.in/maps?hl=en&q=" + lat
	                + "," + lon;*/
	    } catch (NullPointerException e) {
	        e.printStackTrace();
	        /*ret = lat + "," + lon;*/
	    }
	    }
	    return ret;
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
	 	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		//Verify if mobile data is connected
		NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mWifi.isConnected() || mMobile.isConnected()) {
			try{
		int action = event.getAction();
	    int keyCode = event.getKeyCode();
	        switch (keyCode) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
	            if (action == KeyEvent.ACTION_DOWN) {
					    	requestLocation = true;
					    	requestVoice = false;
					    	loadLoc = null;
	            }
	            return true;
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            if (action == KeyEvent.ACTION_DOWN) {
	              requestStreet = true;
	            }
	            return true;
	        default:
	            return super.dispatchKeyEvent(event);
	        }
		}catch(Exception ex){
	    	ex.getMessage();
	    }
	    }	else{
			//Toast.makeText(MainActivity.this, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
			ttsobject.speak("Ocorreu um problema Por favor verifique sua conexão com a Internet", TextToSpeech.QUEUE_FLUSH, null);
		}
		return super.dispatchKeyEvent(event);
	}
	protected void onStart() {
	    super.onStart();
	    mGoogleApiClient.connect();
	}

	protected void onStop() {
	    mGoogleApiClient.disconnect();
	    super.onStop();
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

	@Override
	public void onConnected(Bundle arg0) {
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
  

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "GoogleApiClient connection has been suspend");
		ttsobject.speak("A conexão com a API Google foi suspensa", TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.i(TAG, "GoogleApiClient connection has failed");
		Toast toast = Toast.makeText(this, "Por favor instale ou atualize o Google Play Services!", Toast.LENGTH_LONG);
		toast.show();
		
		final String appPackageName = GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE; // getPackageName() from Context or Activity object
		ttsobject.speak("Por favor instale o Google Play Services você será redirecionado para a Play Store", TextToSpeech.QUEUE_FLUSH, null);
		try {
		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
		
	}

	@Override
	public void onLocationChanged(Location location) {
		String address = "";  
		Double latitude = location.getLatitude();
		Double longitude = location.getLongitude();
		if(location.getAccuracy() <=15)//default: 15
		{
			if(requestLocation==true){
				
			
				Latitude = Double.toString(latitude);
				Longitude = Double.toString(longitude);
				loadLoc = null;
	    		loadLoc = new LoadNearbyLocations();
				loadLoc.execute();
				requestLocation=false;
				requestVoice = true;
			}
			if(requestStreet==true){
				Latitude = Double.toString(latitude);
				Longitude = Double.toString(longitude);
				String phrase = "";
				//Get current street address
				address = GetAddress(latitude, longitude);  
				if(address != ""){
					phrase+="Você está na "+address+".";
				
				}
				requestStreet=false;
				ttsobject.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
		if(requestVoice==true){
			if(loadLoc != null)
			{
				if(loadLoc.getStatus() == AsyncTask.Status.FINISHED)
				{
					if(locationsList.size() != 0 ){
						String phrase = "";
						
							for(int i=0;i<locationsList.size();i++)
							{
								double Distance = Double.valueOf(locationsList.get(i).get(Consts.DISTANCE_IN_KM));
								Distance = Distance*1000;
								Distance = (double)Math.round(Distance * 1d) / 1d;
								if(Distance <= 250.0){
									//String phrase = "";
									phrase += " Você está a aproximadamente "+String.valueOf((int)Distance)+" metros de ";
									phrase += locationsList.get(i).get(Consts.DESCRIPTION);
									//ttsobject.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
									//locationsList.clear();
									//requestVoice=false;
								}
						}
						if(phrase != ""){
							ttsobject.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
							locationsList.clear();
							requestVoice=false;
						}else{
							ttsobject.speak("Não há nenhum ponto de interesse a menos de 250 metros de você", TextToSpeech.QUEUE_FLUSH, null);
							requestVoice=false;
						}
						}else{
							ttsobject.speak("Não foi encontrado nenhum local próximo de você", TextToSpeech.QUEUE_FLUSH, null);
						}
						
					}
				}
			}
			}	
		

	
	
	class LoadNearbyLocations extends AsyncTask<String, String, String>{

		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		protected String doInBackground(String... args) {
		// Building Parameters
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("latitude", Latitude));
	    params.add(new BasicNameValuePair("longitude", Longitude));
		// getting JSON string from URL
	    try
	    {
		JSONObject json = jParser.makeHttpRequest(Consts.GET_LOCATIONS, "GET", params);
		if(json != null){
		// Check your log cat for JSON reponse
		Log.d("locations: ", json.toString());
			int success = json.getInt(Consts.TAG_SUCCESS);
			if (success == 1) {
				locations = json.getJSONArray(Consts.LOCATIONS);
				for (int i = 0; i < locations.length(); i++) {
					JSONObject c = locations.getJSONObject(i);

					// Storing each json item in variable
					String description = c.getString(Consts.DESCRIPTION);
					String latitude = c.getString(Consts.LATITUDE);
					String longitude = c.getString(Consts.LONGITUDE);
					String distance = c.getString(Consts.DISTANCE_IN_KM);
					HashMap<String, String> map = new HashMap<String, String>();
					location = map;
					map.put(Consts.DESCRIPTION, description);
					map.put(Consts.LATITUDE, latitude);
					map.put(Consts.LONGITUDE, longitude);
					map.put(Consts.DISTANCE_IN_KM, distance);
					locationsList.add(map);
				}
			} 
		}else
		{
			ttsobject.speak("O servidor não pode ser acessado verifique a sua conexão com a internet", TextToSpeech.QUEUE_FLUSH, null);
		}
		} catch (JSONException e) {
			ttsobject.speak("O servidor não pode ser acessado verifique a sua conexão com a internet", TextToSpeech.QUEUE_FLUSH, null);
		}

		return null;
	}
		protected void onPostExecute(final String file_url) {
			

		}

	}
}
