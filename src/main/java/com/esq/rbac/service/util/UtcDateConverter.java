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
package com.esq.rbac.service.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

@Converter
public class UtcDateConverter implements AttributeConverter<Date, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(this.toUTC(date.getTime()));
    }

    @Override
    public Date convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(this.fromUTC(timestamp.getTime()));
    }

    private long toUTC(long millis) {
        return millis - (long) TimeZone.getDefault().getOffset(millis);
    }

    private long fromUTC(long millis) {
        return millis + (long) TimeZone.getDefault().getOffset(millis);
    }
}

