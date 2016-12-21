package project.tnguy190.calpoly.edu.smplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by thuy on 11/21/16.
 */

public class SongAdapter extends RecyclerView.Adapter<SongItemViewHolder> {
    private static ArrayList<Song> songList;

    public SongAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    @Override
    public SongItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SongItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.song_entry, parent, false));
    }

    public static ArrayList<Song> songList() {
        return songList;
    }

    @Override
    public void onBindViewHolder(SongItemViewHolder holder, int position) {
        holder.bind(songList.get(position), position);
    }

    @Override
    public long getItemId(int i) { return songList.get(i).getID(); }

    @Override
    public int getItemCount() { return songList.size(); }
}
