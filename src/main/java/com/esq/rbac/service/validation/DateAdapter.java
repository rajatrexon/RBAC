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
package com.esq.rbac.service.validation;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateAdapter extends XmlAdapter<Calendar, Date> {

    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    @Override
    public Calendar marshal(Date v) throws Exception {
        if (v == null) {
            return null;
        }
        Calendar result = Calendar.getInstance(utcTimeZone);
        result.setTimeInMillis(v.getTime());
        return result;
    }

    @Override
    public Date unmarshal(Calendar v) throws Exception {
        if (v == null) {
            return null;
        }
        return new Date(v.getTimeInMillis());
    }
}
