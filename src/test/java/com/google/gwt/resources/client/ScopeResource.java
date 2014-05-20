package com.google.gwt.resources.client;

public interface ScopeResource extends ClientBundle {
  interface ScopeA extends GssResource {
    String foo();
  }

  interface ScopeB extends ScopeA {
    String foo();
  }

  interface ScopeC extends ScopeA {
    // Intentionally not defining foo()
  }

  ScopeA scopeA();

  ScopeA scopeA2();

  ScopeB scopeB();

  ScopeC scopeC();
}
