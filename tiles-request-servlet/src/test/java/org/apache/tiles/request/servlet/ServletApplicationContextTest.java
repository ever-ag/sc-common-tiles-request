/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tiles.request.servlet;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.ServletContext;

import org.apache.tiles.request.ApplicationResource;
import org.apache.tiles.request.collection.ReadOnlyEnumerationMap;
import org.apache.tiles.request.collection.ScopeMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ServletApplicationContext}.
 *
 * @version $Rev$ $Date$
 */
public class ServletApplicationContextTest {

    /**
     * The servlet context.
     */
    private ServletContext servletContext;

    /**
     * The application context to test.
     */
    private ServletApplicationContext context;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        servletContext = createMock(ServletContext.class);
        context = new ServletApplicationContext(servletContext);
    }

    /**
     * Test method for {@link org.apache.tiles.request.servlet.ServletApplicationContext#getContext()}.
     */
    @Test
    public void testGetContext() {
        replay(servletContext);
        assertEquals(servletContext, context.getContext());
        verify(servletContext);
    }

    /**
     * Test method for {@link org.apache.tiles.request.servlet.ServletApplicationContext#getApplicationScope()}.
     */
    @Test
    public void testGetApplicationScope() {
        replay(servletContext);
        assertTrue(context.getApplicationScope() instanceof ScopeMap);
        verify(servletContext);
    }

    /**
     * Test method for {@link org.apache.tiles.request.servlet.ServletApplicationContext#getInitParams()}.
     */
    @Test
    public void testGetInitParams() {
        replay(servletContext);
        assertTrue(context.getInitParams() instanceof ReadOnlyEnumerationMap);
        verify(servletContext);
    }

    /**
     * Test method for {@link org.apache.tiles.request.servlet.ServletApplicationContext#getResource(java.lang.String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testGetResource() throws IOException {
        URL url = new URL("file:///servletContext/my/path.html");
        URL urlFr = new URL("file:///servletContext/my/path_fr.html");
        expect(servletContext.getResource("/my/path.html")).andReturn(url);
        expect(servletContext.getResource("/my/path_fr.html")).andReturn(urlFr);
        expect(servletContext.getResource("/null/path.html")).andReturn(null);

        replay(servletContext);
        ApplicationResource resource = context.getResource("/my/path.html");
        assertNotNull(resource);
        assertEquals(resource.getLocalePath(), "/my/path.html");
        assertEquals(resource.getPath(), "/my/path.html");
        assertEquals(Locale.ROOT, resource.getLocale());
        ApplicationResource resourceFr = context.getResource(resource, Locale.FRENCH);
        assertNotNull(resourceFr);
        assertEquals("/my/path_fr.html", resourceFr.getLocalePath());
        assertEquals("/my/path.html", resourceFr.getPath());
        assertEquals(Locale.FRENCH, resourceFr.getLocale());
        ApplicationResource nullResource = context.getResource("/null/path.html");
        assertNull(nullResource);
        verify(servletContext);
    }

    /**
     * Test method for
     * {@link ServletApplicationContext#getResource(ApplicationResource, Locale)}
     * with locales that must not be concatenated into the path
     * (CVE-2023-49735): only the non-localized path may be requested from the
     * servlet context, and valid locales keep their localized path.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testGetResourceWithUnsafeLocale() throws IOException {
        URL url = new URL("file:///servletContext/WEB-INF/tiles.xml");
        URL urlEnUs = new URL("file:///servletContext/WEB-INF/tiles_en_US.xml");
        Locale[] unsafeLocales = new Locale[] {
            new Locale("../../etc/passwd"),
            new Locale("en", "../x"),
            new Locale("en", "US", "a/b"),
            new Locale("en", "US", "..\\..\\x"),
            new Locale("%2e%2e%2f"),
            new Locale("/etc/passwd"),
            new Locale("http://169.254.169.254/latest")
        };
        expect(servletContext.getResource("/WEB-INF/tiles.xml")).andReturn(url).times(1 + unsafeLocales.length);
        expect(servletContext.getResource("/WEB-INF/tiles_en_US.xml")).andReturn(urlEnUs);

        replay(servletContext);
        ApplicationResource resource = context.getResource("/WEB-INF/tiles.xml");
        for (Locale locale : unsafeLocales) {
            ApplicationResource localized = context.getResource(resource, locale);
            assertNotNull(localized);
            assertEquals("/WEB-INF/tiles.xml", localized.getLocalePath());
            assertEquals("/WEB-INF/tiles.xml", localized.getPath());
        }
        ApplicationResource resourceEnUs = context.getResource(resource, Locale.US);
        assertEquals("/WEB-INF/tiles_en_US.xml", resourceEnUs.getLocalePath());
        verify(servletContext);
    }

    /**
     * Test method for {@link ServletApplicationContext#getResources(String)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testGetResources() throws IOException {
        URL url = new URL("file:///servletContext/my/path");
        expect(servletContext.getResource("/my/path")).andReturn(url);

        replay(servletContext);
        Collection<ApplicationResource> resources = context.getResources("/my/path");
        assertEquals(1, resources.size());
        assertEquals(resources.iterator().next().getLocalePath(), "/my/path");
        verify(servletContext);
    }
}
