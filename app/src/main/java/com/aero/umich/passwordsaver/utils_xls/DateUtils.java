package com.aero.umich.passwordsaver.utils_xls;

import java.text.SimpleDateFormat;
import java.util.Date;

public
class DateUtils {

    // two helper methods for obtaining time stamp
    public static
    Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    public static String toDateString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss, MM/dd/yyyy (EEEE)");
        return simpleDateFormat.format(date);
    }
}
