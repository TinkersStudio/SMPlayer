package project.tnguy190.calpoly.edu.smplayer;

/**
 * Created by thuy on 11/27/16.
 */

public class Constants {

    public static int NEED_TO_UPDATE_TO_A_PLAYLIST = 2;
    public static int CURRENTLY_A_PLAYLIST = 1;
    public static int CURRENTLY_DEFAULT = -1;
    public static int NEED_TO_UPDATE_TO_DEFAULT = -2;
    public interface ACTION {
        public static String MAIN_ACTION = "com.truiton.foregroundservice.action.main";
        public static String PREV_ACTION = "com.truiton.foregroundservice.action.prev";
        public static String PLAY_ACTION = "com.truiton.foregroundservice.action.play";
        public static String NEXT_ACTION = "com.truiton.foregroundservice.action.next";
        public static String STARTFOREGROUND_ACTION = "com.truiton.foregroundservice.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.truiton.foregroundservice.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
