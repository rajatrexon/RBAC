package com.esq.rbac.service.contact.util;

import com.esq.rbac.service.util.UtcDateConverter;
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

class JpaDateConvertorTest {

    @Mock
    Timestamp inputTimestamp;
    @InjectMocks
    UtcDateConverter utcDateConverter;
    private JpaDateConvertor converter;

    @BeforeEach
    void setUp() {
        converter = new JpaDateConvertor();
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void convertToDatabaseColumn() {

        long inputMillis = 1629999999000L; // Example input millis
        Date inputDate = new Date(inputMillis);
        long expectedMillis = inputMillis + TimeZone.getDefault().getOffset(inputMillis);

        Timestamp result = converter.convertToDatabaseColumn(inputDate);

        assertEquals(expectedMillis, result.getTime());
    }

    @Test
    void convertToEntityAttribute() {
        long inputMillis = 1629999999000L; // Example input millis

        Mockito.when(inputTimestamp.getTime()).thenReturn(inputMillis);
        long expectedMillis = inputMillis - TimeZone.getDefault().getOffset(inputMillis);

        Date result = converter.convertToEntityAttribute(inputTimestamp);

        assertEquals(expectedMillis, result.getTime());
    }
}