package com.github.adinsa.picevolve.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used by {@link CommandRunner} that provides command description and prompt messages.
 *
 * @author amar
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * Description of command used when displaying help info
     */
    String description();

    /**
     * The prompt messages to use when getting input for each of the command's parameters
     */
    String[] prompts() default {};
}