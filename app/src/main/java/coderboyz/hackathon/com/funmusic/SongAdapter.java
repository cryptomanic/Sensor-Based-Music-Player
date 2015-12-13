package coderboyz.hackathon.com.funmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {

    ArrayList<Song> songs;   //Storing SongList
    LayoutInflater songInf;  //for mapping SongArtist and SongTitle to textViews of song.xml

    //Constructor to initialize data members
    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    //function to return size of list
    @Override
    public int getCount() {

        return songs.size();
    }

    @Override
    public Object getItem(int position) {

        return null;
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
        //reference of artist view in song.xml
        TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
        //reference of title view in song.xml
        TextView titleView = (TextView) songLay.findViewById(R.id.song_title);

        Song currSong = songs.get(position);

        artistView.setText(currSong.getArtist());

        titleView.setText(currSong.getTitle());

        songLay.setTag(position);

        return songLay;
    }
}
