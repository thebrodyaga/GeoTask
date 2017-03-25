package com.example.pickup.geotask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Intent intent;
    private Integer THRESHOLD = 3;
    private DelayAutoCompleteTextView geo_autocomplete_from;
    private ImageView geo_autocomplete_clear_from;
    private DelayAutoCompleteTextView geo_autocomplete_to;
    private ImageView geo_autocomplete_clear_to;
    private GoogleMap mMap;
    private Marker from;
    private LatLng fromLatLng;
    private Marker to;
    private LatLng toLatLng;
    boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        Button button = (Button) findViewById(R.id.button);
        // инициализация
        tabHost.setup();
        TabHost.TabSpec tabSpec;
        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("Откуда");
        tabSpec.setContent(R.id.tab1);
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Куда");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        //Вкладка откуда, начало
        geo_autocomplete_clear_from = (ImageView) findViewById(R.id.geo_autocomplete_clear_from);
        geo_autocomplete_from = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete_from);
        geo_autocomplete_from.setThreshold(THRESHOLD); //Минимальное кол-во символов для начала поиска
        geo_autocomplete_from.setAdapter(new GeoAutoCompleteAdapter(this));

        geo_autocomplete_from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                geo_autocomplete_from.setText(result.getAddress());
                check = true; //для определия какой маркер добавить
                fromLatLng = null;
                fromLatLng = getLocationFromAddress(MainActivity.this, result.getAddress());
                addMarkersToMap(fromLatLng, check);
                hideKeyboard(MainActivity.this);

            }
        });

        geo_autocomplete_from.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    geo_autocomplete_clear_from.setVisibility(View.VISIBLE);
                } else {
                    geo_autocomplete_clear_from.setVisibility(View.GONE);
                }
            }
        });

        geo_autocomplete_clear_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                geo_autocomplete_from.setText("");
            }
        });
        //Вкладка откуда, конец

        //Вкладка куда, начало
        geo_autocomplete_clear_to = (ImageView) findViewById(R.id.geo_autocomplete_clear_to);
        geo_autocomplete_to = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete_to);
        geo_autocomplete_to.setThreshold(THRESHOLD); //Минимальное кол-во символов для начала поиска
        geo_autocomplete_to.setAdapter(new GeoAutoCompleteAdapter(this));

        geo_autocomplete_to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                geo_autocomplete_to.setText(result.getAddress());
                check = false; //для определия какой маркер добавить
                toLatLng = null;
                toLatLng = getLocationFromAddress(MainActivity.this, result.getAddress());
                addMarkersToMap(toLatLng, check);
                hideKeyboard(MainActivity.this);

            }
        });

        geo_autocomplete_to.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    geo_autocomplete_clear_to.setVisibility(View.VISIBLE);
                } else {
                    geo_autocomplete_clear_to.setVisibility(View.GONE);
                }
            }
        });

        geo_autocomplete_clear_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                geo_autocomplete_to.setText("");
            }
        });
        //Вкладка куда, конец
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = new Intent(this, MapsActivity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromLatLng == null)
                    Toast.makeText(MainActivity.this, "Выбери пункт отправления", Toast.LENGTH_SHORT).show();
                else if (toLatLng == null)
                    Toast.makeText(MainActivity.this, "Выбери пункт назначения", Toast.LENGTH_SHORT).show();
                else {
                    intent.putExtra("from", fromLatLng);
                    intent.putExtra("to", toLatLng);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void addMarkersToMap(LatLng latLng, boolean check) {
        // Uses a colored icon.
        if (latLng == null)
            return;
        if (check) {
            if (from != null) {
                from.remove();
                from = null;
            }
            from = mMap.addMarker(new MarkerOptions()
                    .position(latLng));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17)); //анимация
        } else {
            if (to != null) {
                to.remove();
                to = null;
            }
            to = mMap.addMarker(new MarkerOptions()
                    .position(latLng));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17)); //анимация
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    public static void hideKeyboard(Context context) {

        try {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            View view = ((Activity) context).getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
