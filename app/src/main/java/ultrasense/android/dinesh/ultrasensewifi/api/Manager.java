package ultrasense.android.dinesh.ultrasensewifi.api;

import java.util.Calendar;

/**
 * Created by dinesh on 20.02.17.
 */

public class Manager {

    public static String getWaveFileName(){

        Calendar c = Calendar.getInstance();

        int milliseconds = c.get(Calendar.MILLISECOND);
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        return hours+"_"+minutes+"_"+seconds+"_"+milliseconds;
    }

    public static String getTextFileName(){

        Calendar c = Calendar.getInstance();

        //int milliseconds = c.get(Calendar.MILLISECOND);
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        return hours+"_"+minutes+"_"+seconds;
    }
}
