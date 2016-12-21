package project.tnguy190.calpoly.edu.smplayer;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by thuy on 11/27/16.
 */

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongViewHolder> {
    private ArrayList<Song> songList;
    private ArrayList<Song> alreadyInPlaylist;

    public PlaylistSongAdapter(ArrayList<Song> songList, ArrayList<Song> alreadyInPlaylist) {
        this.songList = songList;
        this.alreadyInPlaylist = alreadyInPlaylist;
    }

    @Override
    public PlaylistSongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlaylistSongViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_create_view, parent, false));
    }

    @Override
    public void onBindViewHolder(PlaylistSongViewHolder holder, int position) {
        holder.bind(songList.get(position), alreadyInPlaylist.contains(songList.get(position)));
    }

    @Override
    public long getItemId(int i) { return songList.get(i).getID(); }

    @Override
    public int getItemCount() { return songList.size(); }
}