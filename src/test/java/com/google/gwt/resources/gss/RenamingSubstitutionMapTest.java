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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class RenamingSubstitutionMapTest {
  @Test
  public void computeReplacementMapWithPrefixedClasses() {
    Map<String, Map<String, String>> replacementWithPrefix = new HashMap<String, Map<String,
        String>>();
    replacementWithPrefix.put("", ImmutableMap.of("class1", "obfuscated1", "class2",
        "obfuscated2"));
    replacementWithPrefix.put("prefix1-", ImmutableMap.of("class3", "obfuscated3", "class4",
        "obfuscated4"));


    RenamingSubstitutionMap substitutionMap = new RenamingSubstitutionMap(replacementWithPrefix);

    assertEquals("obfuscated1", substitutionMap.get("class1"));
    assertEquals("obfuscated2", substitutionMap.get("class2"));
    assertEquals("obfuscated3", substitutionMap.get("prefix1-class3"));
    assertEquals("obfuscated4", substitutionMap.get("prefix1-class4"));
    assertTrue(substitutionMap.getExternalClassCandidates().isEmpty());
  }

  @Test
  public void computeReplacementMapWithMissingCLass() {
    Map<String, Map<String, String>> replacementWithPrefix = new HashMap<String, Map<String,
        String>>();
    replacementWithPrefix.put("", ImmutableMap.of("class1", "obfuscated1"));


    RenamingSubstitutionMap substitutionMap = new RenamingSubstitutionMap(replacementWithPrefix);

    assertEquals("obfuscated1", substitutionMap.get("class1"));
    assertEquals("otherClass", substitutionMap.get("otherClass"));

    assertFalse(substitutionMap.getExternalClassCandidates().isEmpty());
    assertTrue(substitutionMap.getExternalClassCandidates().contains("otherClass"));
  }
}
