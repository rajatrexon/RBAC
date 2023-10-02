/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.contact.schedule.embedded;


import com.esq.rbac.service.validation.annotation.ValidateDate;
import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Embeddable
@Table(name = "schedule_rule", schema = "contact")
public class ScheduleRule {

    @Length(min = 0, max = 100)
    private String description;
    @ValidateDate
    @Column(name = "from_date", length = 10)
    private String fromDate;
    @ValidateDate
    @Column(name = "to_date", length = 10)
    private String toDate;
    @Mandatory
    @Length(min = 0, max = 7)
    @Column(name = "dow_array", nullable = false)
    private String dayOfWeek;
    @Mandatory
    @Length(min = 0, max = 24)
    @Column(name = "hour_array", nullable = false)
    private String hour;
    @Mandatory
    @Column(name = "is_open", nullable = false)
    private boolean isOpen;

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

    public void setDayOfWeek(String dow) {
        this.dayOfWeek = dow;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ScheduleRule: {description:'").append(description);
        sb.append("', fromDate: '").append(fromDate);
        sb.append("', toDate:'").append(toDate);
        sb.append("', dow:'").append(dayOfWeek);
        sb.append("', hour:'").append(hour);
        sb.append("', isOpen:'").append(isOpen);
        sb.append("'}}");
        return sb.toString();
    }
}
