/*
 * Copyright 2013 GWT project.
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

package com.google.gwt.resources.client;

import com.google.gwt.resources.ext.DefaultExtensions;
import com.google.gwt.resources.ext.ResourceGeneratorType;
import com.google.gwt.resources.rg.GssResourceGenerator;

@DefaultExtensions(value = {".gss"})
@ResourceGeneratorType(GssResourceGenerator.class)
public interface GssResource extends ResourcePrototype {

  /**
   * Calls
   * {@link com.google.gwt.dom.client.StyleInjector#injectStylesheet(String)} to
   * inject the contents of the GssResource into the DOM. Repeated calls to this
   * method on an instance of a GssResource will have no effect.
   *
   * @return <code>true</code> if this method mutated the DOM.
   */
  boolean ensureInjected();

  /**
   * Provides the contents of the GssResource.
   */
  String getText();
}
