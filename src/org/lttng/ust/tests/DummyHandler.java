package org.lttng.ust.tests;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class DummyHandler extends Handler {

    public DummyHandler() {
        super();
    }

    @Override
    public void close() throws SecurityException {}

    @Override
    public void flush() {}

    @Override
    public void publish(LogRecord record) {}

}
