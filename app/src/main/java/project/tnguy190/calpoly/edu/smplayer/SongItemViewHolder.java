package project.tnguy190.calpoly.edu.smplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by thuy on 11/21/16.
 */

public class SongItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "SongItemViewHolder";
    private TextView titleTV;
//    private TextView artistTV;
    private int position;
    public static Song song;
    private LinearLayout entry;
    private final Context context;

    private MusicService musicService;

    public SongItemViewHolder(View itemView) {
        super(itemView);

        musicService = new MusicService();

        context = itemView.getContext();

        itemView.setOnClickListener(this);
        titleTV = (TextView) itemView.findViewById(R.id.song_title);
//        artistTV = (TextView) itemView.findViewById(R.id.song_artist);

        entry = (LinearLayout) itemView;
    }

    public void bind(Song song, int pos) {
        this.song = song;

        this.position = pos;
        titleTV.setText(song.getTitle() + " by " + song.getArtist());
//        artistTV.setText(song.getArtist());
    }

    @Override
    public void onClick(View view) {
        // Log.d(TAG, "create intent to play song with id " + song.getID());

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("play", SongAdapter.songList().get(getAdapterPosition()).getID());
        intent.putExtra("class", "picked");

        MainActivity.flag = 1;
        if(!AllSongsActivity.playlistChosen) {

            musicService.state = -1;

        }
        else {
            musicService.state = musicService.playlistNum;
        }
        context.startActivity(intent);
    }
}
