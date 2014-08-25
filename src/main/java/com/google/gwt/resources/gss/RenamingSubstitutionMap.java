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

import com.google.common.css.SubstitutionMap;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This SubstitutionMap is used for renaming each style classes of the ast by its corresponding
 * obfuscated name.
 * <p/>
 * It takes care of eventual prefix and not renaming external style classes. It also raise an
 * error if a style class doesn't have a replacement name and strict mode is enabled.
 * <p/>
 * This class doesn't compute the replacement name of the class.
 *
 */
public class RenamingSubstitutionMap implements SubstitutionMap {
  private final Map<String, String> replacementMap;
  private final boolean isStrict;
  private final TreeLogger logger;

  private boolean hasError;
  private Set<String> classes;

  public RenamingSubstitutionMap(Map<String, Map<String, String>> replacementsWithPrefix,
      Collection<String> externalClasses, boolean isStrict, TreeLogger logger) {
    this.isStrict = isStrict;
    this.logger = logger;
    this.replacementMap = computeReplacementMap(replacementsWithPrefix, externalClasses);

    classes = new HashSet<String>();
  }

  private Map<String, String> computeReplacementMap(
      Map<String, Map<String, String>> replacementsWithPrefix, Collection<String> externalClasses) {

    Map<String, String> result = new HashMap<String, String>();

    for (Entry<String, Map<String, String>> entry : replacementsWithPrefix.entrySet()) {
      final String prefix = entry.getKey();
      Map<String, String> replacement = new HashMap<String, String>();

      for (Entry<String, String> replacementEntry : entry.getValue().entrySet()) {
        replacement.put(prefix + replacementEntry.getKey(), replacementEntry.getValue());
      }

      result.putAll(replacement);
    }

    // override mapping for external classes
    for (String external : externalClasses) {
      result.put(external, external);
    }

    return result;
  }

  @Override
  public String get(String key) {
    classes.add(key);

    String replacement = replacementMap.get(key);

    if (replacement == null) {
      if (isStrict) {
        logger.log(Type.ERROR, "The following non-obfuscated class is present in a strict " +
            "CssResource: " + key);
        hasError = true;
      }
      return key;
    }

    return replacement;
  }

  public boolean hasError() {
    return hasError;
  }

  public Set<String> getStyleClasses() {
    return classes;
  }
}
