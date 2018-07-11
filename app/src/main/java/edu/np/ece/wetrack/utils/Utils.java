package edu.np.ece.wetrack.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String getLocalDateFromUtc(String utcDate, String oldDateFormat, String newDateFormat) {
        String localDate;
        Log.d(TAG, "UTC: " + utcDate);
        if (oldDateFormat == null) oldDateFormat = "yyyy-MM-dd'T'HH:mm:ss+00:00";
        if (newDateFormat == null) newDateFormat = "yyyy-MM-dd HH:mm:ss";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(oldDateFormat);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(utcDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat(newDateFormat);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            localDate = dateFormatter.format(value);

            //Log.d("ourDate", ourDate);
        } catch (Exception e) {
            e.printStackTrace();
            localDate = "00-00 00:00";
        }
        Log.d(TAG, "Local: " + localDate);
        return localDate;
    }
}
