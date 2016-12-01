package project.tnguy190.calpoly.edu.smplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.net.Uri;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import java.io.InputStream;
import android.graphics.BitmapFactory;
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

    /* Intent use for binding service */
    private Intent playIntent;

    /* Indicate if there is an connection with Service */
    private boolean musicBound;

    /* An instance of the MusicService so that
       this Activity can share control with the service */
    private MusicService musicSrv;

    /* Handler to update UI timer, progress bar etc,. */
    private Handler mHandler = new Handler();

    /* Use for seek bar */
    private SeekBar seekBar;
    private boolean isMovingSeekBar = false;
    private Utilities seekBarCursor;        // calculator to generate the correct time
    private TextView songTotalDurationLabel;
    private TextView songCurrentDurationLabel;
    private Thread updateSeekBar;

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
    }
    private void setupPlaybackButtonListener(){
        final ImageView play = (ImageView) findViewById(R.id.play);
        final ImageView next = (ImageView) findViewById(R.id.play_next);
        final ImageView prev = (ImageView) findViewById(R.id.play_prev);
        final ImageView repeat = (ImageView) findViewById(R.id.repeat);
        final ImageView shuffle = (ImageView) findViewById(R.id.shuffle);
        final TextView  title = (TextView)  findViewById(R.id.songBeingPlay);
        final ImageView artWork = (ImageView) findViewById(R.id.album_art);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Play/Pause");
                if(musicSrv.isPaused())
                    play.setImageResource(R.drawable.ic_play);
                else
                    play.setImageResource(R.drawable.ic_pause);
                musicSrv.playSong();
                title.setText(musicSrv.getSongTitle() + "\n" + musicSrv.getSongArtist());
                setAlbumArtWork(artWork);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Next");
                musicSrv.playNext();
                musicSrv.seek(0);
                title.setText(musicSrv.getSongTitle() + "\n" + musicSrv.getSongArtist());
                setAlbumArtWork(artWork);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "-------click Prev");
                musicSrv.playPrev();
                musicSrv.seek(0);
                title.setText(musicSrv.getSongTitle() + "\n" + musicSrv.getSongArtist());
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
    private void setAlbumArtWork(ImageView artWork){
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Bitmap bitmap = null;
        try {
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, musicSrv.getAlbumArt());
            ContentResolver res = this.getContentResolver();
            InputStream in = res.openInputStream(albumArtUri);
            bitmap = BitmapFactory.decodeStream(in);
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
                Log.i("OnSeekBarChangeListener", "onStopTrackingTouch");
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
                    int totalDuration = musicSrv.getDur();
                    int currentPosition = seekBarCursor.progressToTimer(seekBar.getProgress(), totalDuration);

                    // forward or backward to certain seconds
                    musicSrv.seek(currentPosition);
                }
            }
        });
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
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 1000);
        }
    };

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
        } else if (id == R.id.nav_search) {
            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
}
