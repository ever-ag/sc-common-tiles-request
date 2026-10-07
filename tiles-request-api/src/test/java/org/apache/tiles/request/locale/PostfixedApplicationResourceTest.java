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

package org.apache.tiles.request.locale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * Tests PostfixedApplicationResource.
 *
 * @version $Rev$ $Date$
 */
public class PostfixedApplicationResourceTest {

    private class TestApplicationResource extends PostfixedApplicationResource {
        public TestApplicationResource(String path, Locale locale) {
            super(path, locale);
        }

        public TestApplicationResource(String localePath) {
            super(localePath);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public long getLastModified() throws IOException {
            return 0;
        }

    };

    /**
     * Test getLocalePath(String path, Locale locale).
     */
    @Test
    public void testGetLocalePath() {
        TestApplicationResource resource = new TestApplicationResource("/my test/path_fr.html");
        assertEquals("/my test/path.html", resource.getLocalePath(null));
        assertEquals("/my test/path.html", resource.getLocalePath(Locale.ROOT));
        assertEquals("/my test/path_it.html", resource.getLocalePath(Locale.ITALIAN));
        assertEquals("/my test/path_it_IT.html", resource.getLocalePath(Locale.ITALY));
        assertEquals("/my test/path_en_GB_scotland.html", resource.getLocalePath(new Locale("en", "GB", "scotland")));
    }

    @Test
    public void testBuildFromString() {
        TestApplicationResource resource = new TestApplicationResource("/my test/path_en_GB_scotland.html");
        assertEquals("/my test/path_en_GB_scotland.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(new Locale("en", "GB", "scotland"), resource.getLocale());
        resource = new TestApplicationResource("/my test/path_it_IT.html");
        assertEquals("/my test/path_it_IT.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(Locale.ITALY, resource.getLocale());
        resource = new TestApplicationResource("/my test/path_it.html");
        assertEquals("/my test/path_it.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(Locale.ITALIAN, resource.getLocale());
        resource = new TestApplicationResource("/my test/path.html");
        assertEquals("/my test/path.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(Locale.ROOT, resource.getLocale());
        resource = new TestApplicationResource("/my test/path_zz.html");
        assertEquals("/my test/path_zz.html", resource.getLocalePath());
        assertEquals("/my test/path_zz.html", resource.getPath());
        assertEquals(Locale.ROOT, resource.getLocale());
        resource = new TestApplicationResource("/my test/path_en_ZZ.html");
        assertEquals("/my test/path_en.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(new Locale("en"), resource.getLocale());
        resource = new TestApplicationResource("/my test/path_tiles.html");
        assertEquals("/my test/path_tiles.html", resource.getLocalePath());
        assertEquals("/my test/path_tiles.html", resource.getPath());
        assertEquals(Locale.ROOT, resource.getLocale());
        resource = new TestApplicationResource("/my test/path_longwordthatbreaksISO639.html");
        assertEquals("/my test/path_longwordthatbreaksISO639.html", resource.getLocalePath());
        assertEquals("/my test/path_longwordthatbreaksISO639.html", resource.getPath());
        assertEquals(Locale.ROOT, resource.getLocale());
        resource = new TestApplicationResource("/my test/path_en_tiles.html");
        assertEquals("/my test/path_en.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(new Locale("en"), resource.getLocale());
        resource = new TestApplicationResource("/my test/path_en_longwordthatbreaksISO3166.html");
        assertEquals("/my test/path_en.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(new Locale("en"), resource.getLocale());
    }

    @Test
    public void testBuildFromStringAndLocale() {
        TestApplicationResource resource = new TestApplicationResource("/my test/path.html", new Locale("en", "GB", "scotland"));
        assertEquals("/my test/path_en_GB_scotland.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(new Locale("en", "GB", "scotland"), resource.getLocale());
        resource = new TestApplicationResource("/my test/path.html", Locale.ITALY);
        assertEquals("/my test/path_it_IT.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(Locale.ITALY, resource.getLocale());
        resource = new TestApplicationResource("/my test/path.html", Locale.ITALIAN);
        assertEquals("/my test/path_it.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(Locale.ITALIAN, resource.getLocale());
        resource = new TestApplicationResource("/my test/path.html", Locale.ROOT);
        assertEquals("/my test/path.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
        assertEquals(Locale.ROOT, resource.getLocale());
    }

    /**
     * Locales that must never be concatenated into a resource path
     * (CVE-2023-49735): traversal, absolute paths, URL-encoded traversal,
     * URLs (SSRF), wildcards, control characters, non-ASCII and oversized
     * values.
     *
     * @return the unsafe locales.
     */
    static List<Locale> unsafeLocales() {
        return Arrays.asList(
                new Locale("../../etc/passwd"),
                new Locale(".."),
                new Locale("en", "../x"),
                new Locale("en", "US", "../../x"),
                new Locale("en", "US", "a/b"),
                new Locale("en", "US", "..\\..\\x"),
                new Locale("..\\..\\windows\\win"),
                new Locale("%2e%2e%2f"),
                new Locale("en", "%2e%2e%2f"),
                new Locale("en", "US", "%2e%2e%2fetc%2fpasswd"),
                new Locale("/etc/passwd"),
                new Locale("en", "/etc/passwd"),
                new Locale("c:\\windows"),
                new Locale("file:///etc/passwd"),
                new Locale("http://169.254.169.254/latest/meta-data"),
                new Locale("en", "US", "*"),
                new Locale("en", "US", "a.b"),
                new Locale("en", "US", "x\0"),
                new Locale("en", "US", "x\r\nforged-log-line"),
                new Locale("en", "US", "x y"),
                new Locale("\u00e9n"),
                new Locale("languagetoolong"),
                new Locale("en", "COUNTRYTOOLONG"),
                new Locale("en", "US", "v1234567890123456789012345678901234567890123456789012345678901234"));
    }

    /**
     * Unsafe locales must not reach the path: the non-localized path is used
     * instead (CVE-2023-49735).
     */
    @Test
    public void testGetLocalePathRejectsUnsafeLocales() {
        TestApplicationResource resource = new TestApplicationResource("/WEB-INF/tiles.xml");
        for (Locale locale : unsafeLocales()) {
            String path = resource.getLocalePath(locale);
            assertEquals("Unsafe locale must fall back to the non-localized path: " + locale,
                    "/WEB-INF/tiles.xml", path);
            assertFalse(path.contains(".."));
            assertFalse(path.contains("\\"));
            assertFalse(path.contains("%"));
        }
    }

    /**
     * A resource created with an unsafe locale must not build a traversal
     * path either.
     */
    @Test
    public void testBuildFromStringAndUnsafeLocale() {
        for (Locale locale : unsafeLocales()) {
            TestApplicationResource resource = new TestApplicationResource("/WEB-INF/tiles.xml", locale);
            assertEquals("Unsafe locale must fall back to the non-localized path: " + locale,
                    "/WEB-INF/tiles.xml", resource.getLocalePath());
            assertEquals("/WEB-INF/tiles.xml", resource.getPath());
        }
    }

    /**
     * Valid locales keep building exactly the same paths as before.
     */
    @Test
    public void testGetLocalePathValidLocales() {
        TestApplicationResource resource = new TestApplicationResource("/WEB-INF/tiles.xml");
        assertEquals("/WEB-INF/tiles.xml", resource.getLocalePath(null));
        assertEquals("/WEB-INF/tiles.xml", resource.getLocalePath(Locale.ROOT));
        assertEquals("/WEB-INF/tiles_en.xml", resource.getLocalePath(Locale.ENGLISH));
        assertEquals("/WEB-INF/tiles_en_US.xml", resource.getLocalePath(Locale.US));
        assertEquals("/WEB-INF/tiles_fr_CA.xml", resource.getLocalePath(Locale.CANADA_FRENCH));
        assertEquals("/WEB-INF/tiles_de.xml", resource.getLocalePath(Locale.GERMAN));
        assertEquals("/WEB-INF/tiles_es_ES_TRADITIONAL.xml",
                resource.getLocalePath(new Locale("es", "ES", "TRADITIONAL")));
        assertEquals("/WEB-INF/tiles_es_ES_Traditional_WIN.xml",
                resource.getLocalePath(new Locale("es", "ES", "Traditional_WIN")));
        assertEquals("/WEB-INF/tiles_ja_JP_JP.xml", resource.getLocalePath(new Locale("ja", "JP", "JP")));
        assertEquals("/WEB-INF/tiles_de_DE_1996_fonipa.xml",
                resource.getLocalePath(Locale.forLanguageTag("de-DE-1996-fonipa")));
        assertEquals("/WEB-INF/tiles_sr_RS.xml", resource.getLocalePath(Locale.forLanguageTag("sr-Latn-RS")));
    }

    /**
     * A localized file name whose variant cannot be used in a path is mapped
     * to the closest safe locale, like other unsupported locales (TILES-571),
     * instead of failing.
     */
    @Test
    public void testBuildFromStringWithUnsafeVariant() {
        TestApplicationResource resource = new TestApplicationResource("/my test/path_en_US_a.b.html");
        assertEquals(Locale.US, resource.getLocale());
        assertEquals("/my test/path_en_US.html", resource.getLocalePath());
        assertEquals("/my test/path.html", resource.getPath());
    }
}
