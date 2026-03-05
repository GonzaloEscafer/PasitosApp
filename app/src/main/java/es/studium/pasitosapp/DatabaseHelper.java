package es.studium.pasitosapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, "Pasitos.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla para guardar: ID, Latitud, Longitud y Batería
        db.execSQL("CREATE TABLE posiciones (id INTEGER PRIMARY KEY AUTOINCREMENT, lat REAL, lng REAL, bat INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {}
}