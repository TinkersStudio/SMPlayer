package project.tnguy190.calpoly.edu.smplayer;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by thuy on 11/21/16.
 */

public class Song {
    private Long id;
    private String title;
    private String artist;
    private Long albumID;

    public Song(long songID, String songTitle, String songArtist, long albumID) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        this.albumID = albumID;
    }

    public Long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public Long getAlbumArt(){return albumID;}

    @Override
    public boolean equals(Object obj) {
        Song other = (Song) obj;

        if (!id.equals(other.getID())) {
            Log.d(TAG, "ids: " + id + " = " + other.getID());
            return false;
        }
        if (!other.getTitle().equals(title)) {
            Log.d(TAG, title + " = " + other.getTitle());
            return false;
        }
        if (!other.getArtist().equals(artist)) {
            Log.d(TAG, artist + " = " + other.getArtist());
            return false;
        }
        if (!other.getAlbumArt().equals(albumID)) {
            Log.d(TAG, "albumIds: " + albumID + " = " + other.getAlbumArt());
            return false;
        }

        return true;
    }

}
