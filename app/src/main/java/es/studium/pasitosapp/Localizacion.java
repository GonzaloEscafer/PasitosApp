package es.studium.pasitosapp;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.annotation.NonNull;

public class Localizacion implements LocationListener {
    private MainActivity mainActivity;

    // Recibimos la MainActivity para poder enviarle los datos
    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Llamamos a un método en MainActivity para actualizar la UI y el mapa
        mainActivity.actualizarPosicionUI(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}
}
