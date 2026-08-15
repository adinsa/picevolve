package com.github.adinsa.picevolve.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        final List<String> names = new ArrayList<>();

        @Command(description = "Add two numbers", prompts = { "Enter a: ", "Enter b: " })
        public void add(final int a, final int b) {
            results.add(a + b);
        }

        @Command(description = "Greet a person", prompts = { "Enter name: " })
        public void greet(final String name) {
            names.add(name);
        }
    }

    public static class ThrowingHandler {

        @Command(description = "Throws an index exception", prompts = { "Enter i: " })
        public void boom(final int i) {
            throw new App.ExpressionIndexOutOfBoundsException("Invalid expression #");
        }
    }

    public static class BadPromptHandler {

        @Command(description = "Mismatched prompts", prompts = { "Enter a: " })
        public void bad(final int a, final int b) {
        }
    }

    public static class BadTypeHandler {

        @Command(description = "Unmapped parameter type", prompts = { "Enter d: " })
        public void bad(final double d) {
        }
    }

    private String run(final Object handler, final String input) throws IOException {
        final CommandRunner runner = new CommandRunner(handler);
        final ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        runner.mainLoop(in, out);
        return out.toString(StandardCharsets.UTF_8);
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

    @Test
    public void testHelpShowsPromptAndCommandDescriptions() throws IOException {

        final String output = run(new TestHandler(), "?\n");

        assertTrue(output.contains("Enter '?' for help"));
        assertTrue(output.contains("=> "));
        assertTrue(output.contains("Commands:"));
        assertTrue(output.contains("add"));
        assertTrue(output.contains("Add two numbers"));
    }

    @Test
    public void testStringParameterCommand() throws IOException {

        final TestHandler handler = new TestHandler();
        final String output = run(handler, "greet\nworld\n");

        assertEquals(List.of("world"), handler.names);
        assertTrue(output.contains("Enter name: "));
    }

    @Test
    public void testUnrecognizedCommand() throws IOException {

        final String output = run(new TestHandler(), "nonsense\n");

        assertTrue(output.contains("Unrecognized command"));
    }

    @Test
    public void testInvalidInputIsReported() throws IOException {

        final TestHandler handler = new TestHandler();
        final String output = run(handler, "add\nfoo\n2\n");

        assertTrue(output.contains("Invalid input"));
        assertTrue(handler.results.isEmpty());
    }

    @Test
    public void testEmptyLineIsIgnored() throws IOException {

        final TestHandler handler = new TestHandler();
        run(handler, "\n\nadd\n1\n2\n");

        assertEquals(List.of(3), handler.results);
    }

    @Test
    public void testExpressionIndexOutOfBoundsExceptionIsReported() throws IOException {

        final String output = run(new ThrowingHandler(), "boom\n1\n");

        assertTrue(output.contains("Invalid expression #"));
    }

    @Test
    public void testMismatchedPromptCountThrows() {

        assertThrows(RuntimeException.class, () -> new CommandRunner(new BadPromptHandler()));
    }

    @Test
    public void testUnmappedParameterTypeThrows() {

        assertThrows(RuntimeException.class, () -> new CommandRunner(new BadTypeHandler()));
    }

    @Test
    public void testMainLoopTerminatesOnEndOfInput() throws IOException {

        final TestHandler handler = new TestHandler();
        run(handler, "add\n1\n2\n");

        assertEquals(List.of(3), handler.results);
    }
}
