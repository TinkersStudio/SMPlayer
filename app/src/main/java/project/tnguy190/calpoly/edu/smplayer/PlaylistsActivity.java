package project.tnguy190.calpoly.edu.smplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by thuy on 11/22/16.
 */

public class PlaylistsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "PlaylistsActivity";
    private static final int CREATE_PLAYLIST = 10;
    private static final String KEY = "PlaylistList";
    protected static final String EXTRA_PLAYLIST_ID = "PlaylistID";
    protected static final String EXTRA_PLAYLIST_TITLE = "PlaylistTitle";
    protected static final String EXTRA_PLAYLIST_TOGGLE = "ToggleAllSongsToShowPlaylist";
    protected static final boolean PLAYLIST_TOGGLE = true;

    protected static ArrayList<Playlist> plList;
    private RecyclerView listRV;
    private static PlaylistAdapter adapter;

    protected static int idCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listRV = (RecyclerView) findViewById(R.id.fragment_playlist_list);

        getPlaylists();

        adapter = new PlaylistAdapter(this, plList);
        listRV.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create_playlist) {
            // create a dialog asking for playlist name and add to playlist view
            final EditText input = new EditText(PlaylistsActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistsActivity.this);
            builder.setCancelable(false);
            builder.setView(input); // uncomment this line

            builder.setMessage(R.string.make_new_playlist)
                    .setPositiveButton(R.string.create_playlist, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (!String.valueOf(input.getText()).equals("")) {
                                Log.d(TAG, "create playlist named " +  input.getText());
                                Playlist pl = new Playlist(idCount++, String.valueOf(input.getText()));
                                Log.d(TAG, "list size = " + plList.size());
                                plList.add(pl);
                                adapter.notifyDataSetChanged();

                                Utilities.writeToJSonFile(PlaylistsActivity.this, pl);

                                Toast.makeText(getApplicationContext(), "Empty playlist created", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.search_dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            builder.show();
        } else if (id == R.id.action_search) {
            final EditText input = new EditText(PlaylistsActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistsActivity.this);
            builder.setCancelable(false);
            builder.setView(input); // uncomment this line

            builder.setMessage(R.string.search)
                    .setPositiveButton(R.string.search_dialog_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "search" + input.getText());
                            Intent intent = new Intent(getApplicationContext(), AllSongsActivity.class);
                            intent.putExtra("search", String.valueOf(input.getText()));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.search_dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d("MainActivity", "item clicked");

        if (id == R.id.nav_all_songs) {
            Log.d(TAG, "open all songs");
            Intent intent = new Intent(getApplicationContext(), AllSongsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_player) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == CREATE_PLAYLIST) {
            long ids[] = data.getLongArrayExtra(CreatePlaylistActivity.EXTRA_SONG_IDS);
            String artists[] = data.getStringArrayExtra(CreatePlaylistActivity.EXTRA_SONG_ARTISTS);
            String titles[] = data.getStringArrayExtra(CreatePlaylistActivity.EXTRA_SONG_TITLES);
            int playlistID = data.getIntExtra(EXTRA_PLAYLIST_ID, -1);
            long albumIDs[] = data.getLongArrayExtra(CreatePlaylistActivity.EXTRA_SONG_ALBUM_IDS);

            plList.get(playlistID).clear();

            for (int i = 0; i < ids.length; i++)
                plList.get(playlistID).add(new Song(ids[i], titles[i], artists[i], albumIDs[i]));

            adapter.notifyItemChanged(playlistID);
        }
    }

    public static void deleteFromPlaylists(Playlist pl) {
        plList.remove(pl);
        adapter.notifyDataSetChanged();
    }

    public void getPlaylists() {
        String filesArr[] = fileList();
        String files = Arrays.toString(fileList());
        plList = new ArrayList<Playlist>();

        if (files.contains(".json")) {
            for (int i = 0; i < filesArr.length; i++) {
                if (filesArr[i].contains(".json")) {
                    plList.add(Utilities.deserializeJson(this, filesArr[i]));
                }
            }
        }
    }
}
