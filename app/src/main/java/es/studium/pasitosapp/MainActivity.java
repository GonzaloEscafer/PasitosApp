package es.studium.pasitosapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private Handler handler = new Handler();

    private double ultimaLat = 0.0;
    private double ultimaLng = 0.0;

    // Referencias a la UI
    private TextView tvPosicion, tvBateria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        tvPosicion = findViewById(R.id.tvPosicion);
        tvBateria = findViewById(R.id.tvBateria);

        dbHelper = new DatabaseHelper(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        configurarGPS();
        iniciarBucleGuardado();
    }

    private void configurarGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                ultimaLat = location.getLatitude();
                ultimaLng = location.getLongitude();

                // Actualizar panel de posición
                tvPosicion.setText(String.format("Lat: %.5f\nLng: %.5f", ultimaLat, ultimaLng));

                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ultimaLat, ultimaLng), 15));
                }
            }
        });
    }

    private void iniciarBucleGuardado() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                guardarEnBaseDeDatos();
                handler.postDelayed(this, 300000);
            }
        }, 0);
    }

    private void guardarEnBaseDeDatos() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = (batteryStatus != null) ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : 0;

        // Actualizar panel de batería
        tvBateria.setText("Batería: " + level + "%");

        if (ultimaLat != 0.0) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("lat", ultimaLat);
            values.put("lng", ultimaLng);
            values.put("bat", level);

            db.insert("posiciones", null, values);
            db.close();

            Toast.makeText(this, "Posición guardada automáticamente", Toast.LENGTH_SHORT).show();
            actualizarMarcadores();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        actualizarMarcadores();
    }

    private void actualizarMarcadores() {
        if (mMap == null) return;
        mMap.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM posiciones", null);

        BitmapDescriptor icon = descriptorFromVector(this, R.drawable.marcador);

        if (cursor.moveToFirst()) {
            do {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng"));
                int bat = cursor.getInt(cursor.getColumnIndexOrThrow("bat"));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title("Batería: " + bat + "%")
                        .icon(icon));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    private BitmapDescriptor descriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}