package com.esq.rbac.service.restriction.util;

/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.restriction.iprange.RestrictionIpRange;
import com.google.common.base.Strings;
import java.math.BigInteger;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestrictionUtil {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final int DAY_OF_WEEK_INDEX[] = {-1, 0, 1, 2, 3, 4, 5, 6};
    private static final char FULL_HOUR = '1';
    private static final char FIRST_HALF = '2';
    private static final char SECOND_HALF = '3';

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd");
            return result;
        }
    };
    private static final ThreadLocal<DateFormat> DATETIME_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            return result;
        }
    };

    public static boolean isIpRestricted(String clientAddress, Restriction restriction) {
        return false;
    }

    public static boolean isTimeRestricted(Date now, Restriction restriction) {
        if (now == null || restriction == null) {
            return false;
        }

        try {
            Date fromDate = parseDate(restriction.getFromDate(), restriction.getTimeZone());
            if (fromDate != null && now.before(fromDate)) {
                log.info("isAllowedLoginTime; fromDate={}; now={}", fromDate, now);
                return true;
            }
        } catch (Exception e) {
            log.warn("isTimeRestricted; fromDate exception={}", e);
        }

        try {
            Date toDate = parseDate(restriction.getToDate(), restriction.getTimeZone(), true);
            if (toDate != null && now.after(toDate)) {
                log.info("isAllowedLoginTime; now={}; toDate={}", now, toDate);
                return true;
            }
        } catch (Exception e) {
            log.warn("isTimeRestricted; toDate exception={}", e);
        }

        String timeZoneName = Strings.isNullOrEmpty(restriction.getTimeZone())
                ? "UTC"
                : restriction.getTimeZone();
        TimeZone tz = TimeZone.getTimeZone(timeZoneName);
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(now.getTime());

        return isTimeRestricted(c, restriction.getDayOfWeek(), restriction.getHours());
    }

    private static boolean isTimeRestricted(Calendar c, String dayOfWeekArray, String hourArray) {
        if (c == null) {
            return false;
        }

        if (dayOfWeekArray != null) {
            // dayOfWeek is 7 character string, first character is Sunday, second Monday, etc.
            int dayOfWeekIndex = DAY_OF_WEEK_INDEX[c.get(Calendar.DAY_OF_WEEK)];
            if (dayOfWeekIndex < 0) {
                throw new IllegalStateException("Error in DAY_OF_WEEK_INDEX");
            }
            if (dayOfWeekIndex >= dayOfWeekArray.length()) {
                log.debug("isTimeRestricted; day not marked as allowed '{}'", dayOfWeekArray);
                return true;
            }
            char dayOfWeekCode = dayOfWeekArray.charAt(dayOfWeekIndex);
            if (dayOfWeekCode == '-' || dayOfWeekCode == ' ') {
                log.debug("isTimeRestricted; day marked as disallowed '{}'", dayOfWeekArray);
                return true;
            }
        }

        if (hourArray != null) {
            int hour = c.get(Calendar.HOUR_OF_DAY); // 0-23
            int hourIndex = hour;
            if (hourIndex >= hourArray.length()) {
                log.debug("isTimeRestricted; hour not marked as allowed '{}'", hourArray);
                return true;
            }
            char hourCode = hourArray.charAt(hourIndex);
            int minute = c.get(Calendar.MINUTE);
            log.debug("isTimeRestricted; hour={}; minute={}; hourCode={}, hours='{}'",
                    hour, minute, hourCode, hourArray);
            if (minute >= 30 && hourCode == FIRST_HALF) {
                // allowed first half, but we are in second
                return true;
            }
            if (minute < 30 && hourCode == SECOND_HALF) {
                // allowed second half, but we are in first
                return true;
            }
            if (hourCode != FULL_HOUR && hourCode != FIRST_HALF && hourCode != SECOND_HALF) {
                // not marked as full allowed hour
                return true;
            }
        }

        return false;
    }

    public static Date parseDate(String text) throws ParseException {
        return parseDate(text, null, false);
    }

    public static Date parseDate(String text, String timeZone) throws ParseException {
        return parseDate(text, timeZone, false);
    }

    public static Date parseDate(String text, String timeZone, boolean endOfDay) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }

        if (timeZone == null || timeZone.isEmpty()) {
            timeZone = "UTC";
        }
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        DATE_FORMAT.get().setTimeZone(tz);

        Date d = DATE_FORMAT.get().parse(text);

        if (endOfDay) {
            Calendar c = Calendar.getInstance(tz);
            c.setTimeInMillis(d.getTime());
            c.add(Calendar.HOUR, 24);
            d = c.getTime();
        }
        return d;
    }

    public static Date parseDateTime(String text) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return DATETIME_FORMAT.get().parse(text);
    }

    public static String formatUTC(Date d) {
        DATETIME_FORMAT.get().setTimeZone(UTC);
        return DATETIME_FORMAT.get().format(d);
    }

    public static String checkDate(String input) throws ParseException {

        if (input == null || input.isEmpty()) {
            return null;
        }
        Date d = RestrictionUtil.parseDate(input);
        if (d != null) {
            return DATE_FORMAT.get().format(d);
        }
        return null;
    }

    public static InetAddress parseAddress(String input) throws Exception{
        InetAddress result = InetAddress.getByName(input);
        return result;
    }

    public static RestrictionIpRange parseRange(String input) throws Exception {
        if (Strings.isNullOrEmpty(input)) {
            return null;
        }
        String addressText = input.trim();
        String blockSizeText = "1";

        int hashPosition = input.indexOf('#');
        if (hashPosition >= 0) {
            addressText = input.substring(0, hashPosition).trim();
            blockSizeText = input.substring(hashPosition + 1).trim();
        }
        InetAddress address = InetAddress.getByName(addressText);
        int blockSize = Strings.isNullOrEmpty(blockSizeText)
                ? 1
                : Integer.parseInt(blockSizeText);

        log.trace("parseRange; input='{}'; address={}; blockSize={}",
                input, address, blockSize);

        return new RestrictionIpRange(address, blockSize);
    }

    public static boolean inRange(String addresText, String rangeText) throws Exception {
        return inRange(
                InetAddress.getByName(addresText),
                parseRange(rangeText));
    }

    public static boolean inRange(InetAddress address, RestrictionIpRange range) {
        BigInteger a = toBigInteger(address);
        BigInteger s = BigInteger.valueOf(range.getBlockSize() - 1);
        BigInteger r1 = toBigInteger(range.getAddress());
        BigInteger r2 = r1.add(s);

        return a.compareTo(r1) >= 0 && a.compareTo(r2) <= 0;
    }

    private static BigInteger toBigInteger(InetAddress address) {
        int sourceSize = address.getAddress().length;
        byte[] buffer = new byte[sourceSize + 1];
        buffer[0] = 0;
        System.arraycopy(address.getAddress(), 0, buffer, 1, sourceSize);
        return new BigInteger(buffer);
    }
}

