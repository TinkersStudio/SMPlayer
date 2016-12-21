package project.tnguy190.calpoly.edu.smplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;

/**
 * Created by thuy on 11/27/16.
 */

public class PlaylistItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "PlaylistItemViewHolder";
    private static final int CREATE_PLAYLIST = 10;

    private TextView titleTV;
    public static Playlist playlist;
    private ImageView image;
    private LinearLayout entry;
    private final Context context;
    private final Activity activity;

    public PlaylistItemViewHolder(final Activity activity, final View itemView) {
        super(itemView);

        context = itemView.getContext();
        itemView.setOnClickListener(this);
        titleTV = (TextView) itemView.findViewById(R.id.playlist);
        this.activity = activity;
        entry = (LinearLayout) itemView;

        // long click behavior
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popup = new PopupMenu(activity, itemView);
                popup.getMenuInflater().inflate(R.menu.playlist_long_click_popup, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Log.d(TAG, "item id = " + item.getTitle());

                        switch (item.getItemId()) {
                            case R.id.popup_add_to_playlist:
                                addToPlaylistIntent();
                                break;
                            case R.id.popup_delete_playlist:
                                // delete from internal storage
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setCancelable(false);
                                builder.setMessage(R.string.delete_confirmation)
                                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                File file = new File(activity.getFilesDir(), playlist.getTitle() + ".json");
                                                file.delete();
                                                PlaylistsActivity.deleteFromPlaylists(playlist);
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {}
                                        });

                                builder.show();

                                break;
                            default:
                                break;
                        }

                        return true;
                    }
                });

                popup.show();

                return true;
            }
        });
    }

    public void bind(Playlist pl) {
        this.playlist = pl;

        titleTV.setText(playlist.getTitle());
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "view the playlist");

        if ( playlist.size() == 0 ) {
            addToPlaylistIntent();
        }
        else {
            Intent intent = new Intent(activity.getApplicationContext(), AllSongsActivity.class);
            intent.putExtra(PlaylistsActivity.EXTRA_PLAYLIST_TOGGLE, PlaylistsActivity.PLAYLIST_TOGGLE);
            intent.putExtra(PlaylistsActivity.EXTRA_PLAYLIST_TITLE, String.valueOf(playlist.getTitle()));
            intent.putExtra(PlaylistsActivity.EXTRA_PLAYLIST_ID, getAdapterPosition());
            activity.startActivity(intent);
        }
    }

    public void addToPlaylistIntent() {
        Intent intent = new Intent(activity.getApplicationContext(), CreatePlaylistActivity.class);
        intent.putExtra(PlaylistsActivity.EXTRA_PLAYLIST_ID, getAdapterPosition());
        intent.putExtra(PlaylistsActivity.EXTRA_PLAYLIST_TITLE, String.valueOf(playlist.getTitle()));
        activity.startActivityForResult(intent, CREATE_PLAYLIST);
    }
}
