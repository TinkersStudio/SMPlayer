package project.tnguy190.calpoly.edu.smplayer;

/**
 * Created by thuy on 11/21/16.
 */

public class Song {
    private long id;
    private String title;
    private String artist;
    private Long albumID;

    public Song(long songID, String songTitle, String songArtist, Long albumID) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        this.albumID = albumID;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public Long getAlbumArt(){return albumID;}

}
