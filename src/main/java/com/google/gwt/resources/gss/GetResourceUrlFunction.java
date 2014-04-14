package com.google.gwt.resources.gss;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssFunctionArgumentsNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.css.ast.CssCompilerException;
import com.google.gwt.resources.css.ast.CssProperty;
import com.google.gwt.resources.ext.ResourceContext;
import com.google.gwt.resources.ext.ResourceGeneratorUtil;
import com.google.gwt.resources.gss.ast.CssJavaExpressionNode;

public class GetResourceUrlFunction implements GssFunction {
  private final ResourceContext context;
  private final TreeLogger logger;
  private final JClassType dataResourceType;
  private final JClassType imageResourceType;

  public GetResourceUrlFunction(ResourceContext context, TreeLogger logger) {
    this.context = context;
    this.logger = logger;
    this.dataResourceType = context.getGeneratorContext().getTypeOracle()
            .findType(DataResource.class.getCanonicalName());
    this.imageResourceType = context.getGeneratorContext().getTypeOracle()
            .findType(ImageResource.class.getCanonicalName());
  }

  public static String getName() {
    return "getResourceUrl";
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
    String value = functionToEval.getValue();
    CssProperty.DotPathValue dotPathValue = new CssProperty.DotPathValue(value);

    JType methodType;
    try {
      List<String> parts = dotPathValue.getParts();
      methodType = ResourceGeneratorUtil.getMethodByPath(context.getClientBundleType(),
              parts, null).getReturnType();
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, e.getMessage());
      throw new CssCompilerException("Cannot find data method");
    }

    if (!methodType.equals(dataResourceType) &&
            !methodType.equals(imageResourceType)) {
      String message = "Invalid method type for url substitution: " + methodType + ". " +
              "Only DataResource and ImageResource are supported.";
      logger.log(TreeLogger.ERROR, message);
      throw new CssCompilerException(message);
    }

    String instance = "((" + methodType.getQualifiedSourceName() + ")("
            + context.getImplementationSimpleSourceName() + ".this."
            + dotPathValue.getExpression() + "))";

    StringBuilder expression = new StringBuilder();
    if (methodType.equals(dataResourceType)) {
      expression.append(instance).append(".getUrl()");
    } else if (methodType.equals(imageResourceType)) {
      expression.append(instance).append(".getSafeUri().asString()");
    }

    CssFunctionNode node = new CssFunctionNode(CssFunctionNode.Function.byName("url"),
            SourceCodeLocation.getUnknownLocation());
    List<CssValueNode> list = Lists.newArrayList();
    CssJavaExpressionNode cssJavaExpressionNode = new CssJavaExpressionNode(expression.toString());
    list.add(cssJavaExpressionNode);
    CssFunctionArgumentsNode arguments = new CssFunctionArgumentsNode(list);
    node.setArguments(arguments);

    return ImmutableList.of((CssValueNode) node);
  }

  @Override
  public String getCallResultString(List<String> strings) throws GssFunctionException {
    // TODO: I don't know if I should return something more elaborate
    return strings.get(0);
  }
}
