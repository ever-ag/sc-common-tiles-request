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

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests {@link LocaleUtil}.
 *
 * @version $Rev$ $Date$
 */
public class LocaleUtilTest extends TestCase {

    /**
     * Test method for {@link LocaleUtil#getParentLocale(Locale)}.
     */
    public void testGetParentLocale() {
        assertNull("The parent locale of NULL_LOCALE is not correct",
                LocaleUtil.getParentLocale(Locale.ROOT));
        assertEquals("The parent locale of 'en' is not correct",
                Locale.ROOT, LocaleUtil
                        .getParentLocale(Locale.ENGLISH));
        assertEquals("The parent locale of 'en_US' is not correct",
                Locale.ENGLISH, LocaleUtil.getParentLocale(Locale.US));
        Locale locale = new Locale("es", "ES", "Traditional_WIN");
        Locale parentLocale = new Locale("es", "ES");
        assertEquals("The parent locale of 'es_ES_Traditional_WIN' is not correct",
                parentLocale, LocaleUtil.getParentLocale(locale));
    }

    /**
     * Test method for {@link LocaleUtil#isSafeLocale(Locale)} with valid
     * locales.
     */
    public void testIsSafeLocaleAcceptsValidLocales() {
        assertTrue(LocaleUtil.isSafeLocale(Locale.ROOT));
        assertTrue(LocaleUtil.isSafeLocale(Locale.ENGLISH));
        assertTrue(LocaleUtil.isSafeLocale(Locale.US));
        assertTrue(LocaleUtil.isSafeLocale(Locale.CANADA_FRENCH));
        assertTrue(LocaleUtil.isSafeLocale(Locale.GERMAN));
        assertTrue(LocaleUtil.isSafeLocale(new Locale("es", "ES", "TRADITIONAL")));
        assertTrue(LocaleUtil.isSafeLocale(new Locale("es", "ES", "Traditional_WIN")));
        assertTrue(LocaleUtil.isSafeLocale(new Locale("ja", "JP", "JP")));
        assertTrue(LocaleUtil.isSafeLocale(new Locale("th", "TH", "TH")));
        assertTrue(LocaleUtil.isSafeLocale(Locale.forLanguageTag("de-DE-1996-fonipa")));
        assertTrue(LocaleUtil.isSafeLocale(Locale.forLanguageTag("sr-Latn-RS")));
        assertTrue(LocaleUtil.isSafeLocale(Locale.forLanguageTag("es-419")));
        for (Locale locale : Locale.getAvailableLocales()) {
            assertTrue("JDK locale rejected: " + locale, LocaleUtil.isSafeLocale(locale));
        }
    }

    /**
     * Test method for {@link LocaleUtil#isSafeLocale(Locale)} with unsafe
     * locales (CVE-2023-49735).
     */
    public void testIsSafeLocaleRejectsUnsafeLocales() {
        assertFalse(LocaleUtil.isSafeLocale(null));
        for (Locale locale : PostfixedApplicationResourceTest.unsafeLocales()) {
            assertFalse("Unsafe locale accepted: " + locale, LocaleUtil.isSafeLocale(locale));
        }
    }
}
