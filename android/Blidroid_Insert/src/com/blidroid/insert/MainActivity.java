package com.blidroid.insert;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener{
	//Declare Global Variables
	String Latitude;
	String Longitude;
	String Description;
	int Success;
	boolean requestCreateNewLocation;
	//Create Global Components
	private ProgressDialog progressDialog;
	EditText txtDescription;
	//JSONParser jsonParser = new JSONParser();
	JSONParser jsonParser = new JSONParser();
	
	// url to the service
	private static String url_create = "http://audiodescriptionfortheblind.esy.es/insertLocation.php";
	 
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	
	//Tag to make easier to find messages from the application at logcat
	private final String TAG = "Blidroid"; 
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("Blidroid - Inserção de Dados");
		
		//Create an instance of the Location Manager to verify if the Locations are active
		LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		boolean location_enabled = false;
		try {
        location_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	} catch (Exception ex) {
        	Log.e("MainActivity", ex.getMessage());
    	}
		if (! location_enabled) {
            Dialog dialog = createDialog();
            dialog.show();
    	}
		
		//Declare variables for Layout components
		Button btnAdd = (Button)findViewById(R.id.btnAddDescription);		
		txtDescription = (EditText)findViewById(R.id.txtDescription);
		
		//Create instance of GoogleApiClient to use FusedLocation
		mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
		
		if(!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			Toast.makeText(MainActivity.this, "Ative as Redes Moveis (Alta precisão) para localizações mais precisas!", Toast.LENGTH_LONG).show();
		}
		
		//When the user clicks on the btnAdd:
		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Verify if Wi-Fi is connected
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				
				//Verify if mobile data is connected
				NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (mWifi.isConnected() || mMobile.isConnected()) {
				    try{
				    	requestCreateNewLocation = true;
				    	//new CreateNewLocation().execute();
				    }catch(Exception ex){
				    	ex.getMessage();
				    }
				}else{
					Toast.makeText(MainActivity.this, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
					
				}
				
			}
			
		});
		
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
	
	
     //Background Async Task to Create a new location
    
    class CreateNewLocation extends AsyncTask<String, String, String> {
 
        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Adicionando descrição...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
 
        //Creating location
        protected String doInBackground(String... args) {
        	
        	if(Latitude != null && Longitude != null){
        	final EditText txtDescription = (EditText)findViewById(R.id.txtDescription);
            Description = txtDescription.getText().toString();
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("latitude", Latitude));
            params.add(new BasicNameValuePair("longitude", Longitude));
            params.add(new BasicNameValuePair("description", Description));
            
            // getting JSON Object	  
            try{
            JSONObject json = jsonParser.makeHttpRequest(url_create,"GET", params);
            Log.d("Create Response", json.toString());
            try {
	            // check for success tag
	         
	                Success = json.getInt(TAG_SUCCESS);
	 
	                
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
            }catch(Exception ex){
            	ex.printStackTrace();
            }
        	}else{
        		Success = 2;
        		
        	}
           
       
        	return null;
        }
 
        //After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        	progressDialog.dismiss();
            if (Success == 1) {
            	Toast.makeText(MainActivity.this, "A descrição: '"+Description+"' foi adicionada com sucesso!", Toast.LENGTH_LONG).show();
            	txtDescription.setText("");
            } else if(Success == 2) {
            	Toast.makeText(MainActivity.this, "Não foi possível obter sua localização geográfica. Espere alguns segundos e tente novamente!", Toast.LENGTH_LONG).show();
            }else{
            	Toast.makeText(MainActivity.this, "Ocorreu um erro ao tentar adicionar a descrição!", Toast.LENGTH_LONG).show();
            }	
            
        }
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
	public void onLocationChanged(Location location) {
		if(location != null){
			if(location.getAccuracy() < 15){
				if(requestCreateNewLocation==true){
					double latitude = location.getLatitude();
					double longitude = location.getLongitude();
					Latitude = Double.toString(latitude);
					Longitude = Double.toString(longitude);
					new CreateNewLocation().execute();
					requestCreateNewLocation = false;
					//Valor Arredondado
					//Longitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
					//Latitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
				}
			}
			
		}
		
		//Toast toast = Toast.makeText(this, "Location received: " + location.toString(), Toast.LENGTH_LONG);
		//toast.show();
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		//https://developers.google.com/android/guides/api-client#handle_connection_failures
		//https://developers.google.com/android/guides/setup
		Log.i(TAG, "GoogleApiClient connection has failed");
		Toast toast = Toast.makeText(this, "Por favor instale ou atualize o Google Play Services!", Toast.LENGTH_LONG);
		toast.show();
		
		final String appPackageName = GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE; // getPackageName() from Context or Activity object
		try {
		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
		
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
		Toast toast = Toast.makeText(this, "GoogleApiClient connection has been suspend", Toast.LENGTH_LONG);
		toast.show();
		
	}
}
