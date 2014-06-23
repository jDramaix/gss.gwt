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

package com.google.gwt.resources.rg;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.zip.Adler32;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.css.IdentitySubstitutionMap;
import com.google.common.css.MinimalSubstitutionMap;
import com.google.common.css.PrefixingSubstitutionMap;
import com.google.common.css.RecordingSubstitutionMap;
import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.SubstitutionMap;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.AbbreviatePositionalValues;
import com.google.common.css.compiler.passes.CheckDependencyNodes;
import com.google.common.css.compiler.passes.CollectConstantDefinitions;
import com.google.common.css.compiler.passes.CollectMixinDefinitions;
import com.google.common.css.compiler.passes.ColorValueOptimizer;
import com.google.common.css.compiler.passes.ConstantDefinitions;
import com.google.common.css.compiler.passes.CreateComponentNodes;
import com.google.common.css.compiler.passes.CreateConditionalNodes;
import com.google.common.css.compiler.passes.CreateConstantReferences;
import com.google.common.css.compiler.passes.CreateDefinitionNodes;
import com.google.common.css.compiler.passes.CreateMixins;
import com.google.common.css.compiler.passes.CreateStandardAtRuleNodes;
import com.google.common.css.compiler.passes.CssClassRenaming;
import com.google.common.css.compiler.passes.DisallowDuplicateDeclarations;
import com.google.common.css.compiler.passes.EliminateConditionalNodes;
import com.google.common.css.compiler.passes.EliminateEmptyRulesetNodes;
import com.google.common.css.compiler.passes.EliminateUnitsFromZeroNumericValues;
import com.google.common.css.compiler.passes.EliminateUselessRulesetNodes;
import com.google.common.css.compiler.passes.HandleUnknownAtRuleNodes;
import com.google.common.css.compiler.passes.MarkRemovableRulesetNodes;
import com.google.common.css.compiler.passes.MergeAdjacentRulesetNodesWithSameDeclarations;
import com.google.common.css.compiler.passes.MergeAdjacentRulesetNodesWithSameSelector;
import com.google.common.css.compiler.passes.ProcessComponents;
import com.google.common.css.compiler.passes.ProcessKeyframes;
import com.google.common.css.compiler.passes.ProcessRefiners;
import com.google.common.css.compiler.passes.ReplaceConstantReferences;
import com.google.common.css.compiler.passes.ReplaceMixins;
import com.google.common.css.compiler.passes.ResolveCustomFunctionNodes;
import com.google.common.css.compiler.passes.SplitRulesetNodes;
import com.google.common.io.Resources;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.collect.IdentityHashMap;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ClassName;
import com.google.gwt.resources.client.GssResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.resources.ext.ClientBundleRequirements;
import com.google.gwt.resources.ext.ResourceContext;
import com.google.gwt.resources.ext.ResourceGeneratorUtil;
import com.google.gwt.resources.ext.SupportsGeneratorResultCaching;
import com.google.gwt.resources.gss.CssPrinter;
import com.google.gwt.resources.gss.ExternalClassesCollector;
import com.google.gwt.resources.gss.GwtGssFunctionMapProvider;
import com.google.gwt.resources.gss.ImageSpriteCreator;
import com.google.gwt.resources.gss.PermutationsCollector;
import com.google.gwt.resources.gss.RecordingBidiFlipper;
import com.google.gwt.resources.gss.UnassignedCssClassVisitor;
import com.google.gwt.resources.rg.CssResourceGenerator.JClassOrderComparator;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;

public class GssResourceGenerator extends AbstractCssResourceGenerator implements
    SupportsGeneratorResultCaching {
  /**
   * {@link ErrorManager} used to log the errors and warning messages produced by the different
   * {@link com.google.common.css.compiler.ast.CssCompilerPass}
   */
  private static class LoggerErrorManager implements ErrorManager {
    private final TreeLogger logger;
    private boolean hasErrors;

    private LoggerErrorManager(TreeLogger logger) {
      this.logger = logger;
    }

    @Override
    public void generateReport() {
      // do nothing
    }

    @Override
    public boolean hasErrors() {
      return hasErrors;
    }

    @Override
    public void report(GssError error) {
      String fileName = "";
      String location = "";
      SourceCodeLocation codeLocation = error.getLocation();

      if (codeLocation != null) {
        fileName = codeLocation.getSourceCode().getFileName();
        location = "[line:" + codeLocation.getBeginLineNumber() + " column:" + codeLocation
            .getBeginIndexInLine() + "]";
      }

      logger.log(Type.ERROR, "Error in " + fileName + location + ": " + error.getMessage());
      hasErrors = true;
    }

    @Override
    public void reportWarning(GssError warning) {
      logger.log(Type.WARN, warning.getMessage());
    }
  }

  // TODO rename this class
  private static class OptimizationInfo {
    final ConstantDefinitions constantDefinitions;

    private OptimizationInfo(ConstantDefinitions constantDefinitions) {
      this.constantDefinitions = constantDefinitions;
    }
  }

  private static class ExtendedCssTree {
    private final CssTree tree;
    private final List<String> permutationAxes;

    private ExtendedCssTree(CssTree tree, List<String> permutationAxis) {
      this.tree = tree;
      this.permutationAxes = permutationAxis;
    }

    public CssTree getCssTree() {
      return tree;
    }

    public List<String> getPermutationAxes() {
      return permutationAxes;
    }
  }

  private static final Cache<List<URL>, ExtendedCssTree> TREE_CACHE = CacheBuilder.newBuilder()
      .softValues().build();
  private static final Cache<List<URL>, Long> LAST_MODIFIED_CACHE = CacheBuilder.newBuilder()
      .build();
  // TO be sure to avoid conflict during the style classes renaming between different GssResource,
  // we will create a different prefix for each GssResource. We use a MinimalSubstitutionMap
  // that will create a String with 1-6 characters in length but keeping the length of the prefix
  // as short as possible. For instance if we have two GssResources to compile, the  prefix
  // for the first resource will be 'a' and the prefix for the second resource will be 'b' and so on
  private static final SubstitutionMap resourcePrefixBuilder = new MinimalSubstitutionMap();
  // TODO maybe define our own property ?
  private static final String KEY_STYLE = "CssResource.style";
  private static final String KEY_OBFUSCATION_PREFIX = "CssResource.obfuscationPrefix";
  private static final String KEY_CLASS_PREFIX = "prefix";
  private static final char[] BASE32_CHARS = new char[] {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', '0', '1',
      '2', '3', '4', '5', '6'};

  private static String encode(long id) {
    assert id >= 0;

    StringBuilder b = new StringBuilder();

    // Use only guaranteed-alpha characters for the first character
    b.append(BASE32_CHARS[(int) (id & 0xf)]);
    id >>= 4;

    while (id != 0) {
      b.append(BASE32_CHARS[(int) (id & 0x1f)]);
      id >>= 5;
    }

    return b.toString();
  }

  private Map<JMethod, ExtendedCssTree> cssTreeMap;
  private Set<String> allowedNonStandardFunctions;
  private LoggerErrorManager errorManager;
  private JMethod getTextMethod;
  private JMethod ensuredInjectedMethod;
  private JMethod getNameMethod;
  private String obfuscationPrefix;
  // TODO for the time being we just support one mode of class name obfuscation. boolean is enough
  private boolean obfuscateClassName;
  // TODO add the possiblity to the developper to define his own at-rule ?
  private Set<String> allowedAtRules;

  @Override
  public String createAssignment(TreeLogger logger, ResourceContext context, JMethod method)
      throws UnableToCompleteException {
    ExtendedCssTree extendedCssTree = cssTreeMap.get(method);

    checkForUnknownCssClasses(method, extendedCssTree);

    // TODO check if this can be done earlier (in the prepare method) ?
    Map<String, String> substitutionMap = doClassRenaming(extendedCssTree.getCssTree(), method);

    // TODO : Should we foresee configuration properties for simplyfyCss and eliminateDeadCode
    // booleans ?
    OptimizationInfo optimizationInfo = optimize(extendedCssTree, context, true, true);

    checkErrors();

    SourceWriter sw = new StringSourceWriter();
    sw.println("new " + method.getReturnType().getQualifiedSourceName() + "() {");
    sw.indent();

    writeMethods(logger, context, method, sw, optimizationInfo, substitutionMap);

    sw.outdent();
    sw.println("}");

    return sw.toString();
  }

  @Override
  public void init(TreeLogger logger, ResourceContext context) throws UnableToCompleteException {
    cssTreeMap = new IdentityHashMap<JMethod, ExtendedCssTree>();
    errorManager = new LoggerErrorManager(logger);
    // TODO : add ability to developpers to add non standard functions
    // (like "progid:DXImageTransform.Microsoft.gradient") via configuration property
    allowedNonStandardFunctions = Sets.newHashSet();
    allowedAtRules = Sets.newHashSet(ExternalClassesCollector.EXTERNAL_AT_RULE);

    try {
      PropertyOracle propertyOracle = context.getGeneratorContext().getPropertyOracle();
      ConfigurationProperty styleProp = propertyOracle.getConfigurationProperty(KEY_STYLE);
      obfuscateClassName = CssObfuscationStyle.getObfuscationStyle(styleProp.getValues().get(0))
          == CssObfuscationStyle.OBFUSCATED;
      obfuscationPrefix = getObfuscationPrefix(propertyOracle, context);

      ClientBundleRequirements requirements = context.getRequirements();
      requirements.addConfigurationProperty(KEY_STYLE);
    } catch (BadPropertyValueException e) {
      logger.log(TreeLogger.ERROR, "Unable to query module property", e);
      throw new UnableToCompleteException();
    }

    TypeOracle typeOracle = context.getGeneratorContext().getTypeOracle();
    JClassType cssResourceInterface = typeOracle.findType(CssResource.class.getCanonicalName());
    JClassType resourcePrototypeInterface = typeOracle.findType(ResourcePrototype.class
        .getCanonicalName());

    try {
      getTextMethod = cssResourceInterface.getMethod("getText", new JType[0]);
      ensuredInjectedMethod = cssResourceInterface.getMethod("ensureInjected", new JType[0]);
      getNameMethod = resourcePrototypeInterface.getMethod("getName", new JType[0]);
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, "Unable to lookup methods from CssResource and " +
          "ResourcePrototype interface", e);
      throw new UnableToCompleteException();
    }
  }

  private String getObfuscationPrefix(PropertyOracle propertyOracle, ResourceContext context)
      throws BadPropertyValueException {
    String prefix = propertyOracle.getConfigurationProperty(KEY_OBFUSCATION_PREFIX)
        .getValues().get(0);
    if ("empty".equalsIgnoreCase(prefix)) {
      return "";
    } else if ("default".equalsIgnoreCase(prefix)) {
      return getDefaultObfuscationPrefix(context);
    }

    return prefix;
  }

  private String getDefaultObfuscationPrefix(ResourceContext context) {
    String prefix = context.getCachedData(KEY_CLASS_PREFIX, String.class);
    if (prefix == null) {
      prefix = computeDefaultPrefix(context);
      context.putCachedData(KEY_CLASS_PREFIX, prefix);
    }

    return prefix;
  }

  private String computeDefaultPrefix(ResourceContext context) {
    SortedSet<JClassType> gssResources = computeOperableTypes(context);

    Adler32 checksum = new Adler32();

    for (JClassType type : gssResources) {
      checksum.update(Util.getBytes(type.getQualifiedSourceName()));
    }

    int seed = Math.abs((int) checksum.getValue());

    return encode(seed) + "-";
  }

  private SortedSet<JClassType> computeOperableTypes(ResourceContext context) {
    TypeOracle typeOracle = context.getGeneratorContext().getTypeOracle();
    JClassType baseInterface = typeOracle.findType(GssResource.class.getCanonicalName());

    SortedSet<JClassType> toReturn = new TreeSet<JClassType>(new JClassOrderComparator());

    JClassType[] cssResourceSubtypes = baseInterface.getSubtypes();
    for (JClassType type : cssResourceSubtypes) {
      if (type.isInterface() != null) {
        toReturn.add(type);
      }
    }

    return toReturn;
  }

  @Override
  public void prepare(final TreeLogger logger, final ResourceContext context,
      ClientBundleRequirements requirements, JMethod method) throws UnableToCompleteException {

    if (method.getReturnType().isInterface() == null) {
      logger.log(TreeLogger.ERROR, "Return type must be an interface");
      throw new UnableToCompleteException();
    }

    URL[] resourceUrls = ResourceGeneratorUtil.findResources(logger, context, method);
    if (resourceUrls.length == 0) {
      logger.log(TreeLogger.ERROR, "At least one source must be specified");
      throw new UnableToCompleteException();
    }

    final long lastModified = ResourceGeneratorUtil.getLastModified(resourceUrls, logger);
    final List<URL> resources = Lists.newArrayList(resourceUrls);

    maybeInvalidateCacheFor(resources, lastModified, logger);

    ExtendedCssTree extTree;

    try {
      extTree = TREE_CACHE.get(resources, new Callable<ExtendedCssTree>() {
        @Override
        public ExtendedCssTree call() throws Exception {
          ExtendedCssTree tree = parseResources(resources, logger);
          // add last modified time in cache
          LAST_MODIFIED_CACHE.put(resources, lastModified);
          return tree;
        }
      });
    } catch (ExecutionException e) {
      if (e.getCause() instanceof UnableToCompleteException) {
        throw (UnableToCompleteException) e.getCause();
      } else {
        logger.log(Type.ERROR, "Unexpected error occurred", e.getCause());
        throw new UnableToCompleteException();
      }
    }

    ExtendedCssTree finalTree = new ExtendedCssTree(deepCopy(extTree.getCssTree()),
        extTree.getPermutationAxes());
    cssTreeMap.put(method, finalTree);

    for (String permutationAxis : extTree.getPermutationAxes()) {
      try {
        context.getRequirements().addPermutationAxis(permutationAxis);
      } catch (BadPropertyValueException e) {
        logger.log(TreeLogger.ERROR, "Unknown deferred-binding property " + permutationAxis, e);
        throw new UnableToCompleteException();
      }
    }
  }

  @Override
  protected String getCssExpression(TreeLogger logger, ResourceContext context,
      JMethod method) throws UnableToCompleteException {
    CssTree cssTree = cssTreeMap.get(method).getCssTree();

    String standard = printCssTree(cssTree);

    // TODO add configuration properties for swapLtrRtlInUrl, swapLeftRightInUrl and
    // shouldFlipConstantReferences booleans
    RecordingBidiFlipper recordingBidiFlipper =
        new RecordingBidiFlipper(cssTree.getMutatingVisitController(), false, false, true);
    recordingBidiFlipper.runPass();

    if (recordingBidiFlipper.nodeFlipped()) {
      String reversed = printCssTree(cssTree);
      return LocaleInfo.class.getName() + ".getCurrentLocale().isRTL() ? "
          + reversed + " : " + standard;
    } else {
      return standard;
    }
  }

  private void checkErrors() throws UnableToCompleteException {
    if (errorManager.hasErrors()) {
      throw new UnableToCompleteException();
    }
  }

  private CssTree deepCopy(CssTree cssTree) {
    return new CssTree(cssTree.getSourceCode(), cssTree.getRoot().deepCopy());
  }

  private Map<String, String> doClassRenaming(CssTree cssTree, JMethod method) {
    Set<String> externalClasses = collectExternalClasses(cssTree);

    // for the time being, either we don 't rename the classes either we obfuscate them
    // we can (and should) implement our own SubstitutionMap to handle another obfuscation scheme
    SubstitutionMap substitutionMap;
    if (obfuscateClassName) {
      // it renames CSS classes to the shortest string possible. No conflict possible
      substitutionMap = new MinimalSubstitutionMap();
    } else {
      // map the class name to itself (no renaming)
      substitutionMap = new IdentitySubstitutionMap();
    }

    // TODO: compute an automatic  obfuscation prefix if the obfuscationPrefix is null
    if (obfuscationPrefix == null) {
      obfuscationPrefix = "";
    }

    String resourcePrefix = resourcePrefixBuilder.get(method.getReturnType()
        .getQualifiedSourceName());
    // This substitution map will prefix each renamed class with the resource prefix
    SubstitutionMap prefixingSubstitutionMap = new PrefixingSubstitutionMap(substitutionMap,
        obfuscationPrefix + resourcePrefix + "-");

    RecordingSubstitutionMap recordingSubstitutionMap = new RecordingSubstitutionMap
        (prefixingSubstitutionMap, Predicates.not(Predicates.in(externalClasses)));

    new CssClassRenaming(cssTree.getMutatingVisitController(), recordingSubstitutionMap, null)
        .runPass();

    Map<String, String> mapping = Maps.newHashMap(recordingSubstitutionMap.getMappings());

    // add external classes in the mapping
    for (String external : externalClasses) {
      mapping.put(external, external);
    }

    return mapping;
  }

  private Set<String> collectExternalClasses(CssTree cssTree) {
    ExternalClassesCollector externalClassesCollector = new ExternalClassesCollector(cssTree
        .getMutatingVisitController());

    externalClassesCollector.runPass();

    return externalClassesCollector.getExternalClassNames();
  }

  private List<String> finalizeTree(CssTree cssTree) throws UnableToCompleteException {
    new CheckDependencyNodes(cssTree.getMutatingVisitController(), errorManager, false).runPass();

    // Don't continue if errors exist
    checkErrors();

    new CreateStandardAtRuleNodes(cssTree.getMutatingVisitController(), errorManager).runPass();
    new CreateMixins(cssTree.getMutatingVisitController(), errorManager).runPass();
    new CreateDefinitionNodes(cssTree.getMutatingVisitController(), errorManager).runPass();
    new CreateConstantReferences(cssTree.getMutatingVisitController()).runPass();
    new CreateConditionalNodes(cssTree.getMutatingVisitController(), errorManager).runPass();
    new CreateComponentNodes(cssTree.getMutatingVisitController(), errorManager).runPass();

    new HandleUnknownAtRuleNodes(cssTree.getMutatingVisitController(), errorManager,
        allowedAtRules, true, false).runPass();
    new ProcessKeyframes(cssTree.getMutatingVisitController(), errorManager, true, true).runPass();
    new ProcessRefiners(cssTree.getMutatingVisitController(), errorManager, true).runPass();

    PermutationsCollector permutationsCollector = new PermutationsCollector(cssTree
        .getMutatingVisitController(), errorManager);
    permutationsCollector.runPass();

    return permutationsCollector.getPermutationAxes();
  }

  private void maybeInvalidateCacheFor(List<URL> resources, long lastModified, TreeLogger logger) {
    Long lastModifiedFromCache = LAST_MODIFIED_CACHE.getIfPresent(resources);

    if (lastModifiedFromCache == null || lastModified == 0 || (lastModified >
        lastModifiedFromCache)) {
      TREE_CACHE.invalidate(resources);
    }
  }

  private OptimizationInfo optimize(ExtendedCssTree extendedCssTree, ResourceContext context,
      boolean simplifyCss,  boolean eliminateDeadStyles) {
    CssTree cssTree = extendedCssTree.getCssTree();

    // Collect mixin definitions and replace mixins
    CollectMixinDefinitions collectMixinDefinitions = new CollectMixinDefinitions(
        cssTree.getMutatingVisitController(), errorManager);
    collectMixinDefinitions.runPass();
    new ReplaceMixins(cssTree.getMutatingVisitController(), errorManager,
        collectMixinDefinitions.getDefinitions()).runPass();

    new ProcessComponents<Object>(cssTree.getMutatingVisitController(), errorManager).runPass();

    new EliminateConditionalNodes(cssTree.getMutatingVisitController(),
        getPermutationsConditions(context, extendedCssTree.getPermutationAxes())).runPass();

    CollectConstantDefinitions collectConstantDefinitionsPass = new CollectConstantDefinitions(
        cssTree);
    collectConstantDefinitionsPass.runPass();

    ReplaceConstantReferences replaceConstantReferences = new ReplaceConstantReferences(cssTree,
        collectConstantDefinitionsPass.getConstantDefinitions(), true, errorManager, false);
    replaceConstantReferences.runPass();

    new ImageSpriteCreator(cssTree.getMutatingVisitController(), context, errorManager).runPass();

    Map<String, GssFunction> gssFunctionMap = new GwtGssFunctionMapProvider(context).get();
    new ResolveCustomFunctionNodes(cssTree.getMutatingVisitController(), errorManager,
        gssFunctionMap, true, allowedNonStandardFunctions).runPass();

    if (simplifyCss) {
      // Eliminate empty rules.
      new EliminateEmptyRulesetNodes(cssTree.getMutatingVisitController()).runPass();
      // Eliminating units for zero values.
      new EliminateUnitsFromZeroNumericValues(cssTree.getMutatingVisitController()).runPass();
      // Optimize color values.
      new ColorValueOptimizer(cssTree.getMutatingVisitController()).runPass();
      // Compress redundant top-right-bottom-left value lists.
      new AbbreviatePositionalValues(cssTree.getMutatingVisitController()).runPass();
    }

    if (eliminateDeadStyles) {
      // Report errors for duplicate declarations
      new DisallowDuplicateDeclarations(cssTree.getVisitController(), errorManager).runPass();
      // Split rules by selector and declaration.
      new SplitRulesetNodes(cssTree.getMutatingVisitController()).runPass();
      // Dead code elimination.
      new MarkRemovableRulesetNodes(cssTree).runPass();
      new EliminateUselessRulesetNodes(cssTree).runPass();
      // Merge of rules with same selector.
      new MergeAdjacentRulesetNodesWithSameSelector(cssTree).runPass();
      new EliminateUselessRulesetNodes(cssTree).runPass();
      // Merge of rules with same styles.
      new MergeAdjacentRulesetNodesWithSameDeclarations(cssTree).runPass();
      new EliminateUselessRulesetNodes(cssTree).runPass();
    }

    return new OptimizationInfo(collectConstantDefinitionsPass.getConstantDefinitions());
  }

  private Set<String> getPermutationsConditions(ResourceContext context,
      List<String> permutationAxes) {
    Builder<String> setBuilder = ImmutableSet.builder();
    PropertyOracle oracle = context.getGeneratorContext().getPropertyOracle();

    for (String permutationAxis : permutationAxes) {
      String propValue = null;
      try {
        SelectionProperty selProp = oracle.getSelectionProperty(null,
            permutationAxis);
        propValue = selProp.getCurrentValue();
      } catch (BadPropertyValueException e) {
        try {
          ConfigurationProperty confProp = oracle.getConfigurationProperty(permutationAxis);
          propValue = confProp.getValues().get(0);
        } catch (BadPropertyValueException e1) {
          e1.printStackTrace();
        }
      }

      if (propValue != null) {
        setBuilder.add(permutationAxis + ":" + propValue);
      }
    }
    return setBuilder.build();
  }

  private ExtendedCssTree parseResources(List<URL> resources, TreeLogger logger)
      throws UnableToCompleteException {
    List<SourceCode> sourceCodes = new ArrayList<SourceCode>(resources.size());

    for (URL stylesheet : resources) {
      TreeLogger branchLogger = logger.branch(TreeLogger.DEBUG,
          "Parsing GSS stylesheet " + stylesheet.toExternalForm());

      try {
        // TODO : always use UTF-8 to read the file ?
        String fileContent = Resources.asByteSource(stylesheet).asCharSource(Charsets.UTF_8)
            .read();
        sourceCodes.add(new SourceCode(stylesheet.getFile(), fileContent));
        continue;

      } catch (IOException e) {
        branchLogger.log(TreeLogger.ERROR, "Unable to parse CSS", e);
      }
      throw new UnableToCompleteException();
    }

    CssTree tree;

    try {
      tree = new GssParser(sourceCodes).parse();
    } catch (GssParserException e) {
      logger.log(TreeLogger.ERROR, "Unable to parse CSS", e);
      throw new UnableToCompleteException();
    }

    List<String> permutationAxes = finalizeTree(tree);

    checkErrors();

    return new ExtendedCssTree(tree, permutationAxes);
  }

  private String printCssTree(CssTree tree) {
    CssPrinter cssPrinterPass = new CssPrinter(tree);
    cssPrinterPass.runPass();

    return cssPrinterPass.getCompactPrintedString();
  }

  private boolean writeClassMethod(TreeLogger logger, JMethod userMethod,
      Map<String, String> substitutionMap, SourceWriter sw) throws
      UnableToCompleteException {

    if (!isReturnTypeString(userMethod.getReturnType().isClass())) {
      logger.log(Type.ERROR, "The return type of the method [" + userMethod.getName() + "] must " +
          "be java.lang.String.");
      throw new UnableToCompleteException();
    }

    if (userMethod.getParameters().length > 0) {
      logger.log(Type.ERROR, "The method [" + userMethod.getName() + "] shouldn't contain any " +
          "parameters");
      throw new UnableToCompleteException();
    }

    String name = userMethod.getName();

    ClassName classNameOverride = userMethod.getAnnotation(ClassName.class);
    if (classNameOverride != null) {
      name = classNameOverride.value();
    }

    String value = substitutionMap.get(name);

    if (value == null) {
      logger.log(Type.ERROR, "The following style class [" + name + "] is missing from the source" +
          " CSS file");
      return false;
    } else {
      writeSimpleGetter(userMethod, "\"" + value + "\"", sw);
    }

    return true;
  }

  private boolean writeDefMethod(CssDefinitionNode definitionNode, TreeLogger logger,
      JMethod userMethod, SourceWriter sw) throws UnableToCompleteException {

    String name = userMethod.getName();

    JClassType classReturnType = userMethod.getReturnType().isClass();
    List<CssValueNode> params = definitionNode.getParameters();

    if (params.size() != 1 && !isReturnTypeString(classReturnType)) {
      logger.log(TreeLogger.ERROR, "@def rule " + name
          + " must define exactly one value or return type must be String");
      return false;
    }

    String returnExpr;
    if (isReturnTypeString(classReturnType)) {
      List<String> returnValues = new ArrayList<String>();
      for (CssValueNode valueNode : params) {
        returnValues.add(Generator.escape(valueNode.toString()));
      }
      returnExpr = "\"" + Joiner.on(" ").join(returnValues) + "\"";
    } else {
      JPrimitiveType returnType = userMethod.getReturnType().isPrimitive();
      if (returnType == null) {
        logger.log(TreeLogger.ERROR, name + ": Return type must be primitive type " +
            "or String for @def accessors");
        return false;
      }
      CssValueNode valueNode = params.get(0);
      if (!(valueNode instanceof CssNumericNode)) {
        logger.log(TreeLogger.ERROR, "The value of the constant defined by @" + name + " is not a" +
            " numeric");
        return false;
      }
      String numericValue = ((CssNumericNode) valueNode).getNumericPart();

      if (returnType == JPrimitiveType.INT || returnType == JPrimitiveType.LONG) {
        returnExpr = "" + Long.parseLong(numericValue);
      } else if (returnType == JPrimitiveType.FLOAT) {
        returnExpr = numericValue + "F";
      } else if (returnType == JPrimitiveType.DOUBLE) {
        returnExpr = "" + numericValue;
      } else {
        logger.log(TreeLogger.ERROR, returnType.getQualifiedSourceName()
            + " is not a valid primitive return type for @def accessors");
        return false;
      }
    }

    writeSimpleGetter(userMethod, returnExpr, sw);

    return true;
  }

  private void writeMethods(TreeLogger logger, ResourceContext context, JMethod method,
      SourceWriter sw, OptimizationInfo optimizationInfo, Map<String, String> substitutionMap)
      throws UnableToCompleteException {
    JClassType gssResource = method.getReturnType().isInterface();

    boolean success = true;

    for (JMethod toImplement : gssResource.getOverridableMethods()) {
      if (toImplement == getTextMethod) {
        writeGetText(logger, context, method, sw);
      } else if (toImplement == ensuredInjectedMethod) {
        writeEnsureInjected(sw);
      } else if (toImplement == getNameMethod) {
        writeGetName(method, sw);
      } else {
        success &= writeUserMethod(logger, toImplement, sw, optimizationInfo,
            substitutionMap);
      }
    }

    if (!success) {
      throw new UnableToCompleteException();
    }
  }

  private boolean writeUserMethod(TreeLogger logger, JMethod userMethod,
      SourceWriter sw, OptimizationInfo optimizationInfo, Map<String, String> substitutionMap)
      throws UnableToCompleteException {

    ConstantDefinitions constantDefinitions = optimizationInfo.constantDefinitions;
    CssDefinitionNode definitionNode = constantDefinitions.getConstantDefinition(userMethod.getName());

    if (definitionNode != null) {
      return writeDefMethod(definitionNode, logger, userMethod, sw);
    }

    return writeClassMethod(logger, userMethod, substitutionMap, sw);
  }

  private void checkForUnknownCssClasses(JMethod method, ExtendedCssTree extendedCssTree)
          throws UnableToCompleteException {
    JClassType returnType = (JClassType) method.getReturnType();

    JMethod[] methods = returnType.getMethods();

    ImmutableList.Builder<String> methodNamesBuilder = ImmutableList.builder();

    for (JMethod jMethod : methods) {
      methodNamesBuilder.add(jMethod.getName());
    }

    new UnassignedCssClassVisitor(extendedCssTree.getCssTree().getMutatingVisitController(),
            methodNamesBuilder.build(), errorManager)
            .runPass();

    checkErrors();
  }
}
