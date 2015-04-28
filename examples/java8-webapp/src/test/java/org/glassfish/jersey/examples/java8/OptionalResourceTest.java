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

package org.glassfish.jersey.examples.java8;

import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.examples.java8.model.TwoValueHolder;
import org.glassfish.jersey.examples.java8.model.ValueHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.java8.Java8TypesFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for OptionalResource. Shows how to use optional on client.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class OptionalResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new Java8Application();
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(Java8TypesFeature.class);
        config.register(JacksonFeature.class);
    }

    @Test
    public void testEmpty() throws Exception {
        final Response response = target("optional").path("empty").request().get();

        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    @Test
    public void testString() throws Exception {
        final Response response = target("optional").path("string").request().get();

        assertThat(response.readEntity(String.class), is("foo"));
    }

    @Test
    public void testOptionalString() throws Exception {
        final Optional<String> response = target("optional").path("string")
                .request()
                .get(new GenericType<Optional<String>>() {});

        assertThat(response, is(Optional.of("foo")));
    }

    @Test
    public void testParamString() throws Exception {
        final Response response = target("optional").path("param-string")
                .queryParam("foo", "bar")
                .request().get();

        assertThat(response.readEntity(String.class), is("bar"));
    }

    @Test
    public void testEmptyParamString() throws Exception {
        final Response response = target("optional").path("param-string")
                .request().get();

        assertThat(response.readEntity(String.class), is("baz"));
    }

    @Test
    public void testPostJson() throws Exception {
        final Response response = target("optional").path("json")
                .request()
                .header("foo", 42)
                .post(Entity.json(new ValueHolder("bar")));

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(TwoValueHolder.class), is(new TwoValueHolder("bar", 42)));
    }

    @Test
    public void testRoundtrip() throws Exception {
        final Optional<ValueHolder> entity = Optional.of("bar").map(ValueHolder::new);
        final Response response = target("optional").path("json")
                .request()
                .post(Entity.json(entity));

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(new GenericType<Optional<TwoValueHolder>>() {}),
                is(entity.map(v -> new TwoValueHolder(v.getValue(), 23))));
    }
}
