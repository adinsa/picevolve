package com.github.adinsa.picevolve.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.github.adinsa.picevolve.cli.App.ExpressionIndexOutOfBoundsException;

/**
 * Simple utility that runs a CLI loop using command methods annotated with
 * {@link Command}.
 *
 * @author amar
 *
 */
public class CommandRunner {

    private final Object handler;
    private final Map<String, CommandMethod> commandMethods;

    @FunctionalInterface
    interface Parameter {
        Object getValue(Scanner scanner);
    }

    private final Map<Class<?>, Parameter> parameterTypeMap = new HashMap<>();

    {
        this.parameterTypeMap.put(int.class, (scanner) -> scanner.nextInt());
        this.parameterTypeMap.put(String.class, (scanner) -> scanner.next());
    }

    private static class CommandMethod {

        private final Method method;
        private final Command command;
        private final List<Parameter> parameters;

        public CommandMethod(final Method method, final Command command,
                final List<Parameter> parameters) {
            this.method = method;
            this.command = command;
            this.parameters = parameters;
        }

        public Command getCommand() {
            return this.command;
        }

        public void execute(final Object handler, final OutputStream os,
                final Scanner scanner) {
            final PrintStream writer = new PrintStream(os);
            final List<Object> params = new ArrayList<>();
            for (int i = 0; i < this.parameters.size(); i++) {
                writer.print(this.command.prompts()[i]);
                try {
                    params.add(this.parameters.get(i).getValue(scanner));
                } catch (final InputMismatchException e) {
                    writer.println("Invalid input");
                    scanner.next();
                    return;
                }
            }
            try {
                this.method.invoke(handler,
                        params.toArray(new Object[params.size()]));
            } catch (InvocationTargetException | IllegalAccessException
                    | IllegalArgumentException e) {
                if (e.getCause() != null && e
                        .getCause() instanceof ExpressionIndexOutOfBoundsException) {
                    writer.println(e.getCause().getMessage());
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public CommandRunner(final Object handler) {

        this.handler = handler;
        this.commandMethods = new HashMap<>();

        for (final Method method : this.getCommandMethods()) {
            final List<Parameter> params = new ArrayList<CommandRunner.Parameter>();
            final Command annotation = method.getAnnotation(Command.class);
            if (annotation.prompts().length != method.getParameterCount()) {
                throw new RuntimeException(String.format(
                        "Command method '%s' has %d prompts but %d parameters",
                        method.toString(), annotation.prompts().length,
                        method.getParameterCount()));
            }
            for (final Class<?> paramType : method.getParameterTypes()) {
                if (!this.parameterTypeMap.containsKey(paramType)) {
                    throw new RuntimeException(String.format(
                            "Unmapped parameter type '%s' for command method '%s'",
                            paramType.toString(), method.toString()));
                }
                params.add(this.parameterTypeMap.get(paramType));
            }
            this.commandMethods.put(method.getName(),
                    new CommandMethod(method, annotation, params));
        }
    }

    public void mainLoop(final InputStream is, final OutputStream os)
            throws IOException {

        final PrintStream printStream = new PrintStream(os);
        printStream.println("Enter '?' for help");

        try (final BufferedReader br = new BufferedReader(
                new InputStreamReader(is));
                final Scanner scanner = new Scanner(br);) {

            Optional<String> line = Optional.empty();
            do {
                printStream.print("--> ");
                line = Optional.ofNullable(br.readLine());
                if (this.commandMethods.containsKey(line.orElse("").trim())) {
                    this.commandMethods.get(line.get().trim())
                            .execute(this.handler, os, scanner);
                } else if (line.orElse("").trim().equals("?")) {
                    printStream.println(this.getHelp());
                }
            } while (line.isPresent());
        }
        printStream.println();
    }

    private String getHelp() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append("Commands:").append("\n");
        this.commandMethods.entrySet().stream()
                .forEach(entry -> sb
                        .append(String.format("%10s\t%s\n", entry.getKey(),
                                entry.getValue().getCommand().description())));
        return sb.toString();
    }

    private final List<Method> getCommandMethods() {
        return Arrays.stream(this.handler.getClass().getMethods())
                .filter(method -> Arrays.stream(method.getAnnotations())
                        .anyMatch(annotation -> annotation.annotationType()
                                .equals(Command.class)))
                .collect(Collectors.toList());
    }
}
