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

package com.google.gwt.resources.client;

import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;

public interface ImportResource extends ClientBundle {

  public interface ImportCss extends GssResource {
    String className();

    String className2();
  }

  @ImportedWithPrefix("testPrefix")
  public interface ImportWithPrefixCss extends GssResource {
    String className();
  }

  ImportCss importCss();

  ImportWithPrefixCss importWithPrefixCss();
}
