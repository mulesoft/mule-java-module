/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.cache;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.internal.cache.JavaModuleLoadingCacheUtils.getPublicMethods;

import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.NoSuchConstructorModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.internal.parameters.ConstructorIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread safe loading cache implementation for {@link Class} and {@link Executable} elements, using its {@link Class#getName()}
 * and {@link ExecutableIdentifier} as keys respectively. This cache is intended to be used every time a given {@link Class}
 * {@link Constructor} or {@link Method} has to be loaded using reflection.
 *
 * @since 1.0
 */
public final class JavaModuleLoadingCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaModuleLoadingCache.class);

    private final String METHOD_IDENTIFIER = "%s#%s";

    private final Map<String, Class<?>> typesCache = new ConcurrentHashMap<>();
    private final Map<String, List<Constructor>> constructorsCache = new ConcurrentHashMap<>();
    private final Map<String, List<Method>> classMethodsCache = new ConcurrentHashMap<>();
    private final Map<String, List<Method>> instanceMethodsCache = new ConcurrentHashMap<>();
    private final Map<String, String> ambiguousExecutableWarningMessages = new ConcurrentHashMap<>();

    public Class<?> loadClass(String className) {
        return typesCache.computeIfAbsent(className, key -> {
            try {
                return ClassUtils.loadClass(className, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundModuleException(e.getMessage(), e);
            }
        });
    }

    public Constructor getConstructor(ConstructorIdentifier id, Class<?> declaringClass,
                                      Map<String, TypedValue<Object>> args) {
        List<Constructor> constructors =
                constructorsCache.computeIfAbsent(String.format(METHOD_IDENTIFIER, declaringClass.getName(), id.getElementId()),
                        key -> stream(declaringClass.getConstructors())
                                .filter(c -> isPublic(c.getModifiers()))
                                .filter(id::matches)
                                .collect(toList()));

        verifyExecutables(id, declaringClass, args, constructors);

        return constructors.get(0);
    }

    public Method getMethod(ExecutableIdentifier id, Class<?> clazz, Map<String, TypedValue<Object>> args, boolean expectStatic) {
        Map<String, List<Method>> methodsCache = expectStatic ? classMethodsCache : instanceMethodsCache;

        List<Method> methods = methodsCache.computeIfAbsent(String.format(METHOD_IDENTIFIER, clazz.getName(), id.getElementId()),
                key -> getPublicMethods(clazz, expectStatic)
                        .stream()
                        .filter(id::matches)
                        .collect(toList()));

        verifyExecutables(id, clazz, args, methods);

        return methods.get(0);
    }

    public void verifyExecutables(ExecutableIdentifier id, Class<?> declaringClass, Map<String, TypedValue<Object>> args,
                                  List<? extends Executable> executables) {
        if (executables.size() == 0) {
            if (id instanceof ConstructorIdentifier) {
                throw new NoSuchConstructorModuleException(id, declaringClass, args);
            } else {
                throw new NoSuchMethodModuleException(id, declaringClass, args);
            }
        } else if (executables.size() > 1) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(ambiguousExecutableWarningMessages
                        .computeIfAbsent(String.format(METHOD_IDENTIFIER, declaringClass.getName(), id.getElementId()),
                                key -> constructWarningMessage(executables, id.getExecutableTypeName(), key)));
            }
        }
    }

    private String constructWarningMessage(List<? extends Executable> executables, String category, String executableId) {
        String possibleExecutables = join(", ", executables.stream().map(ExecutableIdentifierFactory::create)
                .map(ExecutableIdentifier::getElementId).collect(toList()));
        StringBuilder sb = new StringBuilder();

        sb.append(format("There where multiple %ss found with the id \"%s\"", category, executableId));
        sb.append(format("The %s %s will be executed.\n", category, executables.get(0)));
        sb.append(format("The Possible %ss that share the same ID are [ %s ].\n", category, possibleExecutables));
        sb.append("To use a specific one, use the fully-qualified name for the argument types.");

        return sb.toString();
    }

}
