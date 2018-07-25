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
 * A unique identifier for a given {@link Executable} element
 *
 * @since 1.0
 */
public abstract class ExecutableIdentifier {

  private static final String METHOD_MASK = "%s(%s)";
  private static final String ARG_SEPARATOR = ",";
  private static final Pattern METHOD_MATCHER = Pattern.compile("(.+)\\((.*)\\)");

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
                  stream(argTypes).map(Class::getSimpleName).collect(joining(ARG_SEPARATOR)));
  }

  private String sanitize(String id) {
    return id.trim().replaceAll(" ", "");
  }

}
