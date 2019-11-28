package com.jfleischer.slideshow;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.jfleischer.slideshow.utils.SharedPreferencesUtil;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.QuickContactBadge;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SlideShowOptionsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show_options);

        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id));
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("B71432B27123EA98D0E7B329EBED524F")//Samsung galaxy tab A
                /*.setRequestAgent("android_studio:ad_template")*/.build();

        adView.loadAd(adRequest);

        setup_spinner();
        setup_fade_spinner();

        Switch controllable_switch = findViewById(R.id.controllable_switch);
        controllable_switch.setChecked(SharedPreferencesUtil.get_slideshow_controllable());
        controllable_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesUtil.set_slideshow_controllable(isChecked);
            }
        });


        findViewById(R.id.ad_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 go_to_video_ad();
            }
        });
    }

    private void setup_spinner(){
        Spinner duration_spinner = findViewById(R.id.duration_spinner);

        final List<String> keySet = new ArrayList<String>(){{
            add(getString(R.string.second, String.valueOf(2.5)));
            add(getString(R.string.second, String.valueOf(5)));
            add(getString(R.string.second, String.valueOf(10)));
            add(getString(R.string.minute, String.valueOf(1)));
        }};
        final HashMap<String, Integer> map = new HashMap<String, Integer>(){{
            put(keySet.get(0), 2500);
            put(keySet.get(1), 5000);
            put(keySet.get(2), 10000);
            put(keySet.get(3), 60000);

        }};
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, keySet);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration_spinner.setAdapter(dataAdapter);
        int current_duration = SharedPreferencesUtil.get_slide_duration();
        int i =0;
        for(String key: keySet){
            int value = map.get(key);
            if(value == current_duration){
                duration_spinner.setSelection(i);
                break;
            }
            i++;
        }
        duration_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                SharedPreferencesUtil.set_slide_duration(map.get(item));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setup_fade_spinner(){
        Spinner duration_spinner = findViewById(R.id.fade_duration_spinner);

        final List<String> keySet = new ArrayList<String>(){{
            add(getString(R.string.second, String.valueOf(1)));
            add(getString(R.string.second, String.valueOf(2.5)));
            add(getString(R.string.second, String.valueOf(5)));
        }};
        final HashMap<String, Integer> map = new HashMap<String, Integer>(){{
            put(keySet.get(0), 1000);
            put(keySet.get(1), 2500);
            put(keySet.get(2), 5000);
        }};

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keySet);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration_spinner.setAdapter(dataAdapter);
        int current_duration = SharedPreferencesUtil.get_fade_duration();
        int i =0;
        for(String key: keySet){
            int value = map.get(key);
            if(value == current_duration){
                duration_spinner.setSelection(i);
                break;
            }
            i++;
        }
        duration_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                SharedPreferencesUtil.set_fade_duration(map.get(item));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void go_to_video_ad(){
        Intent intent = new Intent(this, AdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slide_show_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent = null;
        if (id == R.id.action_options) {
            intent = new Intent(this, SlideShowOptionsActivity.class);
        } else if (id == R.id.action_add) {
            intent = new Intent(this, AddSlideActivity.class);
        } else if (id == R.id.action_remove) {
            intent = new Intent(this, RemoveSlideActivity.class);
        } else if (id == R.id.action_return) {
            intent = new Intent(this, SlideShowActivity.class);
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
