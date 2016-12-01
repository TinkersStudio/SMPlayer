package project.tnguy190.calpoly.edu.smplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by thuy on 11/27/16.
 */

public class PlaylistItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "PlaylistItemViewHolder";
    private static final int CREATE_PLAYLIST = 10;

    private TextView titleTV;
    public static Playlist playlist;
    private LinearLayout entry;
    private final Context context;

    public PlaylistItemViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();

        itemView.setOnClickListener(this);
        titleTV = (TextView) itemView.findViewById(R.id.playlist);

        entry = (LinearLayout) itemView;
    }

    public void bind(Playlist pl) {
        this.playlist = pl;

        titleTV.setText(playlist.getTitle());
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "view the playlist");

        Intent intent = new Intent(context, CreatePlaylistActivity.class);
        ((Activity)context).startActivityForResult(intent, CREATE_PLAYLIST);
    }
}
