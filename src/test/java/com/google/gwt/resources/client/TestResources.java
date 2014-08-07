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

package com.google.gwt.resources.client;

public interface TestResources extends ClientBundle {
  public interface SomeGssResource extends GssResource {
    String someClass();
  }

  public interface SpriteGssResource extends GssResource {
    String someClassWithSprite();

    // define a style class having the same name than another resource in the ClientBundle
    // test possible conflict
    String someImageResource();
  }

  public interface ExternalClasses extends GssResource {
    String obfuscatedClass();

    String externalClass();

    String externalClass2();

    String unobfuscated();

    String unobfuscated2();
  }

  public interface EmptyClass extends GssResource {
    String empty();
  }

  public interface WithConstant extends GssResource {
    String constantOne();

    String classOne();
  }

  public interface ClassNameAnnotation extends GssResource {
    @ClassName("renamed-class")
    String renamedClass();

    String nonRenamedClass();
  }

  ClassNameAnnotation classNameAnnotation();

  SomeGssResource mixin();

  SomeGssResource add();

  SomeGssResource eval();

  SomeGssResource resourceUrl();

  SpriteGssResource sprite();

  ExternalClasses externalClasses();

  EmptyClass emptyClass();

  WithConstant withConstant();

  ImageResource someImageResource();

  @Source("bananaguitar.ani")
  DataResource someDataResource();
}
