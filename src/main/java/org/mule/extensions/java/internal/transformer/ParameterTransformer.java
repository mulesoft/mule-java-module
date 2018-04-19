/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.transformer;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionExecutionException;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @since 1.1
 */

public class ParameterTransformer {

  private static Class<? extends Map> DEFAULT_MAP_IMPLEMENTATION = LinkedHashMap.class;
  private static Class<? extends Collection> DEFAULT_COLLECTION_IMPLEMENTATION = LinkedList.class;

  private Executable executable;
  private TransformationService transformationService;
  private ExpressionManager expressionManager;

  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = initializePrimitiveToWrapperMap();

  private static Map<Class<?>, Class<?>> initializePrimitiveToWrapperMap() {
    Map<Class<?>, Class<?>> result = new HashMap<>();
    result.put(boolean.class, Boolean.class);
    result.put(byte.class, Byte.class);
    result.put(char.class, Character.class);
    result.put(double.class, Double.class);
    result.put(float.class, Float.class);
    result.put(int.class, Integer.class);
    result.put(long.class, Long.class);
    result.put(short.class, Short.class);
    result.put(void.class, Void.class);
    return result;
  }

  public ParameterTransformer(Executable executable, TransformationService transformationService,
                              ExpressionManager expressionManager) {
    this.executable = executable;
    this.transformationService = transformationService;
    this.expressionManager = expressionManager;
  }

  private <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }

  public boolean parameterNeedsTransformation(Object value, int parameterIndex) {
    ResolvableType parameterResolvableType = getResolvableType(parameterIndex);
    return parameterNeedsTransformation(value, parameterResolvableType);
  }

  public boolean parameterNeedsTransformation(Object value, ResolvableType parameterResolvableType) {
    Class<?> wrappedParameterType = wrap(resolveType(parameterResolvableType));
    if (wrappedParameterType.isAssignableFrom(value.getClass())) {
      if (Map.class.isAssignableFrom(value.getClass())) {
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
          if (parameterNeedsTransformation(entry.getKey(), parameterResolvableType.getGeneric(0))
              || parameterNeedsTransformation(entry.getValue(), parameterResolvableType.getGeneric(1))) {
            return true;
          }
        }
      } else if (Collection.class.isAssignableFrom(value.getClass())) {
        for (Object collectionItem : ((Collection) value)) {
          if (parameterNeedsTransformation(collectionItem, parameterResolvableType.getGeneric(0))) {
            return true;
          }
        }
      }
      return false;
    }
    return true;
  }

  public Object transformParameter(Object value, int parameterIndex) {
    ResolvableType parameterResolvableType = getResolvableType(parameterIndex);
    return transformParameter(value, parameterResolvableType);
  }

  public Object transformParameter(Object value, ResolvableType parameterResolvableType) {
    Class<?> wrappedParameterType = wrap(resolveType(parameterResolvableType));

    if (value == null) {
      return null;
    }

    if (mapResolutionNeeded(value, parameterResolvableType)) {
      return transformMap((Map) value, parameterResolvableType);
    }

    if (collectionResolutionNeeded(value, parameterResolvableType)) {
      return transformCollection((Collection) value, parameterResolvableType);
    }

    if (wrappedParameterType.isAssignableFrom(value.getClass())) {
      return value;
    }
    try {
      Object transformedValue = transformationService.transform(value,
                                                                DataType.fromType(value.getClass()),
                                                                DataType.fromType(wrappedParameterType));
      if (wrappedParameterType.isAssignableFrom(transformedValue.getClass())) {
        return transformedValue;
      }
    } catch (Exception e) {
      // The transformation could not be made using the transformation service
    }
    try {
      Object expressionValue = expressionManager.evaluate("#[payload]", DataType.fromType(wrappedParameterType),
                                                          BindingContext.builder().addBinding("payload", TypedValue.of(value))
                                                              .build())
          .getValue();
      if (wrappedParameterType.isAssignableFrom(expressionValue.getClass())) {
        return expressionValue;
      }
    } catch (ExpressionExecutionException e) {
      // The transformation could not be made using DataWeave
    }
    return value;
  }

  private Object transformCollection(Collection value, ResolvableType parameterResolvableType) {
    Collection collection = getCollectionImplementation(value, parameterResolvableType);

    for (Object item : value) {
      collection.add(transformParameter(item, parameterResolvableType.getGeneric(0)));
    }
    return collection;
  }

  private Collection getCollectionImplementation(Collection value, ResolvableType parameterResolvableType) {
    Class<? extends Collection> valueClass = value.getClass();
    Class<? extends Collection> parameterClass = (Class<? extends Collection>) parameterResolvableType.getRawClass();
    if (parameterClass.isAssignableFrom(valueClass)) {
      Collection result = tryToCreateInstanse(valueClass);
      if (result != null) {
        return result;
      }
    }
    Collection result = tryToCreateInstanse(parameterClass);
    if (result != null) {
      return result;
    }

    return tryToCreateInstanse(DEFAULT_COLLECTION_IMPLEMENTATION);
  }

  private Object transformMap(Map<Object, Object> value, ResolvableType parameterResolvableType) {
    Map map = getMapImplementation(value, parameterResolvableType);

    for (Map.Entry<Object, Object> entry : value.entrySet()) {
      map.put(transformParameter(entry.getKey(), parameterResolvableType.getGeneric(0)),
              transformParameter(entry.getValue(), parameterResolvableType.getGeneric(1)));
    }
    return map;
  }

  private Map getMapImplementation(Map value, ResolvableType parameterResolvableType) {
    Class<? extends Map> valueClass = value.getClass();
    Class<? extends Map> parameterClass = (Class<? extends Map>) parameterResolvableType.resolve();
    if (parameterClass.isAssignableFrom(valueClass)) {
      Map result = tryToCreateInstanse(valueClass);
      if (value != null) {
        return value;
      }
    }
    Map result = tryToCreateInstanse(parameterClass);
    if (value != null) {
      return value;
    }

    return tryToCreateInstanse(DEFAULT_MAP_IMPLEMENTATION);
  }

  private boolean mapResolutionNeeded(Object value, ResolvableType parameterResolvableType) {
    return value instanceof Map && Map.class.isAssignableFrom(parameterResolvableType.resolve());
  }

  private boolean collectionResolutionNeeded(Object value, ResolvableType parameterResolvableType) {
    return value instanceof Collection && Collection.class.isAssignableFrom(parameterResolvableType.resolve());
  }


  private ResolvableType getResolvableType(int parameterIndex) {
    if (executable instanceof Method) {
      return ResolvableType.forMethodParameter((Method) executable, parameterIndex);
    } else if (executable instanceof Constructor) {
      return ResolvableType.forConstructorParameter((Constructor) executable, parameterIndex);
    }
    throw new IllegalStateException();
  }

  private <T> T tryToCreateInstanse(Class<T> clazz) {
    try {
      T result = clazz.newInstance();
      return result;
    } catch (IllegalAccessException | InstantiationException e) {
      return null;
    }
  }

  private Class<?> resolveType(ResolvableType resolvableType) {
    Class<?> parameterTypeResolved = resolvableType.resolve();
    if (parameterTypeResolved == null) {
      parameterTypeResolved = Object.class;
    }
    return parameterTypeResolved;
  }

}
