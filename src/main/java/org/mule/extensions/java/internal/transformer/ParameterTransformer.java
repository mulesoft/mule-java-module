/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.transformer;

import static java.util.Optional.empty;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionExecutionException;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

/**
 * This class purpose is checking and transforming objects to a parameter type of a certain {@link Executable}
 *
 * When transforming an input into something that fits to an {@link Executable}'s parameter, this class will make a best effort in
 * order to make this transformation possible. In case that the transformation requires to transform a {@link Collection} or
 * {@link Map}, a new instance will be created for it.
 * 
 * @since 1.1
 */

public class ParameterTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTransformer.class);

  private static String TRANSFORMATION_SERVICE_ERROR_MESSAGE =
      "Transformation from type %s to %s could not be done using the Transformation Service";
  private static String DATAWEAVE_TRANSFORMATION_ERROR_MESSAGE =
      "Transformation from type %s to %s could not be done using a DataWeave Transformation";

  private static Class<? extends Map> DEFAULT_MAP_IMPLEMENTATION = HashMap.class;
  private static Class<? extends Collection> DEFAULT_COLLECTION_IMPLEMENTATION = LinkedList.class;

  private Executable executable;
  private TransformationService transformationService;
  private ExpressionManager expressionManager;


  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<Class<?>, Class<?>>() {

    {
      put(boolean.class, Boolean.class);
      put(byte.class, Byte.class);
      put(char.class, Character.class);
      put(double.class, Double.class);
      put(float.class, Float.class);
      put(int.class, Integer.class);
      put(long.class, Long.class);
      put(short.class, Short.class);
      put(void.class, Void.class);
    }
  };

  /**
   * @param executable The executable whose parameters will be checked against {@link Object}s.
   * @param transformationService An instance of a {@link TransformationService} use to tranform parameters.
   * @param expressionManager An instance of a {@link ExpressionManager} used to execute DataWeave expressions.
   */
  public ParameterTransformer(Executable executable, TransformationService transformationService,
                              ExpressionManager expressionManager) {
    this.executable = executable;
    this.transformationService = transformationService;
    this.expressionManager = expressionManager;
  }

  private <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }


  /**
   * Method that allows to check if a value fits a certain argument of the executable. Generic values will only be checked for
   * {@link Map} and {@link Collection} values.
   * 
   * @param value The value which type will be checked.
   * @param parameterIndex The index of the parameter in the executable arguments.
   * @return whether the value needs to be transformed in order to fit the parameter from the executable
   */
  public boolean parameterNeedsTransformation(Object value, int parameterIndex) {
    ResolvableType parameterResolvableType = getResolvableType(parameterIndex);
    return parameterNeedsTransformation(value, parameterResolvableType);
  }

  private boolean parameterNeedsTransformation(Object value, ResolvableType parameterResolvableType) {

    Class<?> wrappedParameterType = wrap(resolveType(parameterResolvableType));
    if (value == null) {
      return Optional.class.isAssignableFrom(wrappedParameterType);
    }

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

  /**
   * Method that makes a best efford to map an object to the type of a parameter of the excecutable. {@link Map} and
   * {@link Collection} will be replaced with new instances. Other object will remain the same instance if they do not need to be
   * transformed. For example, if a list is transformed, a new list will be created and will contain the same instances from the
   * former list that did not needed to be transformed and new instances for the ones being transformed.
   *
   * @param value The value that will be transformed.
   * @param parameterIndex The index of the parameter in the executable arguments.
   * @return The value transformed to fit the executable parameter.
   */
  public Optional<Object> transformParameter(Object value, int parameterIndex) {
    ResolvableType parameterResolvableType = getResolvableType(parameterIndex);
    return transformParameter(value, parameterResolvableType);
  }

  /**
   *
   * @param value the actual value assigned to the parameter
   * @param expectedType the expected type of the assigned parameter
   * @return
   */
  private Optional<Object> transformParameter(Object value, ResolvableType expectedType) {
    Class<?> wrappedParameterType = wrap(resolveType(expectedType));

    boolean isOptional = Optional.class.isAssignableFrom(wrappedParameterType);

    if (value == null) {
      return isOptional ? Optional.of(empty()) : empty();
    }

    if (mapResolutionNeeded(value, expectedType)) {
      return Optional.of(transformMap((Map) value, expectedType));
    }

    if (collectionResolutionNeeded(value, expectedType)) {
      return Optional.of(transformCollection((Collection) value, expectedType));
    }

    if (wrappedParameterType.isAssignableFrom(value.getClass())) {
      return Optional.of(value);
    } else if (isOptional) {
      Class<?> subjectClass = expectedType.hasGenerics() ? expectedType.getGeneric(0).resolve() : Object.class;
      if (subjectClass.isAssignableFrom(value.getClass())) {
        return Optional.of(Optional.of(value)); // (:-D
      }
    }

    Optional<Object> transformedValue = doServiceTransform(value, wrappedParameterType);
    if (transformedValue.isPresent()) {
      return transformedValue;
    }

    transformedValue = doExpressionTransform(value, wrappedParameterType);
    if (transformedValue.isPresent()) {
      return transformedValue;
    }

    // Parameters with generic types in some cases might work at runtime,
    // so we always do the best effort to perform the execution with the given value
    return hasGenerics(expectedType) ? Optional.of(value) : empty();
  }

  private boolean hasGenerics(ResolvableType parameterResolvableType) {
    return parameterResolvableType.hasGenerics() || parameterResolvableType.hasUnresolvableGenerics();
  }

  private Optional<Object> doServiceTransform(Object value, Class<?> wrappedParameterType) {
    try {
      Object transformedValue = transformationService.transform(value,
                                                                DataType.fromType(value.getClass()),
                                                                DataType.fromType(wrappedParameterType));
      if (wrappedParameterType.isAssignableFrom(transformedValue.getClass())) {
        return Optional.ofNullable(transformedValue);
      }
    } catch (Exception e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(String.format(TRANSFORMATION_SERVICE_ERROR_MESSAGE, value.getClass(), wrappedParameterType));
      }
    }

    return empty();
  }

  private Optional<Object> doExpressionTransform(Object value, Class<?> wrappedParameterType) {
    try {
      BindingContext context = BindingContext.builder().addBinding("payload", TypedValue.of(value)).build();
      Object expressionValue = expressionManager.evaluate("#[payload]",
                                                          DataType.fromType(wrappedParameterType),
                                                          context)
          .getValue();
      if (wrappedParameterType.isAssignableFrom(expressionValue.getClass())) {
        return Optional.ofNullable(expressionValue);
      }
    } catch (ExpressionExecutionException | ExpressionRuntimeException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(DATAWEAVE_TRANSFORMATION_ERROR_MESSAGE, value.getClass(), wrappedParameterType);
      }
    }
    return empty();
  }

  private Object transformCollection(Collection values, ResolvableType parameterResolvableType) {
    Collection collection = getCollectionImplementation(values, parameterResolvableType);

    for (Object originalItem : values) {
      Optional<Object> transformedItem = transformParameter(originalItem, parameterResolvableType.getGeneric(0));
      collection.add(transformedItem.orElse(originalItem));
    }
    return collection;
  }

  private Collection getCollectionImplementation(Collection value, ResolvableType parameterResolvableType) {
    Class<? extends Collection> valueClass = value.getClass();
    Class<? extends Collection> parameterClass = (Class<? extends Collection>) parameterResolvableType.resolve();
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
      Optional<Object> transformedKey = transformParameter(entry.getKey(), parameterResolvableType.getGeneric(0));
      Optional<Object> transformedValue = transformParameter(entry.getValue(), parameterResolvableType.getGeneric(1));

      map.put(transformedKey.orElse(entry.getKey()), transformedValue.orElse(entry.getValue()));
    }
    return map;
  }

  private Map getMapImplementation(Map value, ResolvableType parameterResolvableType) {
    Class<? extends Map> valueClass = value.getClass();
    Class<? extends Map> parameterClass = (Class<? extends Map>) parameterResolvableType.resolve();
    if (parameterClass.isAssignableFrom(valueClass)) {
      Map result = tryToCreateInstanse(valueClass);
      if (result != null) {
        return result;
      }
    }
    Map result = tryToCreateInstanse(parameterClass);
    if (result != null) {
      return result;
    }

    return tryToCreateInstanse(DEFAULT_MAP_IMPLEMENTATION);
  }

  private boolean mapResolutionNeeded(Object value, ResolvableType parameterResolvableType) {
    return value instanceof Map && Map.class.isAssignableFrom(resolveType(parameterResolvableType));
  }

  private boolean collectionResolutionNeeded(Object value, ResolvableType parameterResolvableType) {
    return value instanceof Collection && Collection.class.isAssignableFrom(resolveType(parameterResolvableType));
  }


  private ResolvableType getResolvableType(int parameterIndex) {
    if (executable instanceof Method) {
      return ResolvableType.forMethodParameter((Method) executable, parameterIndex);
    } else if (executable instanceof Constructor) {
      return ResolvableType.forConstructorParameter((Constructor) executable, parameterIndex);
    }
    throw new IllegalStateException("Failed when trying to retrieve Resolvable type from executable. " +
        "A 'Method' or 'Contructor' was expected, executable was a " +
        executable.getClass().getName());
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
