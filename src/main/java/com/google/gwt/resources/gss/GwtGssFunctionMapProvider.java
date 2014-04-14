/*
 * Copyright 2013 Julien Dramaix.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.resources.ext.ResourceContext;

import java.util.Map;

public class GwtGssFunctionMapProvider extends DefaultGssFunctionMapProvider {
  private final ResourceContext context;
  private final TreeLogger logger;

  public GwtGssFunctionMapProvider(ResourceContext context, TreeLogger logger) {
    this.context = context;
    this.logger = logger;
  }

  @Override
  public Map<String, GssFunction> get() {
    Map<String, GssFunction> gssFunction = super.get();

    return ImmutableMap.<String, GssFunction>builder().putAll(gssFunction)
        // TODO add a namespace for specific gwt function ?
        .put(EvalFunction.getName(), new EvalFunction())
        .put(ValueFunction.getName(), new ValueFunction())
        .put(GetResourceUrlFunction.getName(), new GetResourceUrlFunction(context, logger))
        .build();
  }
}
