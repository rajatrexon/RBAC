package com.esq.rbac.service.contact.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;


@Converter
public class JpaDateConvertor implements AttributeConverter<Date, Timestamp> {
    /**
     * Converts the value stored in the entity attribute into the
     * data representation to be stored in the database.
     *
     * @param date the entity attribute value to be converted
     * @return the converted data to be stored in the database
     * column
     */
    @Override
    public Timestamp convertToDatabaseColumn(Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(adjust(date.getTime(), 1));
    }

    /**
     * Converts the data stored in the database column into the
     * value to be stored in the entity attribute.
     * Note that it is the responsibility of the converter writer to
     * specify the correct <code>dbData</code> type for the corresponding
     * column for use by the JDBC driver: i.e., persistence providers are
     * not expected to do such type conversion.
     *
     * @param dbData the data from the database column to be
     *               converted
     * @return the converted value to be stored in the entity
     * attribute
     */
    @Override
    public Date convertToEntityAttribute(Timestamp dbData) {
        if (dbData == null) {
            return null;
        }
        return new Date(adjust(dbData.getTime(), -1));
    }

    private long adjust(long millis, long direction) {
        return millis + (direction * TimeZone.getDefault().getOffset(millis));
    }
}
