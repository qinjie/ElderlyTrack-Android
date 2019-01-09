package edu.np.ece.elderlytrack.utils;

import android.app.Activity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static void hideSoftKeyboard(Activity activity) {
        final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            if (activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

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

    public static int getAge(Date birthday) {
        Date current = new Date();
        return (getDiffYears(birthday, current));
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }
}
