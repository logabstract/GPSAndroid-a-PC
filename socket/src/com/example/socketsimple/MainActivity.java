package com.example.socketsimple;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

public class MainActivity extends Activity  implements SensorEventListener{
	String latitud;
	String longitud;
	String presicion;
        String altura;
	Socket socketGps = null;
	Socket socketBrujula = null;
	DataOutputStream dataOutputStreamGps = null;
	DataOutputStream dataOutputStreamBrujula = null;
	private LocationManager locationManager;
	Float azimut = 0.0f;  // View to draw a compass
	Float angulo = 0.0f;
	CustomDrawableView mCustomDrawableView;
	private SensorManager mSensorManager;
	Sensor accelerometer;
	Sensor magnetometer;
        float[] mGravity;
	float[] mGeomagnetic;
        
	public class CustomDrawableView extends View {
	    Paint paint = new Paint();
            
	    public CustomDrawableView(Context context) {
	      super(context);
	      paint.setColor(0xff00ff00);
	      paint.setStyle(Style.STROKE);
	      paint.setStrokeWidth(2);
	      paint.setAntiAlias(true);
	    }
	 
	    protected void onDraw(Canvas canvas) {
	      int width = getWidth();
	      int height = getHeight();
	      int centerx = width/2;
	      int centery = height/2;
	      canvas.drawLine(centerx, 0, centerx, height, paint);
	      canvas.drawLine(0, centery, width, centery, paint);
	      // Rotate the canvas with the azimut     
	      if (azimut != null){
	    	  canvas.drawText(azimut.toString(), centerx-20, centery+30, paint);
	    	  angulo=-azimut*360/(2*3.14159f);
	    	  canvas.rotate(angulo, centerx, centery);
	      }
	      paint.setColor(0xff0000ff);
	      canvas.drawLine(centerx, -1000, centerx, +1000, paint);
	      canvas.drawLine(-1000, centery, 1000, centery, paint);
	      canvas.drawText("N", centerx+5, centery-10, paint);
	      canvas.drawText("S", centerx-10, centery+15, paint);
	      paint.setColor(0xff00ff00);
	    }
	  }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mCustomDrawableView = new CustomDrawableView(this);
	    setContentView(mCustomDrawableView);   
	    
	    // Register the sensor listeners
	    mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
	    // obtener location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    
            LocationListener ll = new LocationListener(){

            @Override
            public void onLocationChanged(Location location) {
                Double lat = (Double) (location.getLatitude());
		Double lng = (Double) (location.getLongitude());
                Float pres = (Float) (location.getAccuracy());
                Double alt = (Double) (location.getAltitude());
                latitud = lat.toString();
		longitud = lng.toString();	
		presicion = pres.toString();
		altura = alt.toString();
                
		new Thread (new Runnable(){
                    public void run(){
                        try{
                            socketGps = new Socket("192.168.1.35", 4444);
                            dataOutputStreamGps = new DataOutputStream(socketGps.getOutputStream());
                            dataOutputStreamGps.writeUTF(latitud);
                            dataOutputStreamGps.writeUTF(longitud);
                            dataOutputStreamGps.writeUTF(altura);
                            dataOutputStreamGps.writeUTF(presicion);
                            dataOutputStreamGps.writeUTF(angulo.toString()+"º");
			} catch (UnknownHostException e){
                            e.printStackTrace();
			} catch (IOException e) {
                            e.printStackTrace();
			} finally {
                            if(socketGps != null){
                                try{
                                    socketGps.close();
				} catch (IOException e){
                                    e.printStackTrace();
				}
                            }
                            if (dataOutputStreamGps != null){
				try{
                                    dataOutputStreamGps.close();
				} catch (IOException e){
                                    e.printStackTrace();
				}
                            }		
			}
                    }
                 }).start();
		}

		@Override
                public void onProviderDisabled(String provider) {
                }
		@Override
                public void onProviderEnabled(String provider) {
                }
		@Override
		public void onStatusChanged(String provider, int status,Bundle extras) {
                }
	     };
	    
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
	    
	    // Initialize the location fields
	    if (location != null) {
	      ll.onLocationChanged(location);
	    } 
	}

	 @Override
	  protected void onResume() {
		 super.onResume();
		 mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		 mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	  }

	  /* no se actualiza mientras esta pausado */
	@Override
	  protected void onPause() {
            super.onPause();
            mSensorManager.unregisterListener(this);
	  }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		    mGeomagnetic = event.values;
		    if (mGravity != null && mGeomagnetic != null) {
		      float R[] = new float[9];
		      float I[] = new float[9];
		      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		      if (success) {
		        float orientation[] = new float[3];
		        SensorManager.getOrientation(R, orientation);
		        azimut = orientation[0]; // orientation contains: azimut, pitch and roll
		      }
		   }
		   mCustomDrawableView.invalidate();
	}
}
