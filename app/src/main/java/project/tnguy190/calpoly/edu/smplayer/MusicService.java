package project.tnguy190.calpoly.edu.smplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by thuy on 11/27/16.
 * modify by Anh on 11/29/16
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = "MusicService";

    /* to build the Notification bar */
    private Notification notification;

    /* media player */
    private MediaPlayer player;

    /* song list to play */
    private ArrayList<Song> songs;

    /* current position of the song on the playlist */
    protected int songPosn;

    /* to bind with the MainActivity */
    private final IBinder musicBind = new MusicBinder();

    //stuff for notifications
    private NotificationManager notificationManager;
    private int notificaitonId = 10231;
    private PendingIntent ppreviousIntent;
    private PendingIntent pplayIntent;
    private PendingIntent pnextIntent;
    private Bitmap icon;
    private PendingIntent pendingIntent;

    private Utilities seekBarCursor;
    protected long currentPosition;
    private String songTitle= "";
    private String songArtist= "";
    private Long albumArt;
    static boolean isPaused;
    private boolean shuffle = false;
    private boolean repeat = false;
    private Random rand;
    static int playlistNum = -1;
    static int state = -1; // playlist number, if -1 all songs
    protected int currlist = -2; //

    LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    /**
     * create a media player at the beginning of service
     */
    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate();               //create the service
        songPosn=0;                     //initialize position

        player = new MediaPlayer();     //create player

        initMusicPlayer();
    }
    /* helper method for onCreate to use */
    private void initMusicPlayer(){
        Log.i(LOG_TAG, "initMusicPlayer");
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        isPaused = true;
        currentPosition = (long)0.0;
        seekBarCursor = new Utilities();
    }


    /**
     * Binder to bind this service with Activities
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent){
        Log.i(LOG_TAG,"onUnbind");
        return false;
    }

    /**
     * Prepare the media player. Make it ready to play media.
     * Set up the Notification bar and start foreground service
     * @param mediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(LOG_TAG, "onPrepared");

        //TODO:
        // when Service is just start, user haven't played music yet, starting player cause Error
        //          E/MediaPlayer: start called in state 1
        //          E/MediaPlayer: error (-38, 0)
        if(isPaused()) {
            mediaPlayer.start();
            mediaPlayer.pause();
        }
        else {
            mediaPlayer.start();
            player.seekTo((int) currentPosition);
            Log.i(LOG_TAG, "Resume player");
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        icon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(isPaused()) {
            setNotificationPlay();
        }
        else {
            setNotificationPause();
        }

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, new Notification());

        Log.i(LOG_TAG, "Done setting up foreground service");

        Intent intent = new Intent(COPA_RESULT);
        intent.putExtra(COPA_MESSAGE, "updateUI");
        broadcaster.sendBroadcast(intent);
    }


    private void setNotificationPause(){
        Log.e(LOG_TAG, "set to pause");
        notification = new NotificationCompat.Builder(this)
                .setContentTitle(songTitle)
                .setTicker("Playing").setContentText(songArtist)
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(createOnDismissedIntent(this, notificaitonId))
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)
                .build();
        notificationManager.notify(notificaitonId, notification);
    }

    private void setNotificationPlay(){
        Log.e(LOG_TAG, "set to play");
        notification = new NotificationCompat.Builder(this)
                .setContentTitle(songTitle)
                .setTicker("Playing").setContentText(songArtist)
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(createOnDismissedIntent(this, notificaitonId))
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)
                .build();
        notificationManager.notify(notificaitonId, notification);
    }
    /**
     * Actual retrieve the Uri and playing the song
     */
    public void playSong() {
        Log.i(LOG_TAG, "playSong");
        Log.e(LOG_TAG, "np" + state);

        if (currlist != state){
            if(state == - 1) {
                songs = getSongs();
            }
            else {
                songs = PlaylistsActivity.plList.get(playlistNum).getAllSongs();
            }
            currlist = state;
        }

        if (songs.size()==0) return;

        if (isPaused) {
            isPaused = false;
            player.reset();

            //get song
            Song playSong = songs.get(songPosn);
            songTitle  = playSong.getTitle();
            songArtist = playSong.getArtist();
            albumArt   = playSong.getAlbumArt();
            Log.i("playsong: ", songTitle + " : " + albumArt);
            //get id
            long currSong = playSong.getID();
            //set uri
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong);

            try{
                player.setDataSource(getApplicationContext(), trackUri);
            }
            catch(Exception e){
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            player.prepareAsync();
            setNotificationPause();
        }
        else {
            isPaused = true;
            currentPosition = player.getCurrentPosition();
            pausePlayer();
            setNotificationPlay();
            Log.i(LOG_TAG,"set currentPosition");
            Intent intent = new Intent(COPA_RESULT);
            intent.putExtra(COPA_MESSAGE, "updateUI");
            broadcaster.sendBroadcast(intent);
        }
    }

    public void playSong(int pos) {
        Log.e(LOG_TAG, "playSong from pos " + pos);
        Log.e(LOG_TAG, "p" + state);

        /*
        if (state == Constants.NEED_TO_UPDATE_TO_DEFAULT){
            songs = getSongs();
            state = Constants.CURRENTLY_DEFAULT;
        }
        else if (state == Constants.NEED_TO_UPDATE_TO_A_PLAYLIST) {
            songs = PlaylistsActivity.plList.get(playlistNum).getAllSongs();
            state = Constants.CURRENTLY_A_PLAYLIST;
        } */
        if (currlist != state){
            if(state == - 1) {
                songs = getSongs();
            }
            else {
                songs = PlaylistsActivity.plList.get(playlistNum).getAllSongs();
            }
            currlist = state;
        }

        isPaused = false;
        player.reset();
        songPosn = pos;
        //get song
        Song playSong = songs.get(pos);
        songTitle=playSong.getTitle();
        songArtist=playSong.getArtist();
        albumArt=playSong.getAlbumArt();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        Intent intent = new Intent(COPA_RESULT);
        intent.putExtra(COPA_MESSAGE, "updateUI");
        broadcaster.sendBroadcast(intent);

        setNotificationPause();
    }


    public void findSong(long songId) {
        if (currlist != state){
            if(state == - 1) {
                songs = getSongs();
            }
            else {
                songs = PlaylistsActivity.plList.get(playlistNum).getAllSongs();
            }
            currlist = state;
        }
        int pos = 0;
        while(pos < songs.size()) {
            if (songs.get(pos).getID() == songId) {
                Log.e("TAG", "found " + songs.get(pos).getTitle());
                playSong(pos);
            }
            pos++;
        }

    }
    /**
     * Compute the previous song to play
     */
    public void playPrev(){
        if (currlist != state){
            if(state == - 1) {
                songs = getSongs();
            }
            else {
                songs = PlaylistsActivity.plList.get(playlistNum).getAllSongs();
            }
            currlist = state;
            songPosn = 0;
        }
        songPosn--;
        if(songPosn < 0) songPosn=songs.size()-1;
        isPaused = false;
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
        songArtist=playSong.getArtist();
        albumArt=playSong.getAlbumArt();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        Intent intent = new Intent(COPA_RESULT);
        intent.putExtra(COPA_MESSAGE, "updateUI");
        broadcaster.sendBroadcast(intent);

        setNotificationPause();
    }

    /**
     * Compute the next song to play according to repeat/shuffle option.
     * Then play that song
     */
    public void playNext(){
//        Log.d(TAG, "playnext");

        if (songs != null) {
            // repeat option is first priority when choosing next song
            if (repeat){}
            // shuffle option is second priority
            else if (shuffle) {
                int newSong = songPosn;
                while (newSong == songPosn) {
                    newSong = rand.nextInt(songs.size());
                }
                songPosn = newSong;
            }
            // no option were selected
            else {
                songPosn++;
                if (songPosn >= songs.size()) songPosn = 0;
            }

            if(songs == null || songs.size()==0)
                songs = getSongs();
            isPaused = false;
            player.reset();
            //get song
            Song playSong = songs.get(songPosn);
            songTitle=playSong.getTitle();
            songArtist=playSong.getArtist();
            albumArt   = playSong.getAlbumArt();
            //get id
            long currSong = playSong.getID();
            //set uri
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong);
            setNotificationPause();
            try{
                player.setDataSource(getApplicationContext(), trackUri);
            }
            catch(Exception e){
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            player.prepareAsync();
            Intent intent = new Intent(COPA_RESULT);
            intent.putExtra(COPA_MESSAGE, "updateUI");
            broadcaster.sendBroadcast(intent);
        }
    }

    /**
     * After complete playing a song. Reset the player and play the next song
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(LOG_TAG, "onCompletion");
        mp.reset();
        currentPosition = 0;
        //player.seekTo(0);
        playNext();

        Intent intent = new Intent(COPA_RESULT);
        intent.putExtra(COPA_MESSAGE, "updateUI");
        broadcaster.sendBroadcast(intent);
    }

    /**
     * This method will be call when startService() is call from inside Activity. It cause this
     *  service to be up. Any click action on the notification bar will make call to this method
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");
        if (intent.getAction() == null) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            this.onPrepared(player);
        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
            // Call play prev
            this.playPrev();
//            player.seekTo(0);
            currentPosition = (long)0.0;
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
            // Call play
            this.playSong();
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
            // Call play next
            this.playNext();
//            player.seekTo(0);
            currentPosition = (long)0.0;
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    /** -----GET and SET methods to update Avticities UI------------ */
    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }
    public String getSongTitle(){return songTitle;}
    public String getSongArtist(){return songArtist;}
    public boolean isPaused() {return isPaused;}
    public boolean isRepeat() {return repeat;}
    public boolean isShuffle() {return shuffle;}
    public MediaPlayer getPlayer(){return player;}
    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Long getAlbumArt() {return albumArt;}

    /**
     * Setter method for Activity to control the service
     */
    public void setShuffle(){
        Log.i(LOG_TAG, "-------set Shuffle");
        if(shuffle)
            shuffle=false;
        else {
            shuffle = true;
            rand = new Random();
        }
    }

    /**
     * Setter method for Activity to control the service
     */
    public void setRepeat(){
        Log.i(LOG_TAG, "-------set Repeat");
        if(repeat) repeat = false;
        else repeat = true;
    }

    @Override
    public void onDestroy() {
        Log.d("MUSICSERVICE", "onDestroy() called");
        //stopForeground(true);
        player.stop();
        player.reset();
        player.release();
        super.onDestroy();
    }

    /**
     * Get the whole list of song. The player will search and play all song incase
     * playlist of song have not been set by user.
     */
    public ArrayList<Song> getSongs() {
        ArrayList<Song> songs = new ArrayList<Song>();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DURATION,
                //MediaStore.Audio.Albums.ALBUM_ART
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);

            if( cursor != null){
                cursor.moveToFirst();

                while( !cursor.isAfterLast() ){
                    String title = cursor.getString(0);
                    String artist = cursor.getString(1);
                    String path = cursor.getString(2);
                    String album = cursor.getString(3);
                    Long albumID = cursor.getLong(4);
                    Long id = cursor.getLong(5);
                    String songDuration = cursor.getString(6);
                    //String albumArt = cursor.getString(7);
                    cursor.moveToNext();
                    if(path != null && path.endsWith(".mp3")) {
                        songs.add(new Song(id, title, artist, albumID));
                    }
                    Log.i("getSong: ", title + "| by " + artist + "(" + songDuration
                            + "), albumID " + albumID + " : " + album);
                }
            }
        } catch (Exception e) {
            Log.e("TAG", e.toString());
            e.printStackTrace();
        } finally{
            if( cursor != null){
                cursor.close();
            }
        }
        return songs;
    }

    /**---- Haven't used these method yet.
     *      These method Provide the access and control the playing song
     *      USE these for setting up seek bar
     */
    public void setSong(int songIndex){
        songPosn=songIndex;
    }
    public int getPosn(){
        return player.getCurrentPosition();
    }
    public int getDur(){
        return player.getDuration();
    }
    public boolean isPng(){
        return player.isPlaying();
    }
    public void pausePlayer(){
        player.pause();
    }
    public void seek(int posn){
        player.seekTo(posn);
    }
    public void go(){
        player.start();
    }


    public static class NotificationDismissedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getExtras().getInt("com.my.app.notificationId");

            if (notificationId == 10231) {
//                stopForeground(false);
                context.stopService(new Intent(context, MusicService.class));
            }
        }
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("com.my.app.notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    public void setCurrList(int i) {
        currlist = i;
    }

    public void setSongPosn(int i ) {
        songPosn = i;
    }

    public void saveCurrentPosition() {
        currentPosition = player.getCurrentPosition();
    }
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }
}

