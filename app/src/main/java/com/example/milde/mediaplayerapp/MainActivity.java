package com.example.milde.mediaplayerapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.milde.mediaplayerapp.service.MediaPlayerService;

public class MainActivity extends AppCompatActivity {

    private MediaPlayerService player;
    private boolean serviceBound = false;

    // Buttons

    private Button btnPlay;
    private Button btnStop;
    private Button btnPause;
    private Button btnLoad;

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

        btnPlay = (Button)findViewById(R.id.btnStart);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
            }
        });


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

}
