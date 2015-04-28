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

import java.lang.reflect.Type;
import java.util.List;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;

/**
 * Utility methods for Java 8 Types.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @since 2.18
 */
final class Utils {

    private static final ClassTypePair STRING_TYPE_PAIR = ClassTypePair.of(String.class);

    /**
     * Get {@link org.glassfish.jersey.internal.util.collection.ClassTypePair class and generic type} for given
     * {@code optionalType}. If no class-type pair can be inferred the method returns class-type pair for String.
     *
     * @param optionalType {@code Optional} type to infer class-type pair.
     * @return class and generic type for given {@code Optional} type.
     */
    static ClassTypePair optionalParamType(final Type optionalType) {
        final List<ClassTypePair> typePairs = ReflectionHelper.getTypeArgumentAndClass(optionalType);

        return typePairs.size() == 1 ? typePairs.get(0) : STRING_TYPE_PAIR;
    }

    /**
     * Get {@link org.glassfish.jersey.internal.util.collection.ClassTypePair class and generic type} for given
     * {@code optionalType}. If no class-type pair can be inferred the method returns class-type pair for the
     * {@code fallbackType}.
     *
     * @param optionalType {@code Optional} type to infer class-type pair.
     * @param fallbackType default value if the class-type pair cannot be inferred.
     * @return class and generic type for given {@code Optional} type.
     */
    static ClassTypePair optionalParamType(final Type optionalType, final Class<?> fallbackType) {
        final List<ClassTypePair> typePairs = ReflectionHelper.getTypeArgumentAndClass(optionalType);

        if (typePairs.size() == 1) {
            return typePairs.get(0);
        } else {
            return ClassTypePair.of(fallbackType);
        }
    }

    /**
     * Prevent instantiation.
     */
    private Utils() {
        throw new AssertionError("No instances allowed.");
    }
}
