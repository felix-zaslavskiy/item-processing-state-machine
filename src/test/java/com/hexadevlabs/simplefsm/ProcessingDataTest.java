package com.hexadevlabs.simplefsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class ProcessingDataTest {

    public static class SomePojo {
        LocalDate test;
    }

    @Test
    public void testSerializingProcessingData(){

        ProcessingData processingData = new ProcessingData();
        processingData.set("test", 1);
        LocalDateTime dateTime = LocalDateTime.now();
        processingData.set("date_time", dateTime);
        LocalDate date = LocalDate.now();
        processingData.set("date", LocalDate.now());
        Map<String, Long> map = Map.of("one", 1L, "two", 2L);
        processingData.set("map", map);

        Map<String, LocalDate> mapD = Map.of("one", date);
        processingData.set("map_date", mapD);

        SomePojo pojo = new SomePojo();
        pojo.test = date;
        processingData.set("pojo", pojo);

        String string = processingData.toString();
        Assertions.assertNotNull(string);

        String json = processingData.toJson();
        Assertions.assertNotNull(json);

        ProcessingData result = ProcessingData.fromJson(json);

        Assertions.assertEquals(processingData.toJson(), result.toJson());
    }
}
