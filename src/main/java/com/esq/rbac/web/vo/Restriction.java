/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.vo;
import java.util.List;

public class Restriction {

    private Integer restrictionId;
    private String timeZone;
    private String fromDate;
    private String toDate;
    private String dayOfWeek;
    private String hours;
    private List<String> allowedIPs;
    private List<String> disallowedIPs;

    public Integer getRestrictionId() {
        return restrictionId;
    }

    public void setRestrictionId(Integer restrictionId) {
        this.restrictionId = restrictionId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public List<String> getAllowedIPs() {
        return allowedIPs;
    }

    public void setAllowedIPs(List<String> allowedIPs) {
        this.allowedIPs = allowedIPs;
    }

    public List<String> getDisallowedIPs() {
        return disallowedIPs;
    }

    public void setDisallowedIPs(List<String> disallowedIPs) {
        this.disallowedIPs = disallowedIPs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User{restrictionId=").append(restrictionId);
        sb.append("; timeZone=").append(timeZone);
        sb.append("; fromDate=").append(fromDate);
        sb.append("; toDate=").append(toDate);
        sb.append("; dayOfWeek=").append(dayOfWeek);
        sb.append("; hours=").append(hours);
        sb.append("; allowedIPs=").append(allowedIPs);
        sb.append("; disallowedIPs=").append(disallowedIPs);
        sb.append("}");
        return sb.toString();
    }
}
