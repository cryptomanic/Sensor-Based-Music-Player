/*Instances of this Class represents specification of each Song*/
package coderboyz.hackathon.com.funmusic;

public class Song {
    long id;//SongId
    String title, artist;//SongTitle,SongArtist

    //Constructor to initialize above variables
    public Song(long songId, String songTitle, String songArtist) {
        id = songId;
        title = songTitle;
        artist = songArtist;
    }

    //this function will return SongId
    public long getId() {

        return id;
    }

    //this function will return SongTitle
    public String getTitle() {

        return title;
    }

    //this function will return SongArtist
    public String getArtist() {

        return artist;
    }

}
