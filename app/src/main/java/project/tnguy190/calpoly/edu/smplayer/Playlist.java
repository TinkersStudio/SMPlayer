package project.tnguy190.calpoly.edu.smplayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by thuy on 11/27/16.
 */

public class Playlist implements Parcelable {
    private int id;
    private String title;
    private ArrayList<Song> songs;

    public Playlist(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public void add(Song song) { songs.add(song); }
    public void delete(Song song) { songs.remove(getPosition(song)); }

    public int getPosition(Song song) { return songs.indexOf(song); }
    public int getID() { return this.id; }
    public String getTitle() { return this.title; }
    public Song getSong(int i) { return songs.get(i); }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
