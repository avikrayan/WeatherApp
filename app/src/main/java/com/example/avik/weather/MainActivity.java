package com.example.avik.weather;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    TextView cityField, detailsField, currentTemperatureField, temp_min_max_field, humidity_field, pressure_field, weatherIcon, updatedField, wind_field;
    ImageButton  btnSearch, btnRefresh, btnMenu;
    Typeface weatherFont;
    Function.placeIdTask asyncTask;
    private ProgressBar progressbar;

    boolean doubleBackToExitPressedOnce = false;
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private String locationAddress;
    private double lng;
    private double lat;
    private LatLng latLng;
    private String locationName;

    private TextView tvDetails_sunrise, tvDetails_sunset,tvDetails_windSpeed, tvdetails_pressure, tvdetails_humidity, tvdetails_visibility, tvDetails_Weathericon, tvDetails_weatherType, tvDetails_temperature, tvDetails_minTemp, tvDetails_maxTemp;

    private String latitude;
    private String longitude;

    final int LOCATION_PERMISSION = 100;
    private GPSTracker gpsTracker;
    private double myLat;
    private double myLng;
    private String currentLatitude;
    private String currentLongitude;
    private RelativeLayout contentlayout;

    private TextView btnExploreDetails, btnHomepage;
    private RelativeLayout mainScreenLayout, detailsScreenLayout;
    private Window window;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.statusbar_color));
        }

        contentlayout = (RelativeLayout) findViewById(R.id.contentlayout);
        checkInternetConnection();
    }

    private void checkInternetConnection() {
        ConnectivityManager ConnectionManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=ConnectionManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()==true )
        {
            //Toast.makeText(MusicGallary.this, "Network Available", Toast.LENGTH_LONG).show();
            //Snackbar.make(contentlayout, "Welcome", Snackbar.LENGTH_LONG).show();
            initViews();
            getCurrrentlocation();
        }
        else
        {
            //Toast.makeText(MusicGallary.this, "Network Not Available", Toast.LENGTH_LONG).show();
            Snackbar.make(contentlayout, "Network Not Available", Snackbar.LENGTH_INDEFINITE).setAction("Retry",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkInternetConnection();
                        }
                    }).show();
        }
    }

    private void getCurrrentlocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
        } else {
            gpsTracker = new GPSTracker(this);
            if (gpsTracker.canGetLocation()) {
                myLat = gpsTracker.getLatitude();
                myLng = gpsTracker.getLongitude();
                currentLatitude = String.valueOf(myLat);
                currentLongitude = String.valueOf(myLng);
                System.out.println("latllong : "+currentLatitude+currentLongitude);

                updateUI(currentLatitude,currentLongitude);
            }
        }

    }

    private void initViews() {

        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");

        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        cityField = (TextView)findViewById(R.id.city_field);
        updatedField = (TextView)findViewById(R.id.updated_field);
        detailsField = (TextView)findViewById(R.id.details_field);
        currentTemperatureField = (TextView)findViewById(R.id.current_temperature_field);
        temp_min_max_field = (TextView)findViewById(R.id.temp_min_max_field);
        humidity_field = (TextView)findViewById(R.id.humidity_field);
        pressure_field = (TextView)findViewById(R.id.pressure_field);
        wind_field = (TextView)findViewById(R.id.wind_field);
        weatherIcon = (TextView)findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
        btnMenu = (ImageButton) findViewById(R.id.btnMenu);

        tvDetails_sunrise = (TextView) findViewById(R.id.tvDetails_sunrise);
        tvDetails_sunset = (TextView) findViewById(R.id.tvDetails_sunset);
        tvDetails_windSpeed = (TextView) findViewById(R.id.tvDetails_windSpeed);
        tvdetails_pressure = (TextView) findViewById(R.id.tvdetails_pressure);
        tvdetails_humidity = (TextView) findViewById(R.id.tvdetails_humidity);
        tvdetails_visibility = (TextView) findViewById(R.id.tvdetails_visibility);
        tvDetails_weatherType = (TextView) findViewById(R.id.tvDetails_weatherType);
        tvDetails_temperature = (TextView) findViewById(R.id.tvDetails_temperature);
        tvDetails_minTemp = (TextView) findViewById(R.id.tvDetails_minTemp);
        tvDetails_maxTemp = (TextView) findViewById(R.id.tvDetails_maxTemp);
        tvDetails_Weathericon = (TextView) findViewById(R.id.tvDetails_Weathericon);
        tvDetails_Weathericon.setTypeface(weatherFont);

        btnExploreDetails = (TextView) findViewById(R.id.btnExploreDetails);
        btnHomepage = (TextView) findViewById(R.id.btnHomepage);
        mainScreenLayout = (RelativeLayout) findViewById(R.id.mainScreenLayout);
        detailsScreenLayout = (RelativeLayout) findViewById(R.id.detailsScreenLayout);

        btnSearch.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnExploreDetails.setOnClickListener(this);
        btnHomepage.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
    }

    private void updateUI(String lat,String lon ){

            asyncTask =new Function.placeIdTask(new Function.AsyncResponse() {
            public void processFinish(String weather_city, String weather_description, String weather_temperature, String weather_tempMin, String weather_tempMax, String weather_humidity, String weather_pressure, String wind_speed, String sunriseTime, String sunsetTime, String visibility, String weather_updatedOn, String weather_iconText, String sun_rise) {

                if (weather_city!=null){
                    progressbar.setVisibility(View.GONE);
                    btnExploreDetails.setVisibility(View.VISIBLE);
                }
                /////Convert //////
                int current_temperature = (int) Double.parseDouble((weather_temperature));
                int min_temperature = ((int) Double.parseDouble((weather_tempMin)))-3;
                int max_temperature = ((int) Double.parseDouble((weather_tempMax)))+3;

                System.out.println("temp : "+current_temperature);
                System.out.println("temp : "+min_temperature);
                System.out.println("temp : "+max_temperature);

                long sunrise = Long.parseLong(sunriseTime);
                long sunset = Long.parseLong(sunsetTime);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date sunRiseTime = new Date(sunrise*1000);
                Date sunSetTime = new Date(sunset*1000);

                String sunRiseTimeString = sdf.format(sunRiseTime);
                String sunSetTimeString = sdf.format(sunSetTime);

                System.out.println("sunrise : "+sunRiseTimeString+"AM");
                System.out.println("sunset : "+sunSetTimeString+"PM");

                float visibilityString = (Float.parseFloat(visibility))/1000;
                System.out.println("visibility : "+visibilityString);

                cityField.setText(weather_city);
                updatedField.setText(weather_updatedOn);
                detailsField.setText(weather_description);
                currentTemperatureField.setText(""+current_temperature +"°c");
                temp_min_max_field.setText("Min Temp : "+min_temperature+"°c"
                        +"   ≈   "+"Max Temp : " +max_temperature+"°c" );
                wind_field.setText("Wind Speed : "+wind_speed);
                humidity_field.setText("Humidity : "+weather_humidity);
                pressure_field.setText("Pressure : "+weather_pressure);
                weatherIcon.setText(Html.fromHtml(weather_iconText));

                detailsUIUpdate(weather_city, weather_description,  current_temperature, min_temperature, max_temperature, weather_humidity, weather_pressure, wind_speed, sunRiseTimeString, sunSetTimeString, weather_updatedOn, weather_iconText, sun_rise , visibilityString);

            }
        });
        asyncTask.execute(lat,lon); //  asyncTask.execute("Latitude", "Longitude")

    }

    private void detailsUIUpdate(String weather_city, String weather_description, int weather_temperature, int weather_tempMin, int weather_tempMax, String weather_humidity, String weather_pressure, String wind_speed, String sunriseTime, String sunsetTime, String weather_updatedOn, String weather_iconText, String sun_rise, float visibilityString) {
        tvdetails_pressure.setText(weather_pressure);
        tvDetails_sunrise.setText(sunriseTime);
        tvDetails_sunset.setText(sunsetTime);
        tvDetails_windSpeed.setText("Speed  :   "+wind_speed);
        tvdetails_humidity.setText(weather_humidity);
        tvDetails_Weathericon.setText(Html.fromHtml(weather_iconText));
        tvDetails_weatherType.setText(weather_description);
        tvDetails_minTemp.setText("Min Temp : ≈  "+weather_tempMin+"°c");
        tvDetails_maxTemp.setText("Max Temp : ≈  " +weather_tempMax+"°c");
        tvDetails_temperature.setText(""+weather_temperature +"°c");
        tvdetails_visibility.setText(String.valueOf(visibilityString)+"  km");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSearch :
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    // dialogView.showSingleButtonDialog(getResources().getString(R.string.app_name), e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    // dialogView.showSingleButtonDialog(getResources().getString(R.string.app_name), e.getMessage());
                }
                break;

            case R.id.btnRefresh :
//                Intent intent = new Intent(this,MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                updateUI(currentLatitude,currentLongitude);
                Toast.makeText(this, "Refreshing ...", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnExploreDetails :
                mainScreenLayout.setVisibility(View.GONE);
                detailsScreenLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.btnHomepage :
                mainScreenLayout.setVisibility(View.VISIBLE);
                detailsScreenLayout.setVisibility(View.GONE);
                break;

            case R.id.btnMenu :
                PopupMenu popup = new PopupMenu(MainActivity.this, btnMenu);
                popup.getMenuInflater().inflate(R.menu.popup_menu,popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;

            case R.id.themeYellow :
                contentlayout.setBackgroundResource(R.drawable.background_yellow);
                window = MainActivity.this.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.statusbar_color_yellow));
                }
                dialog.cancel();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                latLng = place.getLatLng();
                lat = latLng.latitude;
                lng = latLng.longitude;
                locationAddress = place.getAddress().toString();
                locationName = place.getName().toString();
                updateUI(String.valueOf(lat),String.valueOf(lng));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("tag", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(this);
                    if (gpsTracker.canGetLocation()) {

                        myLat = gpsTracker.getLatitude();
                        myLng = gpsTracker.getLongitude();
                        currentLatitude = String.valueOf(myLat);
                        currentLongitude = String.valueOf(myLng);

                        updateUI(currentLatitude,currentLongitude);
                    } else {
                        //dialogView.dismissCustomSpinProgress();
                        //dialogView.showSingleButtonDialog(getResources().getString(R.string.try_again), getResources().getString(R.string.app_location_permission));
                    }
                } else {
                    //dialogView.dismissCustomSpinProgress();
                   // dialogView.showSingleButtonDialog(getResources().getString(R.string.try_again), getResources().getString(R.string.app_location_permission));
                }
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btnThemeMenu :
                showDialoge();
                //Toast.makeText(this, "Theme", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnSettingsMenu :
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnAboutMenu :
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void showDialoge() {
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.change_theme_dialoge);

        TextView darkTheme = dialog.findViewById(R.id.themeDark);
        TextView themeLight = dialog.findViewById(R.id.themeLight);
        TextView themeYellow = dialog.findViewById(R.id.themeYellow);
        ImageButton btnDialogeClose = dialog.findViewById(R.id.btnDialogeClose);

        btnDialogeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        darkTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentlayout.setBackgroundResource(R.drawable.background_dark);
                window = MainActivity.this.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.statusbar_color_dark));
                }
                dialog.cancel();
            }
        });

        themeLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                contentlayout.setBackgroundResource(R.drawable.background);
                window = MainActivity.this.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.statusbar_color));
                }
                dialog.cancel();
            }
        });

        themeYellow.setOnClickListener(this);

        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        Snackbar.make(contentlayout, "Please click BACK again to exit", Snackbar.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
