/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jersey.java8;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * Provider of {@link javax.ws.rs.ext.ParamConverter param converter} that convert the supplied string into a Java 8
 * {@link java.util.Optional} instance. Conversion is actually done by one of the available param converters able to process type
 * wrapped into Optional.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @since 2.18
 */
@Singleton
final class OptionalParamConverterProvider implements ParamConverterProvider {

    private final Value<Set<ParamConverterProvider>> providers;

    @Inject
    public OptionalParamConverterProvider(final ServiceLocator locator) {
        this.providers = Values.lazy((Value<Set<ParamConverterProvider>>)
                () -> Providers.getProviders(locator, ParamConverterProvider.class));
    }

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> raw, final Type generic, final Annotation[] annotations) {
        if (raw.equals(Optional.class)) {
            final ClassTypePair typePair = Utils.optionalParamType(generic);

            // Provide String Optional in case String is desired or Optional type is not defined.
            if (typePair.rawClass().equals(String.class)) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(final String value) {
                        return raw.cast(Optional.ofNullable(value));
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public String toString(final T value) {
                        return ((Optional<Object>) value).map(Object::toString).orElse(null);
                    }
                };
            }

            // Check other param converter providers whether they can process the required type.
            for (final ParamConverterProvider provider : providers.get()) {
                final ParamConverter converter = provider.getConverter(typePair.rawClass(), typePair.type(), annotations);

                if (converter != null) {
                    return new ParamConverter<T>() {
                        @Override
                        public T fromString(final String value) {
                            return raw.cast(Optional.ofNullable(value).map(converter::fromString));
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public String toString(final T value) {
                            return ((Optional<Object>) value).map(converter::toString).orElse(null);
                        }
                    };
                }
            }
        }

        return null;
    }
}
