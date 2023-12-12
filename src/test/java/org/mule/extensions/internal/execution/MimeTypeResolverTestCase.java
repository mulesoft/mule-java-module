/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;

import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.extensions.java.internal.util.MethodInvoker;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MimeTypeResolverTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeResolverTestCase.class);
  private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
  private static final MediaType DEFAULT_MEDIATYPE = MediaType.ANY;
  private static final String APPLICATION_JSON = "application/json";
  private static final String UTF_16 = "UTF-16";

  @Mock
  private TransformationService transformationService;

  @Mock
  private ExpressionManager expressionManager;

  private ExecutableElement instance = new ExecutableElement();
  private Method method;

  @Before
  public void setUp() throws Exception {
    method = ExecutableElement.class.getMethod("sayHi");
  }

  @Test
  public void resolvesMimeTypeWithEncoding() {
    MediaType expectedMimeType = MediaType.parse(APPLICATION_JSON);
    Result<Object, Void> result =
        MethodInvoker.invokeMethod(method, Collections.emptyMap(), instance, new MethodIdentifier(method),
                                   APPLICATION_JSON, UTF_16,
                                   transformationService, expressionManager, LOGGER, DEFAULT_CHARSET.name());

    Optional<MediaType> mediaType = result.getMediaType();
    assertThat(mediaType.isPresent(), Is.is(true));
    assertThat(mediaType.get().getPrimaryType(), Is.is(expectedMimeType.getPrimaryType()));
    assertThat(mediaType.get().getSubType(), Is.is(expectedMimeType.getSubType()));
    assertThat(mediaType.get().getCharset().isPresent(), Is.is(true));
    assertThat(mediaType.get().getCharset().get().name(), Is.is(UTF_16));
  }

  @Test
  public void resolvesMimeTypeOnly() {
    MediaType expectedMimeType = MediaType.parse(APPLICATION_JSON);
    Result<Object, Void> result =
        MethodInvoker.invokeMethod(method, Collections.emptyMap(), instance, new MethodIdentifier(method),
                                   APPLICATION_JSON, null,
                                   transformationService, expressionManager, LOGGER, DEFAULT_CHARSET.name());

    Optional<MediaType> mediaType = result.getMediaType();
    assertThat(mediaType.isPresent(), Is.is(true));
    assertThat(mediaType.get().getPrimaryType(), Is.is(expectedMimeType.getPrimaryType()));
    assertThat(mediaType.get().getSubType(), Is.is(expectedMimeType.getSubType()));
    assertThat(mediaType.get().getCharset().isPresent(), Is.is(true));
    assertThat(mediaType.get().getCharset().get(), Is.is(DEFAULT_CHARSET));
  }

  @Test
  public void resolvesEncodingOnly() {
    Result<Object, Void> result =
        MethodInvoker.invokeMethod(method, Collections.emptyMap(), instance, new MethodIdentifier(method),
                                   null, UTF_16,
                                   transformationService, expressionManager, LOGGER, DEFAULT_CHARSET.name());

    Optional<MediaType> mediaType = result.getMediaType();
    assertThat(mediaType.isPresent(), Is.is(true));
    assertThat(mediaType.get().getPrimaryType(), Is.is(DEFAULT_MEDIATYPE.getPrimaryType()));
    assertThat(mediaType.get().getSubType(), Is.is(DEFAULT_MEDIATYPE.getSubType()));
    assertThat(mediaType.get().getCharset().isPresent(), Is.is(true));
    assertThat(mediaType.get().getCharset().get().name(), Is.is(UTF_16));
  }

  @Test
  public void noMimeTypeNorEncodingOverride() {
    Result<Object, Void> result =
        MethodInvoker.invokeMethod(method, Collections.emptyMap(), instance, new MethodIdentifier(method),
                                   "", "",
                                   transformationService, expressionManager, LOGGER, DEFAULT_CHARSET.name());

    assertThat(result.getMediaType().isPresent(), Is.is(false));
  }
}
