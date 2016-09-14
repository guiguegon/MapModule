package es.guiguegon.mapmodule.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import es.guiguegon.mapmodule.MapActivity;
import es.guiguegon.mapmodule.model.Place;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_MAP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((view) -> openMap());
    }

    public void openMap() {
        startActivityForResult(MapActivity.getCallingIntent(this), REQUEST_CODE_MAP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            Place place = data.getExtras().getParcelable(MapActivity.RESULT_PLACE);
            if (place != null) {
                Toast.makeText(this, "Place selected: " + place.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
