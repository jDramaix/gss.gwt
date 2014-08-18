/*
 * Copyright 2014 Julien Dramaix.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.resources.gss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gwt.core.ext.TreeLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class RenamingSubstitutionMapTest {
  @Test
  public void computeReplacementMapWithPrefixedClasses() {
    TreeLogger logger = mock(TreeLogger.class);

    Map<String, Map<String, String>> replacementWithPrefix = new HashMap<String, Map<String,
        String>>();
    replacementWithPrefix.put("", ImmutableMap.of("class1", "obfuscated1", "class2",
        "obfuscated2"));
    replacementWithPrefix.put("prefix1-", ImmutableMap.of("class3", "obfuscated3", "class4",
        "obfuscated4"));


    RenamingSubstitutionMap substitutionMap = new RenamingSubstitutionMap(replacementWithPrefix,
        new HashSet<String>(), true, logger);


    assertEquals("obfuscated1", substitutionMap.get("class1"));
    assertEquals("obfuscated2", substitutionMap.get("class2"));
    assertEquals("obfuscated3", substitutionMap.get("prefix1-class3"));
    assertEquals("obfuscated4", substitutionMap.get("prefix1-class4"));
    assertFalse(substitutionMap.hasError());
  }

  @Test
  public void computeReplacementMapWithExternalClasses() {
    TreeLogger logger = mock(TreeLogger.class);

    Map<String, Map<String, String>> replacementWithPrefix = new HashMap<String, Map<String,
        String>>();
    replacementWithPrefix.put("", ImmutableMap.of("class1", "obfuscated1", "class2",
        "obfuscated2", "external1", "obfExternal1", "external2", "obfExternal2"));
    replacementWithPrefix.put("prefix1-", ImmutableMap.of("class3", "obfuscated3", "class4",
        "obfuscated4"));

    Set<String> externals = Sets.newHashSet("external1", "external2");

    RenamingSubstitutionMap substitutionMap = new RenamingSubstitutionMap(replacementWithPrefix,
       externals, true, logger);


    assertEquals("obfuscated1", substitutionMap.get("class1"));
    assertEquals("obfuscated2", substitutionMap.get("class2"));
    assertEquals("obfuscated3", substitutionMap.get("prefix1-class3"));
    assertEquals("obfuscated4", substitutionMap.get("prefix1-class4"));
    assertEquals("external1", substitutionMap.get("external1"));
    assertEquals("external2", substitutionMap.get("external2"));
    assertFalse(substitutionMap.hasError());
  }

  @Test
  public void computeReplacementMapWithMissingCLassAndNotStrict() {
    TreeLogger logger = mock(TreeLogger.class);

    Map<String, Map<String, String>> replacementWithPrefix = new HashMap<String, Map<String,
        String>>();
    replacementWithPrefix.put("", ImmutableMap.of("class1", "obfuscated1"));


    RenamingSubstitutionMap substitutionMap = new RenamingSubstitutionMap(replacementWithPrefix,
        new HashSet<String>(), false, logger);

    assertEquals("obfuscated1", substitutionMap.get("class1"));
    assertEquals("notStrictClass", substitutionMap.get("notStrictClass"));

    assertFalse(substitutionMap.hasError());
  }

  @Test
  public void computeReplacementMapWithMissingCLassAndStrict() {
    TreeLogger logger = mock(TreeLogger.class);

    Map<String, Map<String, String>> replacementWithPrefix = new HashMap<String, Map<String,
        String>>();
    replacementWithPrefix.put("", ImmutableMap.of("class1", "obfuscated1"));


    RenamingSubstitutionMap substitutionMap = new RenamingSubstitutionMap(replacementWithPrefix,
        new HashSet<String>(), true, logger);

    assertEquals("obfuscated1", substitutionMap.get("class1"));
    assertEquals("notStrictClass", substitutionMap.get("notStrictClass"));

    assertTrue(substitutionMap.hasError());
  }
}
