package es.guiguegon.mapmodule.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import es.guiguegon.mapmodule.MapActivity;

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
        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            //Toast.makeText(this, "Gallery Media selected: " + galleryMedias.size(), Toast.LENGTH_LONG).show();
        }
    }
}
