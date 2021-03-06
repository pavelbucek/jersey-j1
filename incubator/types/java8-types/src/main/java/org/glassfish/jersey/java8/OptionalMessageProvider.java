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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.MessageBodyReader;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * {@link javax.ws.rs.ext.MessageBodyReader Reader} for {@link java.util.Optional} type. The provider extracts the value and
 * tries to find provider for that type.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @since 2.18
 */
@Singleton
final class OptionalMessageProvider implements MessageBodyReader<Optional> {

    private static final Logger LOGGER = Logger.getLogger(OptionalMessageProvider.class.getName());

    @Inject
    private Provider<MessageBodyWorkers> workers;

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return type == Optional.class;
    }

    @Override
    public Optional readFrom(final Class<Optional> type,
                             final Type genericType,
                             final Annotation[] annotations,
                             final MediaType mediaType,
                             final MultivaluedMap<String, String> httpHeaders,
                             final InputStream entityStream) throws IOException, WebApplicationException {
        final ClassTypePair optionalType = Utils.optionalParamType(genericType);
        final MessageBodyReader reader = workers.get()
                .getMessageBodyReader(optionalType.rawClass(), optionalType.type(), annotations, mediaType);

        if (reader == null) {
            LOGGER.log(Level.WARNING, LocalizationMessages.ERROR_NOTFOUND_MESSAGEBODYWRITER(mediaType,
                    optionalType.rawClass(), optionalType.type()));

            // If we cannot find reader return an empty Optional.
            return Optional.empty();
        }

        try {
            //noinspection unchecked
            final Object entity = reader.readFrom(optionalType.rawClass(),
                    optionalType.type(),
                    annotations,
                    mediaType,
                    httpHeaders,
                    entityStream);

            // If the entity is an empty string, return empty Optional.
            return "".equals(entity) ? Optional.empty() : Optional.ofNullable(entity);
        } catch (final NoContentException ignored) {
            // This exception can be thrown if the incoming stream is empty - returning Optional.empty().
            return Optional.empty();
        }
    }
}
