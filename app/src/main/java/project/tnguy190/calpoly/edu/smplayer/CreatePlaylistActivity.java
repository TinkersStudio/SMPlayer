package project.tnguy190.calpoly.edu.smplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by thuy on 11/22/16.
 */

public class CreatePlaylistActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "CreatePlaylistActivity";

    private ArrayList<Song> songList;
    private RecyclerView listRV;
    private EditText search;
    private PlaylistSongAdapter adapter;
    private String info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

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
        songList = new ArrayList<Song>();

        getSongList();

        adapter = new PlaylistSongAdapter(songList);
        listRV.setAdapter(adapter);

        search = (EditText) findViewById(R.id.search_bar);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        Intent mIntent = getIntent();
        info = mIntent.getStringExtra("search");
        if(info != null) {
            Log.i(TAG, info);
            search.setText(info);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "save playlist");

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d("MainActivity", "item clicked");

        if (id == R.id.nav_playlists) {
            Intent intent = new Intent(getApplicationContext(), PlaylistsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_all_songs) {
            Log.d("MainActivity", "open all songs");
            Intent intent = new Intent(getApplicationContext(), AllSongsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_player) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_search) {
            final EditText input = new EditText(CreatePlaylistActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            AlertDialog.Builder builder = new AlertDialog.Builder(CreatePlaylistActivity.this);
            builder.setCancelable(false);
            builder.setView(input); // uncomment this line

            builder.setMessage(R.string.search)
                    .setPositiveButton(R.string.search_dialog_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "search");
                        }
                    })
                    .setNegativeButton(R.string.search_dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            builder.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getSongList() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
            if( cursor != null){
                cursor.moveToFirst();

                while( !cursor.isAfterLast() ){
                    String title = cursor.getString(0);
                    String artist = cursor.getString(1);
                    String path = cursor.getString(2);
//                    String album  = cursor.getString(3);
                    Long albumID = cursor.getLong(4);
                    Long id = cursor.getLong(5);
//                    String songDuration = cursor.getString(6);
                    cursor.moveToNext();
                    if(path != null && path.endsWith(".mp3")) {
                        songList.add(new Song(id, title, artist,albumID));
                    }
                }
            } // print to see list of mp3 files
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }finally{
            if( cursor != null){
                cursor.close();
            }
        }
    }

    public void search(String req){
        ArrayList<Song> List = new ArrayList<Song>();
        if(req.length() == 0) {
            listRV.setAdapter(adapter);
        }
        else {

            for (int i=0; i < songList.size(); i++){
                Song song = songList.get(i);
                Log.i("TAG", song.getTitle());
                if(song.getTitle().toLowerCase().contains(req.toLowerCase()) || song.getArtist().toLowerCase().contains(req.toLowerCase())) {
                    List.add(song);
                }
            }
            SongAdapter adap = new SongAdapter(List);
            listRV.setAdapter(adap);
        }
    }
}
