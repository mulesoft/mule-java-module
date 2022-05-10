/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.integration;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class JavaPluginIntegrationTestCase extends JavaModuleAbstractTestCase {

  private static final List<String> CAR_DUMP_NAMES = of("doors", "wheels", "windows", "engine");

  @Override
  protected String getConfigFile() {
    return "java-plugins-integration.xml";
  }

  @Test
  public void createEmptyPojo() throws Exception {
    Map<String, Object> result = (Map<String, Object>) flowRunner("createEmptyPojo").run().getMessage().getPayload().getValue();
    assertThat(result, is(notNullValue()));
    for (String name : CAR_DUMP_NAMES) {
      assertThat("Unexpected property " + name, result.get(name), is(nullValue()));
    }
  }

  @Test
  public void createInitialisedPojo() throws Exception {
    Map<String, Object> result =
        (Map<String, Object>) flowRunner("createInitialisedPojo").run().getMessage().getPayload().getValue();
    assertThat(result, is(notNullValue()));
    for (String name : CAR_DUMP_NAMES) {
      assertThat("Missing property " + name, result.get(name), is(notNullValue()));
    }

    assertThat(result.get("doors"), is("doors"));

  }
}
