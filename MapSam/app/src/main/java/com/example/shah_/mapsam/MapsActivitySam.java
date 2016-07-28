package com.example.shah_.mapsam;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivitySam extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button startbutton,stopbutton;
    Sensor accelerometer;
    SensorManager sm;
    TextView potholes;
    int inclinationz,inclinationy,inclinationx ;
    int anglex=0,anglez= 0;
    ArrayList<ArrayList<Double>> longlat = new ArrayList<ArrayList<Double>>();
    boolean startdection = false,stopdection = false;
    double potlongitute,potlattitude,longitude,lattitude;
    //HashMap<Integer,HashSet<Double>> hashmaplong = new HashMap<Integer,HashSet<Double>>();
    //HashMap<Integer,HashSet<Double>> hashmaplat = new HashMap<Integer, HashSet<Double>>();
    HashMap<Integer,HashSet<Double>> hashmap = new HashMap<Integer,HashSet<Double>>();
    Firebase myfirebase ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_sam);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Firebase.setAndroidContext(this);
        myfirebase.child("http://potholedetector.firebaseio.com/");
        startbutton = (Button)findViewById(R.id.startbutton);
        stopbutton = (Button) findViewById(R.id.stopbutton);
        sm  = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        potholes = (TextView)findViewById(R.id.countpotholes);
        potholes.setText("count pothole");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(startdection == true && stopdection == false) {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(loc));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    lattitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", 20000, 0, locationListener);

        startbutton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        startdection = true;
                        stopdection = false;
                        anglez = inclinationz;
                        Log.w("Ron","started");
                        hashmap.clear();
                        mMap.clear();

                    }
                }
        );
        stopbutton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        startdection = false;
                        stopdection = true;
                        mMap.clear();
                        Log.w("Ron","stop");
                        int count=0;
                        Set<Double> setlon = new HashSet<Double>();
                        Set<Double> setlat = new HashSet<Double>();
                        for (List<Double> row : longlat) {
                            //for (int j = 0; j < row.size(); ++j) {
                            // operate on the values here.
                            double valuelat = row.get(1);
                            double valuelon = row.get(0);

                            Log.w("valuelon",""+valuelon);
                            Log.w("valuelat",""+valuelat);


                            valuelat = Math.round(valuelat*10000.0)/10000.0;
                            valuelon = Math.round(valuelon*10000.0)/10000.0;

                            if((setlon.contains(valuelon) && setlat.contains(valuelat))){

                            }else{
                                setlon.add(valuelon);
                                setlat.add(valuelat);
                                HashSet<Double> set = new HashSet<Double>();
                                set.add(valuelat);
                                set.add(valuelon);

                                hashmap.put(count,set);
                                count++;

                            }

                            //}
                        }
                        Log.w("lsize",""+longlat.size());
                        Log.w("Ssize",""+hashmap.size());
                        longlat.clear();
                        int countpot = 0;
                        for (Map.Entry<Integer, HashSet<Double>> entry : hashmap.entrySet()) {
                            double[] loc = new double[2];
                            int i=0;
                            //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                            for(double d:entry.getValue()){
                                Log.w("H1 ",""+d);
                                loc[i]=d;
                                i++;
                            }
                            Log.w("Set","Set");
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pothole);
                            MarkerOptions marker = new MarkerOptions().position(new LatLng(loc[1], loc[0])).title("Pothole Reported");
                            myfirebase.child("potholedetector").setValue("1");
                            marker.icon(icon);
                            countpot++;
                            mMap.addMarker(marker);
                        }
                        potholes.setText(" "+countpot + "   potholes");



                    }
                }
        );



        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(startdection == true && stopdection == false) {
                    float[] g = new float[3];
                    g = event.values.clone();

                    double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);
                    g[0] = (float) (g[0] / norm_Of_g);
                    g[1] = (float) (g[1] / norm_Of_g);
                    g[2] = (float) (g[2] / norm_Of_g);
                    inclinationz = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
                    inclinationy = (int) Math.round(Math.toDegrees(Math.acos(g[1])));
                    inclinationx = (int) Math.round(Math.toDegrees(Math.acos(g[0])));
                    if (startdection) {
                        if (Math.abs(anglez - inclinationz) > 10) {
                            potlattitude = lattitude;
                            potlongitute = longitude;
                            ArrayList<Double> list = new ArrayList<Double>();
                            list.add(potlongitute);
                            list.add(potlattitude);
                            longlat.add(list);

                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng indy = new LatLng(39.7684, 86.1581);
        mMap.addMarker(new MarkerOptions().position(indy).title("Indianapolis"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(indy));

    }

}
