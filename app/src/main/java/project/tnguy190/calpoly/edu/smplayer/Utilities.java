package project.tnguy190.calpoly.edu.smplayer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by anhnguyen on 11/30/16.
 */

public class Utilities {

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }


    public static void writeToJSonFile(Context context, Playlist playlist) {
        try {
            JSONObject jsonPlaylist = new JSONObject();
            jsonPlaylist.put("id", playlist.getID());
            jsonPlaylist.put("title", playlist.getTitle());

            JSONArray jsonSongsArr = new JSONArray();
            for (Song song : playlist.getAllSongs()) {
                JSONObject songObj = new JSONObject();
                songObj.put("id", song.getID());
                songObj.put("title", song.getTitle());
                songObj.put("artist", song.getArtist());
                songObj.put("albumID", song.getAlbumArt());
                jsonSongsArr.put(songObj);
            }

            jsonPlaylist.put("songs", jsonSongsArr);

            try {
                FileOutputStream fOut = context.openFileOutput(playlist.getTitle() + ".json", MODE_PRIVATE);

                fOut.write(jsonPlaylist.toString().getBytes());
                fOut.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public static Playlist deserializeJson(Context context, String fileName) {
        // read from the file
        StringBuilder sb = new StringBuilder();
        int plID = -1;
        long songID, songAlbumID;
        String plTitle = "", songTitle, songArtist;
        ArrayList<Song> songs = new ArrayList<Song>();

        try {
            File file = new File(context.getFilesDir() + fileName);
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject jObj = (JSONObject) new JSONTokener(sb.toString()).nextValue();
            plTitle = jObj.getString("title");
            plID = jObj.getInt("id");

            JSONArray jArr = jObj.getJSONArray("songs");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject tmp = jArr.getJSONObject(i);

                songID = tmp.getLong("id");
                songTitle = tmp.getString("title");
                songArtist = tmp.getString("artist");
                songAlbumID = tmp.getLong("albumID");

                songs.add(new Song(songID, songTitle, songArtist, songAlbumID));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return new Playlist(plID, plTitle, songs);
    }
}

