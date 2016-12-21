package project.tnguy190.calpoly.edu.smplayer;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by thuy on 11/21/16.
 */

public class AllSongsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "AllSongsActivity";

    private static MediaPlayer player;

    private static ArrayList<Song> songList;
    private RecyclerView listRV;
    private EditText search;
    private SongAdapter adapter;
    private String info;
    static ArrayList<Song> List;

    static boolean playlistChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_songs);

        player = new MediaPlayer();

        initMusicPlayer();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listRV = (RecyclerView) findViewById(R.id.fragment_song_list);
        songList = new ArrayList<Song>();

        getSongList();

        adapter = new SongAdapter(songList);
        listRV.setAdapter(adapter);

        search = (EditText) findViewById(R.id.search_bar);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Intent mIntent = getIntent();
        info = mIntent.getStringExtra("search");
        if(info != null) {
            Log.i(TAG, info);
            search.setText(info);
        }
    }

    public void getSongList() {
        Intent intent = getIntent();

        boolean isPlaylist = intent.getBooleanExtra(PlaylistsActivity.EXTRA_PLAYLIST_TOGGLE, false);
        playlistChosen = false;
        if (isPlaylist) {
            Log.d(TAG, "should display playlist songs");
            int playlistID = intent.getIntExtra(PlaylistsActivity.EXTRA_PLAYLIST_ID, -1);
            MusicService.playlistNum = playlistID;
            songList = PlaylistsActivity.plList.get(playlistID).getAllSongs();
            playlistChosen = true;
        }
        else {
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String[] projection = {
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media._ID
            };
            final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

            Cursor cursor = null;
            try {
                Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
                if (cursor != null) {
                    cursor.moveToFirst();

                    while (!cursor.isAfterLast()) {
                        String title = cursor.getString(0);
                        String artist = cursor.getString(1);
                        String path = cursor.getString(2);
//                    String album  = cursor.getString(3);
                        Long albumID = cursor.getLong(4);
                        Long id = cursor.getLong(5);
//                    String songDuration = cursor.getString(6);
                        cursor.moveToNext();
                        if (path != null && path.endsWith(".mp3")) {
                            songList.add(new Song(id, title, artist, albumID));
                        }
                    }
                } // print to see list of mp3 files
            } catch (Exception e) {
                Log.e("TAG", e.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d("MainActivity", "item clicked");

        if (id == R.id.nav_playlists) {
            Intent intent = new Intent(getApplicationContext(), PlaylistsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_player) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void search(String req){
        List = new ArrayList<Song>();
        if(req.length() == 0) {
            listRV.setAdapter(adapter);
        }
        else {
            for (int i=0; i < songList.size(); i++){
                Song song = songList.get(i);
                if(song.getTitle().toLowerCase().contains(req.toLowerCase()) || song.getArtist().toLowerCase().contains(req.toLowerCase())) {
                    List.add(song);
                }
            }


            SongAdapter adap = new SongAdapter(List);
            listRV.setAdapter(adap);
        }
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }
}
