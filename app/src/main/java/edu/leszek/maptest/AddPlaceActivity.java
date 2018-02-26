package edu.leszek.maptest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import static edu.leszek.maptest.Location.PLACES_LAT_KEY;
import static edu.leszek.maptest.Location.PLACES_LONG_KEY;
import static edu.leszek.maptest.MapsActivity.PLACES_AMOUNT_KEY;
import static edu.leszek.maptest.MapsActivity.PREFERENCES_NAME;

public class AddPlaceActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_place);

        Intent intent = getIntent();
        final double lat = intent.getDoubleExtra(PLACES_LAT_KEY, 0.0);
        final double lng = intent.getDoubleExtra(PLACES_LONG_KEY, 0.0);

        TextView latlang = findViewById(R.id.latlangText);
        String pos = String.format(Locale.US, "%.6f, %.6f", lat, lng);
        latlang.setText(pos);

        Button addButton = findViewById(R.id.addButton);
        final Activity mainActivity = this;
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameEditText = findViewById(R.id.nameText);
                EditText descriptionEditText = findViewById(R.id.descriptionText);
                EditText radiusEditText = findViewById(R.id.radiusText);

                if ("".equals(nameEditText.getText().toString()) || "".equals(descriptionEditText.getText().toString()) || "".equals(radiusEditText.getText().toString()))
                {
                    Toast.makeText(mainActivity, "Nie wszystkie pola są wypełnione!", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                int savedPlaces = preferences.getInt(PLACES_AMOUNT_KEY, 0);
                edu.leszek.maptest.Location location = new edu.leszek.maptest.Location(new LatLng(lat, lng),
                        Float.valueOf(radiusEditText.getText().toString()),
                        nameEditText.getText().toString(),
                        descriptionEditText.getText().toString());

                location.saveToSharedPreferences(savedPlaces, preferences);
                savedPlaces++;

                editor.putInt(PLACES_AMOUNT_KEY, savedPlaces);
                editor.commit();

                Toast.makeText(mainActivity, "Dodano nowe miejsce: " + nameEditText.getText().toString(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mainActivity, MapsActivity.class);
                intent.putExtra(PLACES_LAT_KEY, lat);
                intent.putExtra(PLACES_LONG_KEY, lng);
                mainActivity.startActivity(intent);

                finish();
            }
        });
    }
}
