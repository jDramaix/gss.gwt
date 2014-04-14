package com.google.gwt.resources.gss;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;
import com.google.common.css.compiler.gssfunctions.GssFunctions;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.ext.ResourceContext;
import com.google.gwt.resources.ext.ResourceGeneratorUtil;
import com.google.gwt.resources.gss.ast.CssDotPathNode;
import com.google.gwt.resources.gss.ast.CssJavaExpressionNode;

public class ResourceUrlFunction implements GssFunction {
  private final ResourceContext context;
  private final JClassType dataResourceType;
  private final JClassType imageResourceType;

  public ResourceUrlFunction(ResourceContext context) {
    this.context = context;
    this.dataResourceType = context.getGeneratorContext().getTypeOracle()
            .findType(DataResource.class.getCanonicalName());
    this.imageResourceType = context.getGeneratorContext().getTypeOracle()
            .findType(ImageResource.class.getCanonicalName());
  }

  public static String getName() {
    return "resourceUrl";
  }

  @Override
  public Integer getNumExpectedArguments() {
    return 1;
  }

  @Override
  public List<CssValueNode> getCallResultNodes(List<CssValueNode> cssValueNodes, ErrorManager errorManager) throws
          GssFunctionException {
    // TODO: refactor this in smaller methods

    CssValueNode functionToEval = cssValueNodes.get(0);

    CssDotPathNode dotPathValue
            = new CssDotPathNode(functionToEval.getValue(), "", "", functionToEval.getSourceCodeLocation());

    JType methodType;
    try {
      List<String> parts = dotPathValue.getParts();
      methodType = ResourceGeneratorUtil.getMethodByPath(context.getClientBundleType(),
              parts, null).getReturnType();
    } catch (NotFoundException e) {
      String message = e.getMessage();
      errorManager.report(new GssError(message, functionToEval.getSourceCodeLocation()));
      throw new GssFunctionException(message);
    }

    if (!dataResourceType.isAssignableFrom((JClassType) methodType) &&
            !imageResourceType.isAssignableFrom((JClassType) methodType)) {
      String message = "Invalid method type for url substitution: " + methodType + ". " +
              "Only DataResource and ImageResource are supported.";
      errorManager.report(new GssError(message, functionToEval.getSourceCodeLocation()));
      throw new GssFunctionException(message);
    }

    String instance = context.getImplementationSimpleSourceName() + ".this."
            + dotPathValue.getValue() + ".getSafeUri().asString()";

    CssFunctionNode node = GssFunctions.createUrlNode("", functionToEval.getSourceCodeLocation());
    CssJavaExpressionNode cssJavaExpressionNode = new CssJavaExpressionNode(instance);
    CssFunctionArgumentsNode arguments =
            new CssFunctionArgumentsNode(ImmutableList.<CssValueNode>of(cssJavaExpressionNode));
    node.setArguments(arguments);

    return ImmutableList.of((CssValueNode) node);
  }

  @Override
  public String getCallResultString(List<String> strings) throws GssFunctionException {
    return strings.get(0);
  }
}
