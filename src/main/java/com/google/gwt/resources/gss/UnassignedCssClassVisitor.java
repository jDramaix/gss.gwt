package com.google.gwt.resources.gss;

import java.util.List;

import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.VisitController;

public class UnassignedCssClassVisitor extends DefaultTreeVisitor implements CssCompilerPass {
  private final VisitController visitController;
  private final List<String> methodNames;
  private final ErrorManager errorManager;

  public UnassignedCssClassVisitor(
          VisitController visitController,
          List<String> methodNames,
          ErrorManager errorManager) {
    this.visitController = visitController;
    this.methodNames = methodNames;
    this.errorManager = errorManager;
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  @Override
  public boolean enterClassSelector(CssClassSelectorNode classSelector) {
    String refinerName = classSelector.getRefinerName();

    if (!methodNames.contains(refinerName)) {
      GssError gssError =
              new GssError("The following unobfuscated class is present in a strict GssResource: " + refinerName,
              classSelector.getSourceCodeLocation());
      errorManager.report(gssError);
    }

    return super.enterClassSelector(classSelector);
  }
}
