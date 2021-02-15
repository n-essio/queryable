package it.coopservice.api.util;

import org.jboss.logging.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtils {

    static Logger logger = Logger.getLogger(DateUtils.class);
    static DateFormat dateFormat = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss.SSS");
    private static final List<String> COMMON_FORMATS = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy, HH:mm",
            "dd/MM/yyyy, HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static Date parseDate(String fieldValue) {
        for (String format : COMMON_FORMATS) {
            try {
                return new SimpleDateFormat(format).parse(fieldValue);
            } catch (Exception ignore) {
            }
        }

        return null;
    }

    public static Date toBeginOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        logger.debug(dateFormat.format(date) + " -- toBeginOfDay --> "
                + cal.getTime());
        return cal.getTime();
    }

    public static Date toEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        logger.debug(dateFormat.format(date) + " -- toEndOfDay ----> "
                + cal.getTime());
        return cal.getTime();
    }
}
