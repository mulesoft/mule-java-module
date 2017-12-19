/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Executable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * //TODO
 */
public abstract class ExecutableIdentifier {

  private static final String METHOD_MASK = "%s(%s)";
  private static final String ARG_SEPARATOR = ",";
  private static final Pattern METHOD_MATCHER = Pattern.compile("(.+)\\((.*)\\)");
  // private static final String ARG_TYPE_SEPARATOR = ",";

  /**
   * @return the name of the declaring {@link Class} of {@code this} {@link Executable} element,
   * or empty string if it is unknown.
   */
  public abstract String getClazz();

  /**
   * Provides the ID of the {@link Executable} element being represented by
   * {@code this} {@link ExecutableIdentifierFactory}.
   * <p>
   * For example, for method {@code public void echo(String message, int times)},
   * the result of invoking this method will be {@code "echo#String,int"}.
   *
   * @return the ID of the {@link Executable} element being represented by
   * {@code this} {@link ExecutableIdentifierFactory}.
   */
  public abstract String getElementId();

  /**
   * Provides the name of the {@link Executable} element being represented by
   * {@code this} {@link ExecutableIdentifierFactory}.
   * <p>
   * For example, for method {@code public void echo(String message, int times)},
   * the result of invoking this method will be {@code "echo"}.
   *
   * @return the name of the {@link Executable} element being represented by
   * {@code this} {@link ExecutableIdentifierFactory}.
   */
  public String getElementName() {
    Matcher match = METHOD_MATCHER.matcher(getElementId().trim().replaceAll(" ", ""));
    return match.matches() ? match.group(1) : "";
  }
  //
  // /**
  //  * Provides a {@link List} with the {@link Class#getSimpleName} of each
  //  * {@link Parameter} of the {@link Executable} element.
  //  * <p>
  //  * For example, for method {@code public void echo(String message, int times)},
  //  * the result of invoking this method will be a {@link List} containing {@code ["String", "int"]}.
  //  * {@link Executable} elements without {@link Parameter}s will return an empty {@link List}.
  //  *
  //  * @return a {@link List} with the {@link Class#getSimpleName} of each
  //  * {@link Parameter} of the {@link Executable} element, or an empty {@link List}
  //  * if the {@link Executable} element has no {@link Parameter}s.
  //  */
  // public List<String> getArgumentTypeNames() {
  //   Matcher match = METHOD_MATCHER.matcher(getElementId().trim().replaceAll(" ", ""));
  //   return match.groupCount() > 1 ? Arrays.asList(match.group(2).split(ARG_TYPE_SEPARATOR)) : Collections.emptyList();
  // }

  /**
   * @return whether or not {@code this} identifier matches the given {@link ExecutableIdentifierFactory identifier}
   */
  public abstract boolean matches(Executable element);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ExecutableIdentifier)) {
      return false;
    }

    ExecutableIdentifier that = (ExecutableIdentifier) o;
    return this.getClazz().equals(that.getClazz())
        && this.getElementId().trim().replaceAll(" ", "")
            .equals(that.getElementId().trim().replaceAll(" ", ""));
  }

  @Override
  public int hashCode() {
    return (getClazz().hashCode() * 31) + getElementId().hashCode();
  }

  @Override
  public String toString() {
    return getClazz() + "::" + getElementId();
  }

  protected String buildId(String elementName, Class<?>[] argTypes) {
    return format(METHOD_MASK,
                  elementName.trim(),
                  stream(argTypes).map(Class::getSimpleName).collect(joining(ARG_SEPARATOR)));
  }

}
