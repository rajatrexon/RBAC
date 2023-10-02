package com.esq.rbac.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtcDateConverterTest {

    @Mock
    Timestamp inputTimestamp;
    @InjectMocks
    UtcDateConverter utcDateConverter;
    private UtcDateConverter converter;

    @BeforeEach
    void setUp() {
        converter = new UtcDateConverter();
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void convertToDatabaseColumn() {

        long inputMillis = 1629999999000L;
        Date inputDate = new Date(inputMillis);
        long expectedMillis = inputMillis - TimeZone.getDefault().getOffset(inputMillis);

        Timestamp result = converter.convertToDatabaseColumn(inputDate);
        assertEquals(expectedMillis, result.getTime());
    }

    @Test
    void convertToDatabaseColumn_date_null() {
        long inputMillis = 1629999999000L;

        Date inputDate = null;
        long expectedMillis = inputMillis - TimeZone.getDefault().getOffset(inputMillis);

        Timestamp result = converter.convertToDatabaseColumn(inputDate);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute() {

        long inputMillis = 1629999999000L;

        Mockito.when(inputTimestamp.getTime()).thenReturn(inputMillis);
        long expectedMillis = inputMillis + TimeZone.getDefault().getOffset(inputMillis);
        Date result = converter.convertToEntityAttribute(inputTimestamp);
        assertEquals(expectedMillis, result.getTime());
    }

    @Test
    void convertToEntityAttribute_TimeStamp_null() {

        long inputMillis = 1629999999000L;

        Mockito.when(inputTimestamp.getTime()).thenReturn(inputMillis);
        long expectedMillis = inputMillis + TimeZone.getDefault().getOffset(inputMillis);
        Date result = converter.convertToEntityAttribute(null);
        assertNull(result);
    }
}