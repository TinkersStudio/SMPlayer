package project.tnguy190.calpoly.edu.smplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by thuy on 11/27/16.
 */

public class PlaylistSongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "PlaylistSongViewHolder";
    private TextView titleTV;
//    private TextView artistTV;
    private long songID;
    private CheckBox cb;
    public static Song song;
    private LinearLayout entry;
    private final Context context;

    private MusicService musicService;

    public PlaylistSongViewHolder(View itemView) {
        super(itemView);

        musicService = new MusicService();

        context = itemView.getContext();

        itemView.setOnClickListener(this);
        titleTV = (TextView) itemView.findViewById(R.id.song_title);
//        artistTV = (TextView) itemView.findViewById(R.id.song_artist);
        cb = (CheckBox) itemView.findViewById(R.id.create_playlist_check);

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.setChecked(b);

                if (b) {
                    CreatePlaylistActivity.addToPlaylist(getAdapterPosition());
                }
                else {
                    CreatePlaylistActivity.deleteFromPlaylist(getAdapterPosition());
                }
            }
        });

        entry = (LinearLayout) itemView;
    }

    public void bind(Song song, boolean alreadyInPlaylist) {
        this.song = song;

        titleTV.setText(song.getTitle() + " by " + song.getArtist());
//        artistTV.setText(song.getArtist());
        songID = song.getID();

        if (alreadyInPlaylist)
            cb.setChecked(true);
    }

    @Override
    public void onClick(View view) {
        cb.setChecked(!cb.isChecked());

        // notify that song is on playlist or not
        if (cb.isChecked()) {
            CreatePlaylistActivity.addToPlaylist(getAdapterPosition());
        }
        else {
            CreatePlaylistActivity.deleteFromPlaylist(getAdapterPosition());
        }
    }
}