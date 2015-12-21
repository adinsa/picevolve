package com.github.adinsa.picevolve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String LIBRARY_FILE = ".library.txt";
    private static final int PREVIEW_WIDTH = 200;
    private static final int PREVIEW_HEIGHT = 200;

    private static int numProcessors;

    static {
        numProcessors = Runtime.getRuntime().availableProcessors();
        logger.debug("availableProcessors: {}", numProcessors);
    }

    public static void main(final String[] args) throws IOException {

        final App app = new App();
        app.run();
    }

    private void run() throws IOException {

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
                Scanner scanner = new Scanner(br);) {

            List<Expression> population = new ArrayList<>();
            final CommandFactory cmdFactory = CommandFactory.instance(scanner);
            String line = null;
            do {
                System.out.print("Enter command (or 'help'): ");
                line = br.readLine();
                final Command cmd = cmdFactory.getCommand(line);
                if (cmd != null) {
                    population = cmd.execute(population);
                } else if (Optional.ofNullable(line).orElse("")
                        .equalsIgnoreCase("help")) {
                    cmdFactory.printHelp();
                }
            } while (line != null);

        }
    }

    private static void generateImages(final List<Expression> population) {

        final ExecutorService service = Executors
                .newFixedThreadPool(numProcessors);
        for (int i = 0; i < population.size(); i++) {
            System.out.println(i + ": " + population.get(i));
            service.submit(new EvaluationTask(String.valueOf(i),
                    population.get(i), PREVIEW_WIDTH, PREVIEW_HEIGHT));
        }
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            logger.error("Error: ", e);
            throw new RuntimeException(e);
        }
    }

    private static Expression promptForExpression(final String message,
            final Scanner scanner, final List<Expression> population) {

        while (true) {
            System.out.print(message);
            final int idx = scanner.nextInt();
            if (idx >= 0 && idx < population.size()) {
                return population.get(idx);
            } else {
                System.err.println("Invalid Id");
            }
        }
    }

    private static class EvaluationTask implements Runnable {

        private final EvaluatorVisitor evaluator;
        private final Expression expression;

        private final String filename;

        public EvaluationTask(final String filename,
                final Expression expression, final int width,
                final int height) {
            this.evaluator = new EvaluatorVisitor(width, height);
            this.expression = expression;
            this.filename = filename;
        }

        @Override
        public void run() {
            this.expression.accept(this.evaluator);
            this.evaluator.getImage().scaled().write(this.filename);
        }
    }

    private static abstract class Command {

        protected final Scanner scanner;
        protected final PicEvolve picEvolve;

        Command(final Scanner scanner) {
            this.scanner = scanner;
            this.picEvolve = new PicEvolve();
        }

        public abstract List<Expression> execute(
                final List<Expression> population);
    }

    private static class InitializeCommand extends Command {

        InitializeCommand(final Scanner scanner) {
            super(scanner);
        }

        @Override
        public List<Expression> execute(final List<Expression> population) {
            System.out.print("Enter population size: ");
            final int popSize = this.scanner.nextInt();
            final List<Expression> newPopulation = this.picEvolve
                    .initializePopulation(popSize);
            generateImages(newPopulation);
            return newPopulation;
        }
    }

    private static class MutateCommand extends Command {

        MutateCommand(final Scanner scanner) {
            super(scanner);
        }

        @Override
        public List<Expression> execute(final List<Expression> population) {
            final Expression parent = promptForExpression("Enter parent Id: ",
                    this.scanner, population);
            System.out.print("Enter population size: ");
            final int popSize = this.scanner.nextInt();
            final List<Expression> newPopulation = this.picEvolve.mutate(parent,
                    popSize);
            generateImages(newPopulation);
            return newPopulation;
        }
    }

    private static class CrossoverCommand extends Command {

        CrossoverCommand(final Scanner scanner) {
            super(scanner);
        }

        @Override
        public List<Expression> execute(final List<Expression> population) {
            final Expression mom = promptForExpression(
                    "Enter first parent Id: ", this.scanner, population);
            final Expression dad = promptForExpression(
                    "Enter second parent Id: ", this.scanner, population);
            System.out.print("Enter population size: ");
            final int popSize = this.scanner.nextInt();

            final List<Expression> newPopulation = this.picEvolve.crossover(mom,
                    dad, popSize);
            generateImages(newPopulation);
            return newPopulation;
        }
    }

    private static class SaveCommand extends Command {

        SaveCommand(final Scanner scanner) {
            super(scanner);
        }

        @Override
        public List<Expression> execute(final List<Expression> population) {

            final String exprString = promptForExpression(
                    "Enter expression Id: ", this.scanner, population)
                            .toString();

            final File file = new File(LIBRARY_FILE);

            try {
                file.createNewFile();
            } catch (final IOException e) {
                logger.error("Error creating file {}: ", LIBRARY_FILE, e);
                throw new RuntimeException(e);
            }

            try (FileWriter writer = new FileWriter(file, true);) {
                writer.append(exprString).append('\n');
            } catch (final IOException e) {
                logger.error("Error writing to {}: ", LIBRARY_FILE, e);
                throw new RuntimeException(e);
            }

            return population;
        }
    }

    private static class LoadCommand extends Command {

        LoadCommand(final Scanner scanner) {
            super(scanner);
        }

        @Override
        public List<Expression> execute(final List<Expression> population) {

            List<Expression> newPopulation = new ArrayList<>();
            final File file = new File(LIBRARY_FILE);
            try (FileReader reader = new FileReader(file)) {
                newPopulation = Files.readAllLines(file.toPath()).stream()
                        .map(exprStr -> this.picEvolve.parse(exprStr))
                        .collect(Collectors.toList());
            } catch (final IOException e) {
                logger.error("Error loading file {}: ", LIBRARY_FILE, e);
                throw new RuntimeException(e);
            }
            generateImages(newPopulation);
            return newPopulation;
        }
    }

    private static class GenerateCommand extends Command {

        GenerateCommand(final Scanner scanner) {
            super(scanner);
        }

        @Override
        public List<Expression> execute(final List<Expression> population) {

            final Expression expression = promptForExpression(
                    "Enter expression Id: ", this.scanner, population);
            System.out.print("Enter image width: ");
            final int width = this.scanner.nextInt();
            System.out.print("Enter image height: ");
            final int height = this.scanner.nextInt();
            System.out.print("Enter filename: ");
            final String filename = this.scanner.next();

            final Image image = this.picEvolve.evaluate(expression, width,
                    height);
            image.write(filename);

            return population;
        }
    }

    private static class CommandFactory {

        private final Map<String, Command> commands;
        private final Map<String, String> helpMessages;

        private CommandFactory() {
            this.commands = new HashMap<>();
            this.helpMessages = new HashMap<>();
        }

        private void addCommand(final String name, final String messsage,
                final Command command) {
            this.commands.put(name, command);
            this.helpMessages.put(name, messsage);
        }

        public Command getCommand(final String name) {
            return this.commands.get(name);
        }

        public void printHelp() {
            System.out.println("\nCommands:");
            for (final Entry<String, String> entry : this.helpMessages
                    .entrySet()) {
                System.out.println(String.format("%10s\t%s", entry.getKey(),
                        entry.getValue()));
            }
            System.out.println();
        }

        public static CommandFactory instance(final Scanner scanner) {

            final CommandFactory factory = new CommandFactory();

            factory.addCommand("init",
                    "Initialize new population of random images",
                    new InitializeCommand(scanner));
            factory.addCommand("mutate", "Generate mutations of a parent image",
                    new MutateCommand(scanner));
            factory.addCommand("crossover",
                    "Perform crossover between two parent images",
                    new CrossoverCommand(scanner));
            factory.addCommand("generate",
                    "Generate higher resolution version of an image",
                    new GenerateCommand(scanner));
            factory.addCommand("save", "Save an image expression",
                    new SaveCommand(scanner));
            factory.addCommand("load", "Load saved image expressions",
                    new LoadCommand(scanner));

            return factory;
        }
    }
}