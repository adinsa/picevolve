package com.github.adinsa.picevolve.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandRunnerTest {

    public static class TestHandler {

        final List<Integer> results = new ArrayList<>();

        @Command(description = "Add two numbers", prompts = { "Enter a: ", "Enter b: " })
        public void add(final int a, final int b) {
            results.add(a + b);
        }
    }

    @Test
    public void testMainLoopProcessesMultipleCommandsFromPipedInput() throws IOException {

        final TestHandler handler = new TestHandler();
        final CommandRunner runner = new CommandRunner(handler);

        final String input = "add\n1\n2\n?\n";
        final ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        runner.mainLoop(in, out);

        final String output = out.toString(StandardCharsets.UTF_8);

        assertEquals(List.of(3), handler.results, "Command arguments should be read from piped input");
        assertTrue(output.contains("Commands:"), "Help text should be displayed for '?' command after earlier command");
    }
}
