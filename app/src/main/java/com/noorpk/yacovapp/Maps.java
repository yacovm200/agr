package com.noorpk.yacovapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.location.LocationManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap googleMap;
    ImageView imageView;
    private Marker marker;
    private View view;
    public String url;
    public String[] ImageUrl;

    //for the current location on map
    LocationManager locationManager;
    public String latitude, longitude;
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getCurrentLocation();

    }


    void google_api() {
        final ProgressDialog dialog = new ProgressDialog(Maps.this);
        dialog.setMessage("Loading...");
        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://kerron.xyz/htdocs/get-my-locations.php", new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(String result) {
                dialog.dismiss();
                if (result.equals("0")) {
                    Toast.makeText(Maps.this, "No Record found", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONArray array = new JSONArray(result);
                    ImageUrl = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        JSONObject object = array.getJSONObject(i);
                        ImageUrl[i] = object.getString("image_path");
                        //url image from json object, to show on each mark his specific image.
                        //url = object.getString("image_path");
                        Log.i("ImageUrl[i]:", "ImageUrl[i]:" + ImageUrl[i]);
                        markerOptions.position(new LatLng(Double.parseDouble(object.getString("lat")), Double.parseDouble(object.getString("lon"))));
                        googleMap.addMarker(markerOptions.title("details about this marker:").snippet("more details!!!!!")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blackmarker3))
                        .snippet(ImageUrl[i]));

                        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                view = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
                                Maps.this.marker = marker;
                                ImageView image = (ImageView) view.findViewById(R.id.imageViewRoad);
                                Picasso.with(getApplicationContext())
                                        .load(marker.getSnippet())
                                        .error(R.mipmap.ic_launcher)
                                        .into(image);
                                return view;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                if (Maps.this.marker != null
                                        && Maps.this.marker.isInfoWindowShown()) {
                                    Maps.this.marker.hideInfoWindow();
                                    Maps.this.marker.showInfoWindow();
                                }
                                return null;
                            }
                        });
                    }//for loop end
                    //move first to current location
                    googleMap.setMyLocationEnabled(true);//blue point off current location
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 14.0f));
                    Toast.makeText(Maps.this, "current Location For Testing:" + "\nlatitude " + latitude + " \nlongitude " + longitude, Toast.LENGTH_SHORT).show();
                    Log.i("LAT:", "latyacov:" + latitude);
                    Log.i("LON:", "lonyacov:" + longitude);

                    //}
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();
                Toast.makeText(Maps.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("email", getSharedPreferences("user", MODE_PRIVATE).getString("id", ""));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError volleyError) throws VolleyError {

            }
        });
        MySingleton.getInstance(Maps.this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            this.googleMap = googleMap;
            google_api();
        }
    }

    //this function help us to open the map on the current location of the user.
    private void getCurrentLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                Maps.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                Maps.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
