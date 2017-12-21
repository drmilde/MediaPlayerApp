package com.example.milde.mediaplayerapp;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.milde.mediaplayerapp.service.Audio;
import com.example.milde.mediaplayerapp.service.MediaPlayerService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MediaPlayerService player;
    private boolean serviceBound = false;

    // Buttons
    private Button btnPlay;
    private Button btnStop;
    private Button btnPause;
    private Button btnLoad;

    // local audio objects
    private ArrayList<Audio> audioList;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            log("Media Service: bound succesful!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = (Button) findViewById(R.id.btnStart);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
            }
        });


        loadAudio();
        playAudio(audioList.get(1).getData());


    }


    private void log(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    private void playAudio(String media) {
        if (!serviceBound) {

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);

            startService(playerIntent);

            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            log(playerIntent.toString());

        } else {
            // Service ist bereits aktic
            // Medien Daten über Broadcast Receiver schicken

            // TODO wie geht das ?
        }
    }

    // Zustandsspeicherung

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection); // also sets serviceBound to false
            player.stopSelf();
        }
    }


    // ContentResolver zum Laden der lokalen Daten

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";


        Cursor cursor = contentResolver.query(
                uri,
                null,
                selection,
                null,
                sortOrder
        );


        log(cursor.getCount()+"");

        if ((cursor != null) && (cursor.getCount() > 0)) {
            audioList = new ArrayList<Audio>();

            while (cursor.moveToNext()) {
                // TODO hier weiter machen
                String data = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST));


                audioList.add(new Audio(data, title, album, artist));
            }
            cursor.close();
        }
        /*
        */
    }
}
