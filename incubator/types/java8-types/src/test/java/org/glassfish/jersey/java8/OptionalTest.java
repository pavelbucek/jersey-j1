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

import java.util.Optional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class OptionalTest extends JerseyTest {

    @Path("/")
    @Produces("text/plain")
    public static class OptionalResource {

        @GET
        @Path("empty")
        public Response getEmpty() {
            return Response.ok().build();
        }

        @GET
        @Path("response")
        public Response getResponse() {
            return Response.ok(Optional.of("foo")).build();
        }

        @GET
        @Path("string")
        public Optional<String> getString() {
            return Optional.of("foo");
        }

        @GET
        @Path("optional-string")
        public Optional getOptionalString() {
            return Optional.of("foo");
        }

        @GET
        @Path("optional-int")
        public Optional getOptionalInt() {
            return Optional.of(42);
        }

        @GET
        @Path("param-string")
        public String getParamString(@QueryParam("foo") final Optional<String> foo) {
            return foo.orElse("baz");
        }

        @GET
        @Path("param-string-default")
        public String getParamStringDefault(@QueryParam("foo") @DefaultValue("baz") final Optional<String> foo) {
            // Weird, but still.
            return foo.get();
        }

        @POST
        @Path("post-string")
        public String postString(final Optional<String> foo) {
            return foo.orElse("bar");
        }

        @POST
        @Path("post-int")
        public Integer postInt(final Optional<Integer> foo) {
            return foo.orElse(23);
        }

        @POST
        @Path("post-roundtrip")
        public Optional<String> postRoundtrip(final Optional<String> foo) {
            return foo;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(OptionalResource.class)
                .register(Java8TypesFeature.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(Java8TypesFeature.class);
    }

    @Test
    public void testGetResponse() throws Exception {
        final Response response = target("response").request().get();

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(String.class), is("foo"));
    }

    @Test
    public void testGetString() throws Exception {
        final Response response = target("string").request().get();

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(String.class), is("foo"));
    }

    @Test
    public void testGetOptionalStringValue() throws Exception {
        final Response response = target("optional-string").request().get();

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(String.class), is("foo"));
    }

    @Test
    public void testGetOptionalIntValue() throws Exception {
        final Response response = target("optional-int").request().get();

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(Integer.class), is(42));
    }

    @Test
    public void testGetParamString() throws Exception {
        final Response response = target("param-string").queryParam("foo", "bar").request().get();

        assertThat("Unexpected param. Application doesn't support Optional type", response.readEntity(String.class), is("bar"));
    }

    @Test
    public void testGetParamNullString() throws Exception {
        final Response response = target("param-string").request().get();

        assertThat("Unexpected param. Application doesn't support Optional type", response.readEntity(String.class), is("baz"));
    }

    @Test
    public void testGetParamStringDefault() throws Exception {
        final Response response = target("param-string-default").queryParam("foo", "bar").request().get();

        assertThat("Unexpected param. Application doesn't support Optional type", response.readEntity(String.class), is("bar"));
    }

    @Test
    public void testGetParamNullStringDefault() throws Exception {
        final Response response = target("param-string-default").request().get();

        assertThat("Unexpected param. Application doesn't support Optional type", response.readEntity(String.class), is("baz"));
    }

    @Test
    public void testPostString() throws Exception {
        final Response response = target("post-string").request().post(Entity.text(Optional.of("foo")));

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(String.class), is("foo"));
    }

    @Test
    public void testPostInt() throws Exception {
        final Response response = target("post-int").request().post(Entity.text(Optional.of(42)));

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(Integer.class), is(42));
    }

    @Test
    public void testPostIntEmpty() throws Exception {
        final Response response = target("post-int").request().post(Entity.text(Optional.empty()));

        assertThat("Unexpected entity. Application doesn't support Optional type", response.readEntity(Integer.class), is(23));
    }

    @Test
    public void testPostIntInvalid() throws Exception {
        final Response response = target("post-int").request().post(Entity.text(Optional.of("foo")));

        assertThat("Unexpected status, message body reader for \"Integer\" should not be found", response.getStatus(), is(500));
    }

    @Test
    public void testRoundTrip() throws Exception {
        final Optional<Integer> optional = target("post-roundtrip").request().post(Entity.text(Optional.of(42)),
                new GenericType<Optional<Integer>>() {});

        assertThat("Wrong Optional received", optional.get(), is(42));
    }

    @Test
    public void testRoundTripEmpty() throws Exception {
        final Optional<Integer> optional = target("post-roundtrip").request().post(Entity.text(Optional.empty()),
                new GenericType<Optional<Integer>>() {});

        assertThat("Wrong Optional received", optional, is(Optional.empty()));
    }

    @Test
    public void testReceiveEmptyObservableString() throws Exception {
        final Optional<String> optional = target("empty").request().get(new GenericType<Optional<String>>() {});

        assertThat("", optional, is(Optional.empty()));
    }

    @Test
    public void testReceiveEmptyObservableInteger() throws Exception {
        final Optional<Integer> optional = target("empty").request("text/plain").get(new GenericType<Optional<Integer>>() {});

        assertThat("", optional, is(Optional.empty()));
    }
}
