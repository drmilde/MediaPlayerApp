package com.example.milde.mediaplayerapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by milde on 18.12.17.
 */

public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    // Binder
    private IBinder iBinder = new LocalBinder();
    // MediaPlayer
    private MediaPlayer mediaPlayer;
    // path to audio file
    private String mediaFile;
    // current seek position
    private int resumePosition;

    // AudioManager
    private AudioManager audioManager;


    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();

        // TODO was muss hier anstelle von setAudioStrem verwendet werden ?
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        try {
            mediaPlayer.setDataSource(mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        mediaPlayer.prepareAsync();
        ;
    }


    // player control

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    // Service

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // OnCompletionListener
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //Invoked when playback of a media source has completed.
        stopMedia();

        //TODO was genau macht stopSelf() ?
        stopSelf();
    }


    // OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    // OnErrorListener
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: {
                log("Media Error", "not valid for progressive playback");
                break;
            }
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED: {
                log("Media Error", "the poor server died");
                break;
            }
            case MediaPlayer.MEDIA_ERROR_UNKNOWN: {
                log("Media Error", "no idea, what went wrong here");
                break;
            }


        }


        return false;
    }

    private void log(String type, String message) {
        Log.d(type, message);
    }


    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    // OnSeekCompletionListener
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        //Invoked indicating the completion of a seek operation.
    }

    // OnInfoListener
    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    // OnBufferingIpdateListener
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    // OnAudioFocusChanged
    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.

        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mediaPlayer == null)
                    initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();

                mediaPlayer.setVolume(0.7f, 0.7f);
                break;
            }

            case AudioManager.AUDIOFOCUS_LOSS: {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            }

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            }

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
            }
        }
    }


    // weitere Methoden

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // TODO welche Methode muss hier verwendet werden ?
        int result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }

        // no focus gain
        return false;
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    // life cycle methods


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("Jan", "onStartCommand");

        // check for null pointer exception !!
        try {
            mediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (!requestAudioFocus()) {
            stopSelf();
        }


        if ((mediaFile != null) && (!mediaFile.equalsIgnoreCase(""))) {
            initMediaPlayer();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }

        removeAudioFocus();
    }

    private boolean removeAudioFocus() {
        // TODO welche Methode muss hier verwendet werden
        return (
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
        );
    }
}
