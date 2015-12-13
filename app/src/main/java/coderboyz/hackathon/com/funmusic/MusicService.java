package coderboyz.hackathon.com.funmusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    //Media Player
    MediaPlayer player;
    //list of songs
    ArrayList<Song> songs;
    //current song position
    int songPosition;
    //instance of MusicBinder Class
    private final IBinder musicBind = new MusicBinder();
    //current song title
    private String songTitle;
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;

    @Override
    public IBinder onBind(Intent intent) {

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();            //service is created
        songPosition = 0;            //current song position
        player = new MediaPlayer();  //instance of media player class
        initMusicPlayer();           //calling this function to set media player properties
        rand = new Random();           //instance of Random class
    }

    public void initMusicPlayer() {
        //WakeLock will not allow the screen to sleep
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //stream type is set to music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //when song will complete
        player.setOnCompletionListener(this);
        //when error occurs
        player.setOnErrorListener(this);
        //when mediaPlayer instance created
        player.setOnPreparedListener(this);
    }

    public void setList(ArrayList<Song> mySongs) {
        songs = mySongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {
        //resetting media player
        player.reset();
        //instance of current song
        Song playSong = songs.get(songPosition);
        //current song id
        long currSong = playSong.getId();
        //setting uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        songTitle = playSong.getTitle();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();//start playback
        //instance of Intent Class
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //builder object for creating notification
        Notification.Builder builder = new Notification.Builder(this);
        //set content of notification
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_action_play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentText(songTitle)
                .setContentTitle("Playing");
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    public void setSong(int songIndex) {

        songPosition = songIndex;
    }

    public int getPosition() {

        return player.getCurrentPosition();
    }

    public int getDur() {

        return player.getDuration();
    }

    public boolean isPng() {

        return player.isPlaying();
    }

    public void pausePlayer() {

        player.pause();
    }

    public void seek(int pos) {

        player.seekTo(pos);
    }

    public void go() {

        player.start();
    }

    /*after calling this function next button on
      controller take us to any random song*/
    public void setShuffle() {
        if (shuffle)
            shuffle = false;
        else
            shuffle = true;
    }

    //playing previous song
    public void playPrev() {
        songPosition--;
        if (songPosition <= 0)
            songPosition = songs.size() - 1;
        playSong();
    }

    //playing next song
    public void playNext() {
        if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = rand.nextInt(songs.size());
            }
            songPosition = newSong;
        } else {
            songPosition++;
            if (songPosition >= songs.size())
                songPosition = 0;
        }
        playSong();

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() >= 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }


}
