/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.unit;


import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.util.JavaErrorUtils;
import org.mule.runtime.api.exception.TypedException;
import org.mule.runtime.api.message.Error;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JavaErrorUtilsTestCase {

  private static final Throwable NPE = new NullPointerException("Some NPE");
  private static final Throwable WITH_NPE_CAUSE = new RuntimeException("Some Runtime", NPE);

  @Mock
  private TypedException typedException;

  @Mock
  private Error error;

  @Test
  public void getCauseObtainsOnlyCausePresent() {
    Throwable cause = new WrongTypeModuleException("foo.Class", "bar.Type");
    setErrorCause(cause);
    Throwable actualCause = JavaErrorUtils.getCause(error);
    assertThat(actualCause, is(cause));
  }

  @Test
  public void getCauseObtainsNonMuleCause() {
    Throwable topCause = new ClassNotFoundModuleException("No class", WITH_NPE_CAUSE);
    setErrorCause(topCause);
    Throwable actualCause = JavaErrorUtils.getCause(error);
    assertThat(actualCause, is(WITH_NPE_CAUSE));
  }

  @Test
  public void getCauseObtainsNestedNonMuleCause() {
    when(typedException.getCause()).thenReturn(WITH_NPE_CAUSE);
    Throwable topCause = new ClassNotFoundModuleException("No class", typedException);
    setErrorCause(topCause);
    Throwable actualCause = JavaErrorUtils.getCause(error);
    assertThat(actualCause, is(WITH_NPE_CAUSE));
  }

  @Test
  public void getCauseUnwrapsReflectionInvocation() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    Throwable topCause = createInvocationException(invocationException);
    setErrorCause(topCause);
    Throwable actualCause = JavaErrorUtils.getCause(error);
    assertThat(actualCause, is(WITH_NPE_CAUSE));
  }

  @Test
  public void getRootCauseObtainsNestedCause() {
    when(typedException.getCause()).thenReturn(WITH_NPE_CAUSE);
    Throwable topCause = new ClassNotFoundModuleException("No class", typedException);
    setErrorCause(topCause);
    Throwable actualCause = JavaErrorUtils.getRootCause(error);
    assertThat(actualCause, is(NPE));
  }

  @Test
  public void getRootCauseUnwrapsReflectionInvocation() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    Throwable topCause = createInvocationException(invocationException);
    setErrorCause(topCause);
    Throwable actualCause = JavaErrorUtils.getRootCause(error);
    assertThat(actualCause, is(NPE));
  }

  @Test
  public void isCausedByIsTrueRootExactType() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    Throwable topCause = createInvocationException(invocationException);
    setErrorCause(topCause);
    boolean result = JavaErrorUtils.isCausedBy(error, NullPointerException.class, false);
    assertThat(result, is(true));
  }

  @Test
  public void isCausedByIsTrueTopExactType() {
    setErrorCause(WITH_NPE_CAUSE);
    boolean result = JavaErrorUtils.isCausedBy(error, RuntimeException.class, false);
    assertThat(result, is(true));
  }

  @Test
  public void isCausedByIsFalseRootExactType() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    Throwable topCause = createInvocationException(invocationException);
    setErrorCause(topCause);
    boolean result = JavaErrorUtils.isCausedBy(error, CustomNPException.class, false);
    assertThat(result, is(false));
  }

  @Test
  public void isCausedByIsTrueSubtype() {
    setErrorCause(new RuntimeException("Some Runtime", new CustomNPException()));
    boolean result = JavaErrorUtils.isCausedBy(error, NullPointerException.class, true);
    assertThat(result, is(true));
  }

  protected void setErrorCause(Throwable topCause) {
    when(error.getCause()).thenReturn(topCause);
  }

  protected Throwable createInvocationException(InvocationTargetException invocationException) {
    Executable executableMock = mock(Executable.class);
    when(executableMock.getParameterCount()).thenReturn(0);
    when(executableMock.getParameters()).thenReturn(new Parameter[] {});


    return new InvocationModuleException("", executableMock, emptyMap(),
                                         invocationException);
  }

  public static class CustomNPException extends NullPointerException {

  }
}
