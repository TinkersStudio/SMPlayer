package project.tnguy190.calpoly.edu.smplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.InputStream;

import project.tnguy190.calpoly.edu.smplayer.MusicService.MusicBinder;

/**
 * Created by thuy on 11/27/16.
 * modify by Anh on 11/29/16
 *      album art function OK
 * modify by Anh on 11/30/16
 *      seekBar function OK
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String LAST_PLAYED_SONG_POSN = "LastSongPosition";
    private static final String LAST_PLAYLIST = "LastPlaylistPlayed";
    private static final String LAST_PLAYED_SONG_DURATION = "LastSongDuration";

    /* Intent use for binding service */
    private Intent playIntent;

    /* Indicate if there is an connection with Service */
    private boolean musicBound;

    /* An instance of the MusicService so that
       this Activity can share control with the service */
    static MusicService musicSrv;
    private ImageView play;
    private TextView  title;
    private TextView artist;
    private ImageView artWork;
    static int flag = 0;

    /* Handler to update UI timer, progress bar etc,. */
    private Handler mHandler = new Handler();

    /* Use for seek bar */
    private SeekBar seekBar;
    private boolean isMovingSeekBar = false;
    private Utilities seekBarCursor;        // calculator to generate the correct time
    private TextView songTotalDurationLabel;
    private TextView songCurrentDurationLabel;
    private Thread updateSeekBar;

    private BroadcastReceiver receiver;
    static int totalDuration;

    /**
     * Initialize the welcome view of the Activity which is the Player screen
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "---------------------- onCreate");
        super.onCreate(savedInstanceState);

        isStoragePermissionGranted();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupPlaybackButtonListener();
        setupSeekBar();

        if (savedInstanceState != null) {
            musicSrv.setSongPosn(savedInstanceState.getInt(LAST_PLAYED_SONG_POSN));
            musicSrv.setCurrList(savedInstanceState.getInt(LAST_PLAYLIST));
            musicSrv.setCurrentPosition(savedInstanceState.getLong(LAST_PLAYED_SONG_DURATION));
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(MusicService.COPA_MESSAGE);
                try {
                    if(!musicSrv.getSongArtist().toString().equals("<unknown>")) {
                        title.setText(musicSrv.getSongTitle());
                        Log.d(TAG, "set artist");
                        artist.setText(musicSrv.getSongArtist());
                    }
                    setAlbumArtWork(artWork);
                }
                catch (Exception e) {
                    Log.i(TAG," UI not up yet");
                }
            }
        };
    }
    private void setupPlaybackButtonListener(){
        play = (ImageView) findViewById(R.id.play);
        //final ImageView play = (ImageView) findViewById(R.id.play);
        final ImageView next = (ImageView) findViewById(R.id.play_next);
        final ImageView prev = (ImageView) findViewById(R.id.play_prev);
        final ImageView repeat = (ImageView) findViewById(R.id.repeat);
        final ImageView shuffle = (ImageView) findViewById(R.id.shuffle);
        //final TextView  title = (TextView)  findViewById(R.id.songBeingPlay);
        artWork = (ImageView) findViewById(R.id.album_art);
        title= (TextView)  findViewById(R.id.songBeingPlay);
        artist = (TextView) findViewById(R.id.songBeingPlayArtist);

        title.setSelected(true);
        artist.setSelected(true);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Play/Pause");
                if(musicSrv.isPaused())
                    play.setImageResource(R.drawable.ic_pause);
                else
                    play.setImageResource(R.drawable.ic_play);
                musicSrv.playSong();
                //title.setText(musicSrv.getSongTitle() + "\n" + musicSrv.getSongArtist());
                title.setSelected(true);
                artist.setSelected(true);

                title.setText(musicSrv.getSongTitle());
                if(!musicSrv.getSongArtist().toString().equals("<unknown>")) {
                    Log.d(TAG, "set artist");
                    artist.setText(musicSrv.getSongArtist());
                }
                setAlbumArtWork(artWork);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Next");
                musicSrv.playNext();
                //musicSrv.seek(0);
                musicSrv.setCurrentPosition(0);
                //title.setText(musicSrv.getSongTitle() + "\n" + musicSrv.getSongArtist());
                play.setImageResource(R.drawable.ic_pause);

                title.setSelected(true);
                artist.setSelected(true);
                title.setText(musicSrv.getSongTitle());
                if(!musicSrv.getSongArtist().toString().equals("<unknown>")) {
//                    title.setText(musicSrv.getSongTitle());
                    artist.setText(musicSrv.getSongArtist());
                }
                setAlbumArtWork(artWork);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Prev");
                musicSrv.playPrev();
                //musicSrv.seek(0);
                musicSrv.setCurrentPosition(0);
                //title.setText(musicSrv.getSongTitle() + "\n" + musicSrv.getSongArtist());
                play.setImageResource(R.drawable.ic_pause);

                title.setSelected(true);
                artist.setSelected(true);
                title.setText(musicSrv.getSongTitle());
                if(!musicSrv.getSongArtist().toString().equals("<unknown>")) {
//                    title.setText(musicSrv.getSongTitle());
                    artist.setText(musicSrv.getSongArtist());
                }
                setAlbumArtWork(artWork);
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Repeat");
                musicSrv.setRepeat();
                if(musicSrv.isRepeat())
                    repeat.setImageResource(R.drawable.ic_repeat_once);
                else
                    repeat.setImageResource(R.drawable.ic_repeat);
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Shuffle");
                musicSrv.setShuffle();
                if(musicSrv.isShuffle())
                    shuffle.setImageResource(R.drawable.ic_shuffle);
                else
                    shuffle.setImageResource(R.drawable.ic_shuffle_disabled);
            }
        });
    }
    public void setAlbumArtWork(ImageView artWork){
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Bitmap bitmap = null;
        try {
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, musicSrv.getAlbumArt());
            ContentResolver res = this.getContentResolver();
            InputStream in = res.openInputStream(albumArtUri);
            bitmap = BitmapFactory.decodeStream(in);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                artWork.setClipToOutline(true);
            }
            artWork.setImageBitmap(bitmap);
        } catch (Exception exception) {
            //exception.printStackTrace();
            artWork.setImageResource(R.drawable.ic_default_album_art);
        }
    }

    private void setupSeekBar(){
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        songCurrentDurationLabel = (TextView)findViewById(R.id.timeCompleted);
        songTotalDurationLabel = (TextView)findViewById(R.id.timeTotal);
        mHandler = new Handler();
        seekBarCursor = new Utilities();
        updateSeekBar = new Thread(mUpdateTimeTask);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingSeekBar = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isMovingSeekBar) {
                    Log.i("OnSeekBarChangeListener", "onProgressChanged");
                    seekBarCursor = new Utilities();
                    int currentPosition = seekBarCursor.progressToTimer(seekBar.getProgress(), totalDuration);

                    // forward or backward to certain seconds
                    musicSrv.seek(currentPosition);
                }
            }
        });
    }

    public void playHit(){
        if(musicSrv.isPaused())
            play.setImageResource(R.drawable.ic_pause);
        else
            play.setImageResource(R.drawable.ic_play);

        title.setSelected(true);
        artist.setSelected(true);
        title.setText(musicSrv.getSongTitle());
        if(!musicSrv.getSongArtist().toString().equals("<unknown>")) {
            artist.setText(musicSrv.getSongArtist());
        }
        setAlbumArtWork(artWork);
    }


    /**
     * Everytime we start this activity, bind it to the Service
     */
    @Override
    protected void onStart() {
        Log.i(TAG, "---------------------- onStart");
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);

            startService(playIntent);
            Log.i(TAG, "---------------------- afterStartingService");

            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            Log.i(TAG, "---------------------- afterBindingService");

            // Run the independent thread to update UI
            updateSeekBar.start();

            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter(MusicService.COPA_RESULT)
            );
        }
    }

    /* This variable is the binding connection with the MusicService */
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "---------------------- onServiceConnected");
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    /**
     * Background independent Runnable thread to update the UI of seek bar
     *
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                //No need to update seek bar UI if the player is pausing
                if(!musicSrv.isPaused()) {
                    long totalDuration = musicSrv.getDur();
                    long currentDuration = musicSrv.getPosn();

                    // Displaying Total Duration time
                    songTotalDurationLabel.setText("" + seekBarCursor.milliSecondsToTimer(totalDuration));
                    // Displaying time completed playing
                    songCurrentDurationLabel.setText("" + seekBarCursor.milliSecondsToTimer(currentDuration));

                    // Updating progress bar
                    int progress = (int) (seekBarCursor.getProgressPercentage(currentDuration, totalDuration));
                    //Log.d("Progress", ""+progress);
                    seekBar.setProgress(progress);
                }
            }
            catch (Exception e) {
                //Exception thrown when Service haven't up yet
            }
            // Running this thread after 1000 milliseconds
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "----------------------  onResume");

        if (musicSrv != null) {
            Log.i(TAG, "----------------------  onResume - has MusicService");

            title.setSelected(true);
            artist.setSelected(true);
            title.setText(musicSrv.getSongTitle()); /*+ "\n" + musicSrv.getSongArtist());*/
            artist.setText(musicSrv.getSongArtist());
            if (musicSrv.isPaused() ){
                play.setImageResource(R.drawable.ic_play);
            }
            else {
                play.setImageResource(R.drawable.ic_pause);
            }
        } else
            Log.i(TAG, "----------------------  onResume - does not has MusicService");
        if (flag == 1) {
            Intent mIntent = getIntent();
            Log.i(TAG, "PICKED A SONG```````````````````````````````````````````");
            //musicSrv.playSong();
            musicSrv.findSong(mIntent.getLongExtra("play", 0));
            play.setImageResource(R.drawable.ic_pause);
            getIntent().removeExtra("play");
            flag = 0;

            title.setSelected(true);
            artist.setSelected(true);
            title.setText(musicSrv.getSongTitle()); /*+ "\n" + musicSrv.getSongArtist());*/
            artist.setText(musicSrv.getSongArtist());
            setAlbumArtWork(artWork);

        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        } else if (id == R.id.nav_all_songs) {
            Log.d("MainActivity", "open all songs");
            Intent intent = new Intent(getApplicationContext(), AllSongsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED)) {
                Log.v("TAG","Permission is granted");
                return true;
            } else {

                Log.v("TAG","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("TAG","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    /**
     * Appropriate way to unbind the MusicService when this activity get killed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicConnection != null) {
            unbindService(musicConnection);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "save the last song and playlist");
        super.onSaveInstanceState(savedInstanceState);
        musicSrv.saveCurrentPosition();
        savedInstanceState.putInt(LAST_PLAYED_SONG_POSN, musicSrv.songPosn);
        savedInstanceState.putInt(LAST_PLAYLIST, musicSrv.currlist);
        savedInstanceState.putLong(LAST_PLAYED_SONG_DURATION, musicSrv.currentPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp1);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(input); // uncomment this line
        builder.setMessage(R.string.search)
                .setPositiveButton(R.string.search_dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("-onOptionsSelection", "search" + input.getText());
                        Log.d(TAG, "asdkfhsakdfh");
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

        return true;
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}
