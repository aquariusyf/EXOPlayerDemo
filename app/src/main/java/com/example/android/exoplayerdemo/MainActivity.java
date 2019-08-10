package com.example.android.exoplayerdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_id_100";
    private static final String CHANNEL_NAME = "channel_media_style";
    private static final int NOTIFICATION_ID = 101;
    private static final String MEDIA_URL_1 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2Ftingjianxiayudeshengyin.mp3?alt=media&token=4b0bcb50-591e-4d96-b729-7d6215ad0f64";
    private static final String MEDIA_URL_2 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2FPersona%205%20OST%20-%20Rivers%20In%20the%20Desert.mp3?alt=media&token=e2d3e7bd-d47e-45a3-a842-e06578756968";
    private static final String MEDIA_URL_3 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2FProof%20Of%20A%20Hero.mp3?alt=media&token=d1cde7a6-d3e1-4ee6-8218-9eb49ca810e3";
    private static final String MEDIA_URL_4 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2Fxuemaojiao.mp3?alt=media&token=e0191f19-6fbf-4f3c-97fe-8bfbb069a0b9";
    private static final String MEDIA_URL_5 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2FYoung%20For%20You%20-%20GALA.mp3?alt=media&token=f148ae10-1a2d-405a-8d42-200e9a0aa56e";
    private static final String MEDIA_URL_6 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2F%E5%91%A8%E6%9D%B0%E4%BC%A6%2B-%2B%E6%97%B6%E5%85%89%E6%9C%BA.mp3?alt=media&token=33773e8d-b5c2-4204-bc9e-173cabb58e59";
    private static final String MEDIA_URL_7 = "https://firebasestorage.googleapis.com/v0/b/fir-demo-ac3eb.appspot.com/o/music%2F%E5%91%A8%E6%9D%B0%E4%BC%A6%2B-%2B%E7%AD%89%E4%BD%A0%E4%B8%8B%E8%AF%BE%2B(with%2B%E6%9D%A8%E7%91%9E%E4%BB%A3).mp3?alt=media&token=7975a2e5-305b-4971-884a-0548f4fcb547";

    private PlayerView mPlayerView; // The exo player view
    private PlayerControlView mPlayerControlView; // The exo player control view for audio source

    /**
     * These exo views can be override by creating a custom layout file and assign it to the view
     *  app:controller_layout_id="@layout/custom_controls"
     *  The id of individual views in custom layout has to be defined as follow
     *  exo_play - The play button
     *  exo_pause - The pause button
     *  exo_ffwd - The fast forward button
     *  exo_rew - The rewind button
     *  exo_prev - The previous track button
     *  exo_next - The next track button
     *  exo_repeat_toggle - The repeat toggle button
     *  exo_shuffle - The shuffle button
     *  exo_vr - The VR mode button
     *  exo_position - Text view displaying the current playback position
     *  exo_duration - Text view displaying the current media duration
     *  exo_progress - Time bar that's updated during playback and allows seeking
     *  */

    private SimpleExoPlayer mPlayer; // The exo player object
    private MediaSource mMediaSource; // The media source object for single media
    private ConcatenatingMediaSource mMediaSourceList; // The media source object for playlist
    private List<MediaSource> mPlaylist = new ArrayList<>();

    private static MediaSessionCompat mMediaSession; // The media session object
    private PlaybackStateCompat.Builder mStateBuilder; // The builder of playback state

    private NotificationManager mNotificationManager; // The notification manager object
    private NotificationChannel mNotificationChannel; // The notification channel object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMediaSession();
        initNotificationManagerAndChannel();
        createExoPlayer();
        bindPlayerView();
        prepareExoPlayerWithPlaylist();
        mPlayer.setPlayWhenReady(true);
    }

    /** Release the player and media session objects */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mMediaSession.setActive(false);
    }

    /** Create a new exo player object */
    private void createExoPlayer() {
        mPlayer = ExoPlayerFactory.newSimpleInstance(this);
        addEventListener(); // Add event listener
    }

    private void initViews() {
        mPlayerView = findViewById(R.id.player_view);
        mPlayerControlView = findViewById(R.id.player_control_view);
    }

    /** Bind the exo player to the player view or control view */
    private void bindPlayerView() {
        mPlayerView.setPlayer(mPlayer);
        mPlayerControlView.setPlayer(mPlayer);
        mPlayerControlView.setShowTimeoutMs(0); // The control view will stay without dismiss timer
    }

    /** Create media source with a single media and prepare exo player */
    private void prepareExoPlayerWithSingleMedia() {
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_1));
        mPlayer.prepare(mMediaSource);
        Log.v(LOG_TAG, "Single Media ready");
    }

    /** Create playlist and prepare exo player */
    private void prepareExoPlayerWithPlaylist() {
        mMediaSourceList = new ConcatenatingMediaSource();
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_1));
        mPlaylist.add(mMediaSource);
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_2));
        mPlaylist.add(mMediaSource);
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_3));
        mPlaylist.add(mMediaSource);
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_4));
        mPlaylist.add(mMediaSource);
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_5));
        mPlaylist.add(mMediaSource);
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_6));
        mPlaylist.add(mMediaSource);
        mMediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EXOPlayerDemo")))
                .createMediaSource(Uri.parse(MEDIA_URL_7));
        mPlaylist.add(mMediaSource);
        for(MediaSource m: mPlaylist) {
            mMediaSourceList.addMediaSource(m);
        }
        mPlayer.prepare(mMediaSourceList);
        Log.v(LOG_TAG, "Media list ready");
    }

    /** Release the player */
    private void releasePlayer() {
        mPlayer.stop();
        mPlayer.release();
        Log.v(LOG_TAG, "Player is released!");
    }

    /** Add event listener to player object */
    private void addEventListener() {
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playWhenReady && playbackState == Player.STATE_READY) {
                    Log.v(LOG_TAG, "Media is ready and playing!");
                    mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            mPlayer.getCurrentPosition(), 1f); // Set playback state to playing
                } else if(playbackState == Player.STATE_READY) {
                    Log.v(LOG_TAG, "Media is ready not playing!");
                    mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            mPlayer.getCurrentPosition(), 1f); // Set playback state to pause
                } else if(playbackState == Player.STATE_BUFFERING) {
                    Log.v(LOG_TAG, "Media is buffering");
                    Toast.makeText(MainActivity.this, "Buffering", Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(LOG_TAG, "Playback state is: " + playbackState);
                }
                mMediaSession.setPlaybackState(mStateBuilder.build()); // Set current transport state
                showNotification(); // Show the notification on state change
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }
        });
    }

    /** Initiate notification manager and channel */
    private void initNotificationManagerAndChannel() {
        mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationChannel.enableVibration(false);
            mNotificationChannel.setVibrationPattern(new long[]{ 0 });
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
    }

    /** Create a media style notification */
    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID); // Create notification builder

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0); // Create pending intent to main activity

        // Initialize play pause icon according to transport state
        int icon;
        String actionTitle;
        if(mPlayer.getPlayWhenReady()) {
            icon = R.drawable.exo_notification_pause;
            actionTitle = getString(R.string.exo_controls_pause_description);
        } else {
            icon = R.drawable.exo_notification_play;
            actionTitle = getString(R.string.exo_controls_play_description);
        }

        builder.setContentTitle("Title") // Set title
                .setContentText("Subtitle") // Set sub title
                .setSubText("This is description") // Set description text
                .setContentIntent(contentPendingIntent) // Click notification launch activity
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible on lock screen
                .setOngoing(true) // Always show notification
                .setSmallIcon(R.drawable.exo_notification_small_icon) // Set small icon
                .addAction(new NotificationCompat.Action(R.drawable.exo_controls_previous,
                        getString(R.string.exo_controls_previous_description),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))) // Add previous button
                .addAction(new NotificationCompat.Action(icon, actionTitle,
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE))) // Add play pause button
                .addAction(new NotificationCompat.Action(R.drawable.exo_controls_next,
                        getString(R.string.exo_controls_next_description),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken()) // Add next button
                        .setShowActionsInCompactView(0)); // Set notification style to media style

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /** Initialize the media session */
    private void initMediaSession() {
        mMediaSession = new MediaSessionCompat(this, LOG_TAG); // Create media session instance

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS); // Enable callbacks for external button and transport control

        mMediaSession.setMediaButtonReceiver(null); // Do not let external buttons restart player when the app is invisible

        mStateBuilder = new PlaybackStateCompat.Builder() // Set the available actions
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        mMediaSession.setCallback(new MyMediaSessionCallback()); // Set the media session callback

        mMediaSession.setActive(true); // Start the media session
    }

    /** Media session callbacks where external client controls the player */
    private class MyMediaSessionCallback extends MediaSessionCompat.Callback {

        /** Override the actions which are intent to be performed by external clients(Bluetooth etc.) */
        @Override
        public void onPlay() {
            mPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToNext() {
            mPlayer.seekTo(mPlayer.getNextWindowIndex(), 0);
        }

        @Override
        public void onSkipToPrevious() {
            if(mPlayer.getCurrentPosition() != 0) {
                mPlayer.seekTo(0);
            } else {
                mPlayer.seekTo(mPlayer.getPreviousWindowIndex(), 0);
            }
        }
    }

    /** Create broadcast receiver to handle button click on media style notification*/
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver(){} // 0 argument constructor is required

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }
}
