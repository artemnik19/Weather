package com.example.weather;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

class Wind {
    int deg;
    float speed;

    @Override
    public String toString() {
        return "Wind{" +
                "deg=" + deg +
                ", speed=" + speed +
                '}';
    }
}
class Clouds{
    int all;

    @Override
    public String toString() {
        return "Clouds{" +
                "all=" + all +
                '}';
    }
}
class Sys{
    int sunrise, sunset;

    @Override
    public String toString() {
        return "Sys{" +
                "sunrise=" + sunrise +
                ", sunset=" + sunset +
                '}';
    }
}
class Weather{
    int id;
    String main, description, icon;

    @Override
    public String toString() {
        return "Weather{" +
                "id=" + id +
                ", main='" + main + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
class M{
    float temp, temp_min, temp_max;
    int pressure, humidity;

    @Override
    public String toString() {
        return "M{" +
                "temp=" + temp +
                ", temp_min=" + temp_min +
                ", temp_max=" + temp_max +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                '}';
    }
}
public class MainActivity extends AppCompatActivity {

    TextView tDate, tTemp, tHumidity, tPressure, tWind, tClouds, tLength_day;
    EditText etCity;
    GisAsyncTask gisTask;
    private static final String TAG = "forecast debug "; // тег для  лога
    public static final String CHANNEL = "GIS_SERVICE";
    public static final String INFO = "INFO";
    String city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etCity      = (EditText) findViewById(R.id.city);

        tDate = (TextView) findViewById(R.id.date);
        tTemp = (TextView) findViewById(R.id.temp);
        tPressure = (TextView) findViewById(R.id.pressure);
        tClouds = findViewById(R.id.clouds);
        tHumidity = findViewById(R.id.humidity);
        tWind = findViewById(R.id.wind);
        tLength_day = findViewById(R.id.length_day);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClick(View v)
    {
        Toast.makeText(this, "получаем данные о погоде", Toast.LENGTH_SHORT).show();
        //city = Integer.parseInt(etCity.getText().toString());
        //if (city < 0)
        //{
        //    city = 21;
        //}
        city = etCity.getText().toString();

        gisTask  = new GisAsyncTask();
        gisTask.execute();
    }

    private class GisAsyncTask extends AsyncTask<Void,Void,String>{
        @TargetApi(Build.VERSION_CODES.O)
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String aVoid) {
            int temp_in_celc;
            Intent i = new Intent(CHANNEL);
            i.putExtra(INFO, aVoid);
            sendBroadcast(i);
            Log.d(TAG, aVoid); // пишем в лог всю строку, что скачали с сайта

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            OpenWeatherMap owm = gson.fromJson(aVoid,OpenWeatherMap.class);

            temp_in_celc = (int) (owm.main.temp - 273.15);
            String temp = Integer.toString(temp_in_celc);

            long owm_date = owm.dt;
            Date normal_date = new Date (owm_date * 1000);
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String date = sfd.format(normal_date);

            String pressure = Integer.toString(owm.main.pressure);

            String clouds =  Integer.toString(owm.clouds.all);

            int sunrise = owm.sys.sunrise;
            int sunset = owm.sys.sunset;
            int len_day = sunset-sunrise;
            int hour = len_day /3600;
            int minute = (len_day%3600) /60;

            String wind = Float.toString(owm.wind.speed);

            String humidity = Integer.toString(owm.main.humidity);





            tDate.setText("date: "+date);
            tTemp.setText("temp: "+temp+" 'C");
            tPressure.setText("pressure: "+pressure+" hPa");
            tClouds.setText("clouds: "+clouds);
            tLength_day.setText("length of day: "+Integer.toString(hour)+" hours "+Integer.toString(minute)+" minutes");
            tWind.setText("wind: "+wind+"m/s");
            tHumidity.setText("humidity: "+humidity+" %");


        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=9ecc5d456598b0438ea92172c63e088e");
                Scanner in = new Scanner((InputStream) url.getContent());
                result = in.nextLine();
            }
            catch (Exception e) {
                result = "не удалось загрузить информацию о погоде" + e.toString();
            }
            return result;
        }
    }

    }


    //String s = "";
        //http://api.openweathermap.org/data/2.5/weather?id=6753765&appid=9ecc5d456598b0438ea92172c63e088e
        //try {
            // http://developer.alexanderklimov.ru/android/theory/assets.php
           // AssetManager am = getAssets();
           // InputStream stream = am.open("forecast.json");
          //  Scanner sc = new Scanner(stream);
         //   s = sc.nextLine();
       // } catch (IOException e) {
       // }
       // Log.d("mytag", s);
       // try {
            // http://developer.alexanderklimov.ru/android/json.php
        //    JSONObject j = new JSONObject(s); // обрабатываем строку из файла
         //   Log.d("mytag", "Your city is " + j.getString("name"));
            // для вложенных объектов и массивов используйте getJSONArray, getJSONObject


      //  } catch (JSONException e) {}

   // }
//}
