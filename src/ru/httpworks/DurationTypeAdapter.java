package ru.httpworks;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.taskmanagment.TimeManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DurationTypeAdapter extends TypeAdapter<Duration> {
    public static final DateTimeFormatter timeFormatter = TimeManager.timeFormatter;

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        jsonWriter.value(TimeManager.duration2String(duration));
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        LocalTime localTime = LocalTime.parse(jsonReader.nextString(), timeFormatter);
        return Duration.ofMinutes(localTime.getHour() * 60 + localTime.getMinute());
    }
}
