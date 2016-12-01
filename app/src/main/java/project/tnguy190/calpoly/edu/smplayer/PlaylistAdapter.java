package project.tnguy190.calpoly.edu.smplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by thuy on 11/27/16.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistItemViewHolder> {
    private ArrayList<Playlist> plList;

    public PlaylistAdapter(ArrayList<Playlist> plList) { this.plList = plList; }

    @Override
    public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlaylistItemViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_single_view, parent, false));
    }

    @Override
    public void onBindViewHolder(PlaylistItemViewHolder holder, int position) {
        holder.bind(plList.get(position));
    }

    @Override
    public long getItemId(int i) { return plList.get(i).getID(); }

    @Override
    public int getItemCount() { return plList.size(); }
}
