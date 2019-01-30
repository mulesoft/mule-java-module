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
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.internal.util.JavaExceptionUtils;
import org.mule.runtime.api.exception.TypedException;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JavaErrorUtilsTestCase {

  private static final Throwable NPE = new NullPointerException("Some NPE");
  private static final Throwable WITH_NPE_CAUSE = new RuntimeException("Some Runtime", NPE);

  @Mock
  private TypedException typedException;

  @Test
  public void getRootCauseObtainsNestedCause() {
    when(typedException.getCause()).thenReturn(WITH_NPE_CAUSE);
    Throwable actualCause = JavaExceptionUtils.getRootCause(new ClassNotFoundModuleException("No class", typedException));
    assertThat(actualCause, is(NPE));
  }

  @Test
  public void getRootCauseUnwrapsReflectionInvocation() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    Throwable actualCause = JavaExceptionUtils.getRootCause(createInvocationException(invocationException));
    assertThat(actualCause, is(NPE));
  }

  @Test
  public void isCausedByIsTrueRootExactType() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    boolean result = JavaExceptionUtils.isCausedBy(createInvocationException(invocationException),
                                                   NullPointerException.class, false);
    assertThat(result, is(true));
  }

  @Test
  public void isCausedByIsTrueTopExactType() {
    boolean result = JavaExceptionUtils.isCausedBy(WITH_NPE_CAUSE, RuntimeException.class, false);
    assertThat(result, is(true));
  }

  @Test
  public void isCausedByIsFalseRootExactType() {
    InvocationTargetException invocationException = new InvocationTargetException(WITH_NPE_CAUSE);
    boolean result = JavaExceptionUtils.isCausedBy(createInvocationException(invocationException),
                                                   CustomNPException.class, false);
    assertThat(result, is(false));
  }

  @Test
  public void isCausedByIsTrueSubtype() {
    RuntimeException runtimeException = new RuntimeException("Some Runtime", new CustomNPException());
    boolean result = JavaExceptionUtils.isCausedBy(runtimeException, NullPointerException.class, true);
    assertThat(result, is(true));
  }

  private Throwable createInvocationException(InvocationTargetException invocationException) {
    Executable executableMock = mock(Executable.class, new MockSettingsImpl().defaultAnswer(RETURNS_DEFAULTS).lenient());
    when(executableMock.getParameterCount()).thenReturn(0);
    when(executableMock.getParameters()).thenReturn(new Parameter[] {});


    return new InvocationModuleException("", executableMock, emptyMap(),
                                         invocationException);
  }

  public static class CustomNPException extends NullPointerException {

  }
}
