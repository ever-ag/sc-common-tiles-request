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

/**
 * Utilities for locale manipulation.
 *
 * @version $Rev$ $Date$
 */
public final class LocaleUtil {

    /**
     * The "null" Locale, i.e. a Locale that points to no real locale.
     *
     * @deprecated use Locale.ROOT instead.
     */
    @Deprecated
    public static final Locale NULL_LOCALE = Locale.ROOT;

    /**
     * Maximum length accepted for the language, script and country parts of a
     * locale that is used to build a resource path.
     */
    private static final int MAX_SUBTAG_LENGTH = 8;

    /**
     * Maximum length accepted for the variant part of a locale that is used to
     * build a resource path. Variants may legitimately contain several
     * subtags, e.g. <code>Traditional_WIN</code> or <code>1996_fonipa</code>.
     */
    private static final int MAX_VARIANT_LENGTH = 64;

    /**
     * Private constructor to avoid instantiation.
     */
    private LocaleUtil() {
    }

    /**
     * <p>
     * Checks whether a locale is safe to be used to build a localized resource
     * path, e.g. <code>/WEB-INF/tiles_&lt;language&gt;_&lt;country&gt;_&lt;variant&gt;.xml</code>.
     * </p>
     * <p>
     * {@link Locale} instances created with the public constructors accept
     * arbitrary strings, such as <code>new Locale("../../x")</code>, so a
     * locale coming from a user-influenced source must not be concatenated
     * into a path without being checked (CVE-2023-49735).
     * </p>
     * <p>
     * A locale is safe when its language, script and country contain only
     * ASCII letters and digits (at most {@value #MAX_SUBTAG_LENGTH} characters
     * each) and its variant contains only ASCII letters, digits,
     * <code>'_'</code> and <code>'-'</code> (at most
     * {@value #MAX_VARIANT_LENGTH} characters). {@link Locale#ROOT} is safe.
     * </p>
     *
     * @param locale The locale to check.
     * @return <code>true</code> if the locale is not <code>null</code> and
     * safe to use in a resource path.
     */
    public static boolean isSafeLocale(Locale locale) {
        if (locale == null) {
            return false;
        }
        return isSafeLocalePart(locale.getLanguage(), MAX_SUBTAG_LENGTH, false)
                && isSafeLocalePart(locale.getScript(), MAX_SUBTAG_LENGTH, false)
                && isSafeLocalePart(locale.getCountry(), MAX_SUBTAG_LENGTH, false)
                && isSafeLocalePart(locale.getVariant(), MAX_VARIANT_LENGTH, true);
    }

    /**
     * Checks a single part of a locale.
     *
     * @param part The part to check. <code>null</code> is treated as empty.
     * @param maxLength The maximum allowed length.
     * @param allowSeparators Whether <code>'_'</code> and <code>'-'</code>
     * are allowed.
     * @return <code>true</code> if the part only contains allowed characters.
     */
    private static boolean isSafeLocalePart(String part, int maxLength, boolean allowSeparators) {
        if (part == null) {
            return true;
        }
        int length = part.length();
        if (length > maxLength) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char ch = part.charAt(i);
            boolean allowed = (ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9')
                    || (allowSeparators && (ch == '_' || ch == '-'));
            if (!allowed) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Returns the "parent" locale of a given locale.
     * </p>
     * <p>
     * If the original locale is only language-based, the {@link #NULL_LOCALE}
     * object is returned.
     * </p>
     * <p>
     * If the original locale is {@link #NULL_LOCALE}, then <code>null</code>
     * is returned.
     * </p>
     *
     * @param locale The original locale.
     * @return The parent locale.
     */
    public static Locale getParentLocale(Locale locale) {
        Locale retValue = null;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if (!"".equals(variant)) {
            retValue = new Locale(language, country);
        } else if (!"".equals(country)) {
            retValue = new Locale(language);
        } else if (!"".equals(language)) {
            retValue = Locale.ROOT;
        }

        return retValue;
    }
}
