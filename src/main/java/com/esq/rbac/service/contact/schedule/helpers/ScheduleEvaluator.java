package com.esq.rbac.service.contact.schedule.helpers;

import com.esq.rbac.service.contact.schedule.embedded.ScheduleRule;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.contact.schedule.domain.Schedule;
import com.esq.rbac.service.contact.util.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Component
@Slf4j
public class ScheduleEvaluator {

    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static final ThreadLocal<SimpleDateFormat> timestampFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }
    };
    private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    private static final int calendarDayOfWeekIndex[] = {-1, 0, 1, 2, 3, 4, 5, 6};
    private static final char FULL_HOUR = '1';
    private static final char FIRST_HALF = '2';
    private static final char SECOND_HALF = '3';
    public static final long OUT_OF_WORKING_HOURS = 0;

    /**
     * Evaluate current schedule type at the time of call.
     *
     * @param schedule Schedule object with list of Schedule Rules.
     * @return Schedule type (working, closed, etc.)
     */
    public static long evaluate(Schedule schedule) {
        return evaluate(schedule, null);
    }

    /**
     * Evaluate current schedule type at the provided timestamp.
     *
     * @param schedule Schedule object with list of Schedule Rules.
     * @param timestamp Timestamp to be used for evaluation.
     * @return Schedule type (working, closed, etc.)
     */
    public static long evaluate(Schedule schedule, Calendar timestamp) {
        long result = OUT_OF_WORKING_HOURS;
        TimeZone scheduleTimeZone = TimeZone.getTimeZone(schedule.getTimeZone());
        if (timestamp == null) {
            timestamp = Calendar.getInstance(scheduleTimeZone);
        } else {
            timestamp.setTimeZone(scheduleTimeZone);
        }
        for (ScheduleRule scheduleRule : schedule.getRules()) {
            if (isWithinPeriod(timestamp, scheduleRule.getFromDate(), scheduleRule.getToDate()) == false) {
                continue;
            }
            if (isApplicable(timestamp, scheduleRule.getDayOfWeek(), scheduleRule.getHour()) == false) {
                continue;
            }
            result = scheduleRule.getIsOpen() ? 1 : 0;
            log.debug("evaluate; result={}; timestamp={}; scheduleName={}; rule={}",
                    result, DebugUtil.toString(timestamp), schedule.getName(), scheduleRule);
            return result;
        }

        log.debug("evaluate; result={}; timestamp={}; scheduleName={}",
                result, DebugUtil.toString(timestamp), schedule.getName());
        return result;
    }

    public static Calendar findAvailableTime(Schedule schedule, Calendar timestamp) {
        TimeZone scheduleTimeZone = TimeZone.getTimeZone(schedule.getTimeZone());
        if (timestamp == null) {
            timestamp = Calendar.getInstance(scheduleTimeZone);
        } else {
            timestamp.setTimeZone(scheduleTimeZone);
        }
        Calendar result = (Calendar) timestamp.clone();

        if (OUT_OF_WORKING_HOURS != evaluate(schedule, result)) {
            return result;
        }

        // set possible result to 0 or 30 minutes
        result.set(Calendar.MINUTE, (result.get(Calendar.MINUTE) / 30) * 30);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);

        // test availability for period of 14 days
        // by default return availability at the end of test period
        int notAvailable=(14 * 24 * 2)-1;
        for (int i = 0; i < (14 * 24 * 2); i++) {
            result.add(Calendar.MINUTE, 30);
            if (OUT_OF_WORKING_HOURS != evaluate(schedule, result)) {
                break;
            }else if(i==notAvailable){
                result=null;
            }
        }

        return result;
    }

    public static Calendar findAvailableTimeNextSchedule(Schedule schedule, Calendar timestamp) {
        TimeZone scheduleTimeZone = TimeZone.getTimeZone(schedule.getTimeZone());
        if (timestamp == null) {
            timestamp = Calendar.getInstance(scheduleTimeZone);
        } else {
            timestamp.setTimeZone(scheduleTimeZone);
        }
        Calendar result = (Calendar) timestamp.clone();

        if (OUT_OF_WORKING_HOURS != evaluate(schedule, result)) {
            return result;
        }

        // set possible result to 0 or 30 minutes
        result.set(Calendar.MINUTE, (result.get(Calendar.MINUTE) / 30) * 30);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);

        result.add(Calendar.DAY_OF_MONTH, -1); //reduced one day
        // test availability for period of 14 days
        // by default return availability at the end of test period
        int notAvailable=(14 * 24 * 2)-1;
        for (int i = 0; i < (14 * 24 * 2); i++) {
            result.add(Calendar.MINUTE, 30);
            if (OUT_OF_WORKING_HOURS != evaluate(schedule, result)) {
                break;
            }else if(i==notAvailable){
                result=null;
            }
        }

        return result;
    }

    public static Calendar findWorkEndTime(Schedule schedule, Calendar timestamp, int workMinutes) {
        // 1st: find out when work can start
        timestamp = findAvailableTime(schedule, timestamp);
        TimeZone scheduleTimeZone = TimeZone.getTimeZone(schedule.getTimeZone());
        timestamp.setTimeZone(scheduleTimeZone);

        // 2nd: round to start of half hour/full hour
        int remainingMinutes = workMinutes;
        int toHalfHour = 30 - (timestamp.get(Calendar.MINUTE) % 30);
        if (toHalfHour < 30) {
            if (remainingMinutes < toHalfHour) {
                timestamp.add(Calendar.MINUTE, remainingMinutes);
                return timestamp;
            }
            timestamp.add(Calendar.MINUTE, toHalfHour);
            remainingMinutes -= toHalfHour;
        }

        // 3rd: iterate for each 30 working minutes
        int maxSteps = 14 * 24 * 2; // days * hoursPerDay * hourHalves
        while (remainingMinutes > 0 && --maxSteps > 0) {
            if (OUT_OF_WORKING_HOURS == evaluate(schedule, timestamp)) {
                timestamp.add(Calendar.MINUTE, 30);

            } else {
                int advance = Math.min(remainingMinutes, 30);
                remainingMinutes -= advance;
                timestamp.add(Calendar.MINUTE, advance);
            }
        }
        return timestamp;
    }

    /**
     * Check if provided timestamp is within time period (from, to).
     *
     * Timestamp must be in time zone of a schedule.
     *
     * @param timestamp Timestamp
     * @param fromDate Start date (start of day, 00:00:00) of time period, can
     * be null meaning open time period
     * @param toDate End date (end of day 24:00:00) of time period, can be null
     * meaning open time period
     * @return true when within time period or time period not specified (both
     * fromDate and toDate open).
     */
    public static boolean isWithinPeriod(Calendar timestamp, String fromDate, String toDate) {
        // fromDate and toDate are in the same time zone as timestamp
        dateFormat.get().setTimeZone(timestamp.getTimeZone());

        if (fromDate != null) {
            try {
                Date date = dateFormat.get().parse(fromDate);
                if (timestamp.getTimeInMillis() < date.getTime()) {
                    return false;
                }
            } catch (Exception e) {
            }
        }

        if (toDate != null) {
            try {
                Date date = dateFormat.get().parse(toDate);
                Calendar endOfDay = Calendar.getInstance(timestamp.getTimeZone());
                endOfDay.setTimeInMillis(date.getTime());
                endOfDay.add(Calendar.DAY_OF_MONTH, 1);
                if (timestamp.getTimeInMillis() > endOfDay.getTimeInMillis()) {
                    return false;
                }
            } catch (Exception e) {
            }
        }

        return true;
    }

    /**
     * Check if provided timestamp is after the end Date of the schedule.
     *
     * Timestamp must be in time zone of a schedule.
     *
     * @param timestamp Timestamp
     * @param toDate End date (end of day 24:00:00) of time period, can be null
     * meaning open time period
     * @return true when within time period or time period not specified (both
     * fromDate and toDate open).
     */
    public static boolean isScheduleOld(Calendar timestamp, String toDate) {
        // toDate is in the same time zone as timestamp
        dateFormat.get().setTimeZone(timestamp.getTimeZone());
        if (toDate != null) {
            try {
                Date date = dateFormat.get().parse(toDate);
                Calendar endOfDay = Calendar.getInstance(timestamp.getTimeZone());
                endOfDay.setTimeInMillis(date.getTime());
                endOfDay.add(Calendar.DAY_OF_MONTH, 1);
                if (timestamp.getTimeInMillis() > endOfDay.getTimeInMillis()) {
                    return true;
                }
            } catch (Exception e) {
            }
        }

        return false;
    }

    /**
     * Check if provided timestamp is applicable to day of week and hour of day
     * rules.
     *
     * Timestamp must be in time zone of a schedule.
     *
     * @param timestamp Timestamp
     * @param dayOfWeekArray String with 7 characters marking days of week.
     * First character is for Sunday, second for Monday, etc.
     * @param hourArray String with 24 characters marking hours of a day.
     * @return True when timestamp is applicable
     */
    public static boolean isApplicable(Calendar timestamp, String dayOfWeekArray, String hourArray) {
        if (timestamp == null || dayOfWeekArray == null || hourArray == null) {
            return false;
        }
        // dayOfWeek is 7 character string, first character is Sunday, second Monday, etc.
        int dayOfWeekIndex = calendarDayOfWeekIndex[timestamp.get(Calendar.DAY_OF_WEEK)];
        if (dayOfWeekIndex < 0 || dayOfWeekIndex >= dayOfWeekArray.length()) {
            return false;
        }
        char dayOfWeekCode = dayOfWeekArray.charAt(dayOfWeekIndex);
        if (dayOfWeekCode == '-' || dayOfWeekCode == ' ') {
            return false;
        }

        int hour = timestamp.get(Calendar.HOUR_OF_DAY); // 0-23
        int hourIndex = hour;
        if (hourIndex >= hourArray.length()) {
            return false;
        }
        char hourCode = hourArray.charAt(hourIndex);
        if (hourCode == FULL_HOUR) {
            return true;
        }
        int minute = timestamp.get(Calendar.MINUTE);
        if (minute < 30 && hourCode == FIRST_HALF) {
            return true;
        }
        if (minute >= 30 && hourCode == SECOND_HALF) {
            return true;
        }
        return false;
    }

    public static Calendar parseTimestampToUtcCalendar(String timestamp) throws Exception {
        Calendar result = null;

        if (timestamp != null && timestamp.isEmpty() == false) {
            Date date = timestampFormat.get().parse(timestamp);
            result = Calendar.getInstance(utcTimeZone);
            result.setTime(date);
        }
        // log.debug("parseTimestampToUtcCalendar; timestamp={}; calendar={}", timestamp, result);
        return result;
    }
    public static Calendar parseTimestampToUtcCalendar2(String timestamp, String timezone, Schedule schedule) throws Exception {
        Calendar result = null;

        if (timestamp != null && timestamp.isEmpty() == false) {
            if(timezone == null || !Lookup.isTimeZoneValid(timezone)) {
                timezone = schedule.getTimeZone() != null ? schedule.getTimeZone() : "UTC";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            TimeZone tz = TimeZone.getTimeZone(timezone); // set time zone of user
            sdf.setTimeZone(tz);
            Date date = sdf.parse(timestamp);
            result = Calendar.getInstance(utcTimeZone);
            result.setTime(date);
        }
        return result;
    }
}

