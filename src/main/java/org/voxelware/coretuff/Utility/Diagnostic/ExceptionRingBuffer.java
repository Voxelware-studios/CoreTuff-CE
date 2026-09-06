package org.voxelware.coretuff.Utility.Diagnostic;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExceptionRingBuffer {

    private static final int MAX_SIZE = 30;
    private final LinkedList<Record> records = new LinkedList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
            .withZone(ZoneId.systemDefault());

    public void record(Throwable throwable) {
        synchronized (records) {
            if (records.size() >= MAX_SIZE) {
                records.removeFirst();
            }
            StackTraceElement[] stack = throwable.getStackTrace();
            String source = stack.length > 0 ? stack[0].getClassName() + "." + stack[0].getMethodName() : "Unknown";
            records.add(new Record(
                    Instant.now(),
                    throwable.getClass().getName(),
                    source,
                    throwable.getMessage() != null ? throwable.getMessage() : "No message"
            ));
        }
    }

    public List<Record> snapshot() {
        synchronized (records) {
            return new ArrayList<>(records);
        }
    }

    public record Record(Instant timestamp, String type, String source, String message) {
        public String formatted() {
            return FORMATTER.format(timestamp) + " | " + type + " | " + source + " | " + message;
        }
    }
}
