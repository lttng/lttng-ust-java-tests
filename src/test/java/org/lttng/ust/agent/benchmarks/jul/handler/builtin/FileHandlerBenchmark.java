package org.lttng.ust.agent.benchmarks.jul.handler.builtin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;

public class FileHandlerBenchmark extends JulHandlerBenchmarkBase {

    private Path outputFile;

    @Before
    public void testSetup() throws SecurityException, IOException {
        outputFile = Files.createTempFile(this.getClass().getSimpleName(), null);

        handler = new FileHandler(outputFile.toString(), false);
        handler.setFormatter(new SimpleFormatter());
    }

    @After
    public void testTeardown() throws IOException {
        Files.deleteIfExists(outputFile);
    }
}
