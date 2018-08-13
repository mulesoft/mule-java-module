/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import org.mule.runtime.api.metadata.MetadataKey;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class KeysMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  public void instanceMethodKeys() throws Exception {

    Set<MetadataKey> keys = getKeys(flow(INVOKE, EXECUTABLE_ELEMENT), INSTANCE_CATEGORY);
    assertThat(keys.iterator().next().getChilds().size(), is(21));
    assertThat(getIds(keys), containsInAnyOrder("getPhaseId()", "notify()", "getPhase()",
                                                "nextPhase()", "addToList(List)", "addToList(LinkedList)",
                                                "sayHi(String)", "wait()", "toString()",
                                                "sayHi()", "sayHi(int)", "addToList(ArrayList)",
                                                "equals(Object)", "wait(long)", "addToMap(Map)",
                                                "createEmptyPojo()", "notifyAll()", "wait(long,int)",
                                                "getClass()", "hashCode()", "sayHi(String,int)"));
  }

  @Test
  public void staticMethodKeys() throws Exception {
    Set<MetadataKey> keys = getKeys(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE + PHASE), STATIC_CATEGORY);
    assertThat(getIds(keys), containsInAnyOrder("create(Phase)", "create()", "className()",
                                                "throwException(String)", "getNull()"));
  }

  @Test
  public void constructorKeys() throws Exception {
    Set<MetadataKey> keys = getKeys(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO), CONSTRUCTOR_CATEGORY);
    assertThat(keys.iterator().next().getChilds().size(), is(4));
    assertThat(getIds(keys), containsInAnyOrder("CompositePojo()", "CompositePojo(CompositePojo)",
                                                "CompositePojo(String)", "CompositePojo(String,Map)"));

    keys = getKeys(flow(NEW, EXECUTABLE_ELEMENT), CONSTRUCTOR_CATEGORY);
    assertThat(keys.iterator().next().getChilds().size(), is(3));
    assertThat(getIds(keys), containsInAnyOrder("ExecutableElement()", "ExecutableElement(Phase)", "ExecutableElement(String)"));
  }

  private List<String> getIds(Set<MetadataKey> keys) {
    return keys.stream().flatMap(k -> k.getChilds().stream())
        .map(MetadataKey::getId)
        .collect(Collectors.toList());
  }

}
