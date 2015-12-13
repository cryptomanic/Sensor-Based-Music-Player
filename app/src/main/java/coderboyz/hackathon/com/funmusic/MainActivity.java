package coderboyz.hackathon.com.funmusic;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends ActionBarActivity
        implements MediaController.MediaPlayerControl, SensorEventListener {

    ArrayList<Song> songList;           //Array for Storing each song specifications
    ListView songView;                  //displaying specification of each song
    MusicService myMusicService;        //Music service
    Intent myIntent = null;             //intent for starting the service
    boolean musicBound = false;         //keep track whether activity is bound to service or not
    SensorManager manager;              //for handling work related to sensor
    Sensor proximity,accelerometer;     //type of sensor to be used
    MusicController controller;         //for accessing MusicController Class features
    int toKnow = 1;
    int check = 0;
    int checkThemePresent = 0;
    boolean paused=false;
    boolean playbackPaused=false;
    int savedSensorState=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //songView refers to listView in activity_main.xml
        songView = (ListView) findViewById(R.id.songs_list);
        //instantiate ArrayList
        songList = new ArrayList<Song>();
        //call this function to get specifications of each song
        getSongList();
        //Sorting of list by Title
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        //instantiate SongAdapter Class
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        //call to enhance features of controller
        setController();
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximity = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void getSongList() {
        //instantiating ContentResolver Class
        ContentResolver musicResolver = getContentResolver();
        //retrieving URI of external music files
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        /*instantiate Cursor using ContentResolver Instance
          for query music files*/
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //index of SongTitle Column
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            //index of PrimaryKey or id column
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            //index of SongArtist Column
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                //appending an initialized object of Song Class to songList
                songList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    //Connect Main Activity Class to Service Class
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            myMusicService = binder.getService();// to get service
            myMusicService.setList(songList);    //pass songs list
            musicBound = true;                   //activity is bound to service
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;//service disconnected
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (myIntent == null) {
            //instance of Intent Class
            myIntent = new Intent(this, MusicService.class);
            //binding the service
            bindService(myIntent, musicConnection, this.BIND_AUTO_CREATE);
            //starting the service
            startService(myIntent);
        }
    }

    public void songPicked(View v) {
        //set song using tag
        myMusicService.setSong(Integer.parseInt(v.getTag().toString()));
        //calling function of MusicService Class
        myMusicService.playSong();
        //displaying control tool permanently
        if(playbackPaused) {
            setController();
            playbackPaused = false;
        }
        if (check == 0) {
            //registering proximity sensor
            manager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            check = 1;
            controller.show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onDestroy() {
        stopService(myIntent);
        myMusicService = null;
        //unregister proximity sensor listener
        manager.unregisterListener(this);
        super.onDestroy();
    }

    private void setController() {
        //instantiate the controller
        controller = new MusicController(this);
        //for adding previous and next button on controller
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        //controller will be displayed above list at bottom
        controller.setAnchorView(findViewById(R.id.songs_list));
        controller.setEnabled(true);
    }

    private void playNext() {
        myMusicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
    }

    private void playPrev() {
        myMusicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
    }

    @Override
    public void start() {

        playbackPaused=true;
        myMusicService.go();
    }

    @Override
    public void pause() {

        playbackPaused=true;
        myMusicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (myMusicService != null && musicBound && myMusicService.isPng())
            return myMusicService.getDur();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (myMusicService != null && musicBound && myMusicService.isPng())
            return myMusicService.getPosition();
        return 0;
    }

    @Override
    public void seekTo(int pos) {

        myMusicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (myMusicService != null && musicBound)
            return myMusicService.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {

        return 0;
    }

    @Override
    public boolean canPause() {

        return true;
    }

    @Override
    public boolean canSeekBackward() {

        return true;
    }

    @Override
    public boolean canSeekForward() {

        return true;
    }

    @Override
    public int getAudioSessionId() {

        return 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_end) {
            stopService(myIntent);
            myMusicService = null;
            System.exit(0);
            return true;
        }
        if (id == R.id.action_shuffle) {
            myMusicService.setShuffle();
            return true;
        }
        if (id == R.id.developer) {
            Intent myIntent1 = new Intent(MainActivity.this, Developers.class);
            myIntent1.putExtra("fromIntent1", checkThemePresent);
            startActivity(myIntent1);
            return true;
        }
        if (id == R.id.rateus) {
            Intent myIntent2 = new Intent(MainActivity.this, RateUs.class);
            myIntent2.putExtra("fromIntent2", checkThemePresent);
            startActivity(myIntent2);
            return true;
        }
        if (id == R.id.chngbacg) {
            LinearLayout myLL1 = (LinearLayout) findViewById(R.id.my_layout1);
            if (checkThemePresent == 0) {
                myLL1.setBackgroundResource(R.drawable.skin_102);
                checkThemePresent++;
            } else {
                myLL1.setBackgroundResource(R.drawable.skin_101);
                checkThemePresent--;
            }
            return true;
        }
        if(id==R.id.sensorState){
            if(savedSensorState==1){
                manager.unregisterListener(this);
                item.setTitle("Enable Sensor");
                savedSensorState=0;
            }
            else{
                manager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
                manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                item.setTitle("Disable Sensor");
                savedSensorState=1;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_PROXIMITY) {
            toKnow++;
            if (toKnow == 1) {
                if (isPlaying()) {
                    playbackPaused = true;
                    myMusicService.pausePlayer();
                } else {
                    playbackPaused = false;
                    myMusicService.go();
                }

            } else {
                toKnow = 0;
            }
        }
        else{
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if(Math.sqrt(Math.pow(x,2.0)+Math.pow(y,2.0)+Math.pow(z,2.0))>15 && x>4){
                playNext();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
