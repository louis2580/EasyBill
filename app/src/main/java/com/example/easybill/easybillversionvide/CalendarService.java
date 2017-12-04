package com.example.easybill.easybillversionvide;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static com.example.easybill.easybillversionvide.MainActivity.NOMSEVENTS;

/**
 * Created by Apero on 03/12/2017.
 */


public class CalendarService extends IntentService {

    private static final String TAG = "MyService";

    private ArrayList<String> eventTitre = new ArrayList<String>();
    private ArrayList<Long> eventStartTime = new ArrayList<Long>();
    private ArrayList<Long> eventEndTime = new ArrayList<Long>();
    private long timer;

    public CalendarService() {
        super("CalendarService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service called");
        timer = 10;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("Service ", " >>> Started");
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        // Gets data from the incoming Intent
        getDataFromCalendarTable();
        clear();

        tempo();
    }

    protected void tempo() {
        try {
            TimeUnit.SECONDS.sleep(timer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onHandleIntent(new Intent());
    }

    public void sendNotification(String nomEvent) {
        //Start activity add_folder

        Intent addFolder = new Intent(CalendarService.this, add_folder.class);
        addFolder.putExtra("Titre", nomEvent);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, addFolder, 0);



        NotificationCompat.Builder notif = new NotificationCompat.Builder(this.getApplicationContext(), "channel01");
        notif.setContentText(nomEvent + " commence bientôt : Créer un dossier pour cet évènement?");
        notif.setContentTitle(nomEvent + " approche !");
        notif.setContentIntent(pendingIntent);
        notif.setSmallIcon(R.drawable.ic_launcher_foreground);
        notif.setAutoCancel(true);
        //notif.setWhen(millis);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(001, notif.build());
    }

    public void getDataFromCalendarTable() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Cursor cur = null;
            ContentResolver cr = getContentResolver();
            String[] mProjection = new String[]{CalendarContract.Events.CALENDAR_ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.ALL_DAY,
                    CalendarContract.Events.EVENT_LOCATION};

            Calendar startTime = Calendar.getInstance();
            long now = new Date().getTime();
            long sevenDaysInMilli = 7*24*3600*1000;
            startTime.setTimeInMillis(now);

            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(now + sevenDaysInMilli);

            // the range is all data from 2014

            String selection = "(( " + CalendarContract.Events.DTSTART + " >= "
                    + startTime.getTimeInMillis() + " ) AND ( "
                    + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            } else {
                Cursor cursor = this.getBaseContext().getContentResolver()
                        .query(CalendarContract.Events.CONTENT_URI, mProjection, selection,
                                null, null);

                if (cursor.moveToFirst()) {
                    do {
                        // Add the event tittle
                        Log.d(TAG, "Title: " + cursor.getString(1));
                        eventTitre.add(cursor.getString(1));
                        // Add the event begin time
                        Log.d(TAG, " Start-Time: " + (new Date(cursor.getLong(3))).toString());
                        eventStartTime.add((new Date(cursor.getLong(3))).getTime());
                        // Add the event end time
                        Log.d(TAG, " End-Time: " + (new Date(cursor.getLong(4))).toString());
                        eventEndTime.add((new Date(cursor.getLong(4))).getTime());

                    } while ( cursor.moveToNext());
                }
            }

            Log.d(TAG, " Nouvel event");
            for (int i = 0; i < eventTitre.size(); i++){
                // new event to add

                long st = eventStartTime.get(i);
                long et = eventEndTime.get(i);
                long dureeEvent = et - st;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                // if the events lasts more than a day or if it begins one day and ends the next day
                if (dureeEvent > 12*3600*1000 && !NOMSEVENTS.contains(eventTitre.get(i)))
                {
                    sendNotification(eventTitre.get(i));
                    NOMSEVENTS.add(eventTitre.get(i));
                }
            }
        }
        else{
            Toast.makeText(this, "Not Connected", Toast.LENGTH_LONG).show();
        }

    }

    public void clear(){
        eventTitre.clear();
        eventEndTime.clear();
        eventStartTime.clear();
        Log.d(TAG, "clear");
    }
}
