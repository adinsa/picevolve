package com.github.adinsa.picevolve.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adinsa.picevolve.PicEvolve;
import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.visitor.EvaluatorVisitor;

/**
 * Simple command line interface providing ability to save/load/delete image expressions to a text file.
 *
 * @author amar
 *
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private List<Expression> population;
    private final PicEvolve picEvolve;
    private final ExecutorService executor;
    private final Configuration configuration;

    public App() throws IOException {
        population = new ArrayList<>();
        picEvolve = new PicEvolve();
        configuration = new Configuration();

        final int numProcessors = Runtime.getRuntime().availableProcessors();
        logger.debug("availableProcessors: {}", numProcessors);
        executor = Executors.newFixedThreadPool(numProcessors);
    }

    public static void main(final String[] args) throws IOException, InterruptedException {

        final App app = new App();
        new CommandRunner(app).mainLoop(System.in, System.out);

        logger.info("Shutting down...");
        app.executor.shutdown();
        app.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Command(description = "Initialize a population of random images", prompts = { "Enter population size: " })
    public void init(final int populationSize) throws IOException {

        population = picEvolve.initializePopulation(populationSize);
        generateImages(population);
    }

    @Command(description = "Generate mutations of a parent image", prompts = { "Enter parent #: ", "Enter population size: " })
    public void mutate(final int parentId, final int populationSize) throws IOException {

        population = picEvolve.mutate(getExpression(parentId), populationSize);
        generateImages(population);
    }

    @Command(description = "Perform crossover between two parent images", prompts = { "Enter first parent #: ", "Enter second parent #: ",
            "Enter population size: " })
    public void crossover(final int momId, final int dadId, final int populationSize) throws IOException {

        population = picEvolve.crossover(getExpression(momId), getExpression(dadId), populationSize);
        generateImages(population);
    }

    @Command(description = "Generate higher resolution version of an image", prompts = { "Enter image #: ", "Enter width: ", "Enter height: ",
            "Enter filename: " })
    public void generate(final int expressionId, final int width, final int height, final String filename) {

        executor.submit(new EvaluationTask(new File(filename), getExpression(expressionId), width, height));
    }

    @Command(description = "Load saved image expressions")
    public void load() throws IOException {

        final File libraryFile = getLibraryFile();
        population = new ArrayList<>();
        try (FileReader reader = new FileReader(libraryFile)) {
            population = Files.readAllLines(libraryFile.toPath()).stream().map(exprStr -> picEvolve.parse(exprStr)).collect(Collectors.toList());
        }
        generateImages(population);
    }

    @Command(description = "Save an image expression", prompts = { "Enter image #: " })
    public void save(final int expressionId) throws IOException {

        try (FileWriter writer = new FileWriter(getLibraryFile(), true);) {
            writer.append(getExpression(expressionId).toString()).append('\n');
        }
    }

    @Command(description = "Delete saved image expression", prompts = { "Enter image #: " })
    public void delete(final int expressionId) throws IOException {

        final File libraryFile = getLibraryFile();
        final List<String> lines = Files.readAllLines(libraryFile.toPath()).stream()
                .filter(line -> !line.trim().equals(getExpression(expressionId).toString())).collect(Collectors.toList());

        try (FileWriter writer = new FileWriter(libraryFile, false)) {
            for (final String line : lines) {
                writer.append(line).append("\n");
            }
        }
    }

    static class ExpressionIndexOutOfBoundsException extends IndexOutOfBoundsException {

        private static final long serialVersionUID = -2161367907524405709L;

        public ExpressionIndexOutOfBoundsException(final String msg) {
            super(msg);
        }
    }

    private Expression getExpression(final int expressionIdx) {
        if (expressionIdx < 0 || expressionIdx >= population.size()) {
            throw new ExpressionIndexOutOfBoundsException(
                    String.format("Invalid expression #. Index: %d, Size: %d", expressionIdx, population.size()));
        }
        return population.get(expressionIdx);
    }

    private File getLibraryFile() throws IOException {

        final File file = new File(configuration.getLibraryFile());
        file.createNewFile();
        return file;
    }

    private File getImagesDirectory() throws IOException {

        final File imageDir = new File(configuration.getImagesDirectory());
        if (Files.notExists(imageDir.toPath())) {
            Files.createDirectory(imageDir.toPath());
        }
        return imageDir;
    }

    private void generateImages(final List<Expression> population) throws IOException {

        for (int i = 0; i < population.size(); i++) {
            logger.info("{}: {}", i, population.get(i));
            executor.submit(new EvaluationTask(new File(getImagesDirectory(), i + "." + configuration.getImageFormat()), population.get(i),
                    configuration.getPreviewWidth(), configuration.getPreviewHeight()));
        }
    }

    private class EvaluationTask implements Runnable {

        private final EvaluatorVisitor evaluator;
        private final Expression expression;
        private final File file;

        public EvaluationTask(final File file, final Expression expression, final int width, final int height) {
            evaluator = new EvaluatorVisitor(width, height);
            this.expression = expression;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                expression.accept(evaluator);
                evaluator.getImage().scaled().write(file, configuration.getImageFormat());
            } catch (final Throwable t) {
                logger.error("Error:", t);
                throw t;
            }
        }
    }
}
