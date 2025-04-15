/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A unique identifier for a given {@link Executable} element
 *
 * @since 1.0
 */
public abstract class ExecutableIdentifier {

  private static final String METHOD_MASK = "%s(%s)";
  private static final String ARG_SEPARATOR = ",";
  private static final Pattern METHOD_MATCHER = Pattern.compile("(.+)\\((.*)\\)");

  /**
   * @return the name of the declaring {@link Class} of {@code this} {@link Executable} element, or empty string if it is unknown.
   */
  public abstract String getClazz();

  /**
   * Provides the ID of the {@link Executable} element being represented by {@code this} {@link ExecutableIdentifierFactory}.
   * <p>
   * For example, for method {@code public void echo(String message, int times)}, the result of invoking this method will be
   * {@code "echo#String,int"}.
   *
   * @return the ID of the {@link Executable} element being represented by {@code this} {@link ExecutableIdentifierFactory}.
   */
  public abstract String getElementId();

  /**
   * Provides the name of the {@link Executable} element being represented by {@code this} {@link ExecutableIdentifierFactory}.
   * <p>
   * For example, for method {@code public void echo(String message, int times)}, the result of invoking this method will be
   * {@code "echo"}.
   *
   * @return the name of the {@link Executable} element being represented by {@code this} {@link ExecutableIdentifierFactory}.
   */
  public String getElementName() {
    Matcher match = METHOD_MATCHER.matcher(getElementId().trim().replaceAll(" ", ""));
    return match.matches() ? match.group(1) : "";
  }

  /**
   * Provides the type name of the {@link Executable} arguments being represented by {@code this}
   * {@link ExecutableIdentifierFactory}.
   * <p>
   * For example, for method {@code public void echo(String message, int times)}, the result of invoking this method will be
   * {@code ["String", "int"]}.
   *
   * @return the type name of the {@link Executable} arguments element being represented by {@code this}
   *         {@link ExecutableIdentifierFactory}. if the id of {@code this} {@link ExecutableIdentifierFactory} is invalid,
   *         {@code empty()} will be returned.
   */
  public Optional<String[]> getArgumentTypeNames() {
    Matcher match = METHOD_MATCHER.matcher(getElementId().trim().replaceAll(" ", ""));
    if (match.matches()) {
      String[] argumentTypeNames = match.group(2).split(ARG_SEPARATOR);
      if (argumentTypeNames.length == 1 && argumentTypeNames[0].equals("")) {
        return of(new String[0]);
      } else {
        return of(argumentTypeNames);
      }
    }
    return empty();
  }

  /**
   * @return whether or not {@code this} identifier matches the given {@link ExecutableIdentifierFactory identifier}
   */
  public abstract boolean matches(Executable element);

  /**
   * @return the name of the executable element type
   */
  public abstract String getExecutableTypeName();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ExecutableIdentifier)) {
      return false;
    }

    ExecutableIdentifier that = (ExecutableIdentifier) o;
    return this.getClazz().equals(that.getClazz()) && sanitize(getElementId()).equals(sanitize(that.getElementId()));
  }

  @Override
  public int hashCode() {
    return (getClazz().hashCode() * 31) + sanitize(getElementId()).hashCode();
  }

  @Override
  public String toString() {
    return getClazz() + "::" + getElementId();
  }

  protected String buildId(String elementName, Class<?>[] argTypes) {
    return format(METHOD_MASK,
                  elementName.trim(),
                  stream(argTypes).map(Class::getCanonicalName).collect(joining(ARG_SEPARATOR)));
  }

  protected boolean matchesArguments(Class[] arguments) {
    Optional<String[]> optionalArgumentTypeNames = getArgumentTypeNames();
    if (!optionalArgumentTypeNames.isPresent()) {
      return false;
    }
    String[] argumentTypeNames = optionalArgumentTypeNames.get();

    if (arguments.length != argumentTypeNames.length) {
      return false;
    }

    for (int i = 0; i < arguments.length; i++) {
      if (!argumentTypeNames[i].equals(arguments[i].getSimpleName())
          && !argumentTypeNames[i].equals(arguments[i].getCanonicalName())
          && !argumentTypeNames[i].equals(arguments[i].getName())) {
        return false;
      }
    }

    return true;
  }

  private String sanitize(String id) {
    return id.trim().replaceAll(" ", "");
  }

}
