/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.napile.compiler.di;

import javax.annotation.PreDestroy;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.descriptors.ModuleDescriptor;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.resolve.NamespaceFactoryImpl;
import org.napile.compiler.lang.resolve.TopDownAnalysisContext;
import org.napile.compiler.lang.resolve.TopDownAnalysisParameters;
import org.napile.compiler.lang.resolve.TopDownAnalyzer;
import org.napile.compiler.lang.resolve.calls.CallResolver;
import org.napile.compiler.lang.resolve.calls.OverloadingConflictResolver;
import org.napile.compiler.lang.resolve.processors.*;
import org.napile.compiler.lang.types.expressions.ExpressionTypingServices;
import com.intellij.openapi.project.Project;

/* This file is generated by org.jetbrains.jet.di.AllInjectorsGenerator. DO NOT EDIT! */
public class InjectorForTopDownAnalyzerBasic
{
	private TopDownAnalyzer topDownAnalyzer;
	private TopDownAnalysisContext topDownAnalysisContext;
	private BodyResolver bodyResolver;
	private ControlFlowAnalyzer controlFlowAnalyzer;
	private DeclarationsChecker declarationsChecker;
	private DescriptorResolver descriptorResolver;
	private final Project project;
	private final TopDownAnalysisParameters topDownAnalysisParameters;
	private final BindingTrace bindingTrace;
	private final ModuleDescriptor moduleDescriptor;
	private NamespaceFactoryImpl namespaceFactory;
	private DeclarationResolver declarationResolver;
	private AnnotationResolver annotationResolver;
	private CallResolver callResolver;
	private ExpressionTypingServices expressionTypingServices;
	private TypeResolver typeResolver;
	private QualifiedExpressionResolver qualifiedExpressionResolver;
	private OverloadingConflictResolver overloadingConflictResolver;
	private ImportsResolver importsResolver;
	private OverloadResolver overloadResolver;
	private OverrideResolver overrideResolver;
	private TypeHierarchyResolver typeHierarchyResolver;
	private AnnotationChecker annotationChecker;
	private EnumEntryChecker enumEntryChecker;
	private ModifiersChecker modifiersChecker;

	public InjectorForTopDownAnalyzerBasic(@NotNull Project project,@NotNull TopDownAnalysisParameters topDownAnalysisParameters,@NotNull BindingTrace bindingTrace,@NotNull ModuleDescriptor moduleDescriptor)
	{
		this.topDownAnalyzer = new TopDownAnalyzer();
		this.topDownAnalysisContext = new TopDownAnalysisContext();
		this.bodyResolver = new BodyResolver();
		this.controlFlowAnalyzer = new ControlFlowAnalyzer();
		this.declarationsChecker = new DeclarationsChecker();
		this.descriptorResolver = new DescriptorResolver();
		this.project = project;
		this.topDownAnalysisParameters = topDownAnalysisParameters;
		this.bindingTrace = bindingTrace;
		this.moduleDescriptor = moduleDescriptor;
		this.namespaceFactory = new NamespaceFactoryImpl();
		this.declarationResolver = new DeclarationResolver();
		this.annotationResolver = new AnnotationResolver();
		this.callResolver = new CallResolver();
		this.expressionTypingServices = new ExpressionTypingServices();
		this.typeResolver = new TypeResolver();
		this.qualifiedExpressionResolver = new QualifiedExpressionResolver();
		this.overloadingConflictResolver = new OverloadingConflictResolver();
		this.importsResolver = new ImportsResolver();
		this.overloadResolver = new OverloadResolver();
		this.overrideResolver = new OverrideResolver();
		this.typeHierarchyResolver = new TypeHierarchyResolver();
		this.annotationChecker = new AnnotationChecker();
		this.enumEntryChecker = new EnumEntryChecker();
		this.modifiersChecker = new ModifiersChecker();

		this.topDownAnalyzer.setBodyResolver(bodyResolver);
		this.topDownAnalyzer.setContext(topDownAnalysisContext);
		this.topDownAnalyzer.setDeclarationResolver(declarationResolver);
		this.topDownAnalyzer.setModuleDescriptor(moduleDescriptor);
		this.topDownAnalyzer.setNamespaceFactory(namespaceFactory);
		this.topDownAnalyzer.setOverloadResolver(overloadResolver);
		this.topDownAnalyzer.setOverrideResolver(overrideResolver);
		this.topDownAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
		this.topDownAnalyzer.setTrace(bindingTrace);
		this.topDownAnalyzer.setTypeHierarchyResolver(typeHierarchyResolver);

		this.topDownAnalysisContext.setTopDownAnalysisParameters(topDownAnalysisParameters);

		this.bodyResolver.setAnnotationChecker(annotationChecker);
		this.bodyResolver.setCallResolver(callResolver);
		this.bodyResolver.setControlFlowAnalyzer(controlFlowAnalyzer);
		this.bodyResolver.setDeclarationsChecker(declarationsChecker);
		this.bodyResolver.setDescriptorResolver(descriptorResolver);
		this.bodyResolver.setEnumEntryChecker(enumEntryChecker);
		this.bodyResolver.setExpressionTypingServices(expressionTypingServices);
		this.bodyResolver.setModifiersChecker(modifiersChecker);
		this.bodyResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
		this.bodyResolver.setTrace(bindingTrace);

		this.controlFlowAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
		this.controlFlowAnalyzer.setTrace(bindingTrace);

		this.declarationsChecker.setTrace(bindingTrace);

		this.descriptorResolver.setAnnotationResolver(annotationResolver);
		this.descriptorResolver.setExpressionTypingServices(expressionTypingServices);
		this.descriptorResolver.setTypeResolver(typeResolver);

		namespaceFactory.setModuleDescriptor(moduleDescriptor);
		namespaceFactory.setTrace(bindingTrace);

		declarationResolver.setAnnotationResolver(annotationResolver);
		declarationResolver.setContext(topDownAnalysisContext);
		declarationResolver.setDescriptorResolver(descriptorResolver);
		declarationResolver.setImportsResolver(importsResolver);
		declarationResolver.setTrace(bindingTrace);
		declarationResolver.setTypeResolver(typeResolver);

		annotationResolver.setCallResolver(callResolver);
		annotationResolver.setExpressionTypingServices(expressionTypingServices);

		callResolver.setDescriptorResolver(descriptorResolver);
		callResolver.setExpressionTypingServices(expressionTypingServices);
		callResolver.setOverloadingConflictResolver(overloadingConflictResolver);
		callResolver.setTypeResolver(typeResolver);

		expressionTypingServices.setCallResolver(callResolver);
		expressionTypingServices.setDescriptorResolver(descriptorResolver);
		expressionTypingServices.setProject(project);
		expressionTypingServices.setTypeResolver(typeResolver);

		typeResolver.setAnnotationResolver(annotationResolver);
		typeResolver.setDescriptorResolver(descriptorResolver);
		typeResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);

		importsResolver.setContext(topDownAnalysisContext);
		importsResolver.setProject(project);
		importsResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);
		importsResolver.setTrace(bindingTrace);

		overloadResolver.setContext(topDownAnalysisContext);
		overloadResolver.setTrace(bindingTrace);

		overrideResolver.setContext(topDownAnalysisContext);
		overrideResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
		overrideResolver.setTrace(bindingTrace);

		typeHierarchyResolver.setContext(topDownAnalysisContext);
		typeHierarchyResolver.setDescriptorResolver(descriptorResolver);
		typeHierarchyResolver.setImportsResolver(importsResolver);
		typeHierarchyResolver.setNamespaceFactory(namespaceFactory);
		typeHierarchyResolver.setTrace(bindingTrace);

		annotationChecker.setTrace(bindingTrace);

		enumEntryChecker.setCallResolver(callResolver);
		enumEntryChecker.setTrace(bindingTrace);

		modifiersChecker.setTrace(bindingTrace);

	}

	@PreDestroy
	public void destroy()
	{
	}

	public TopDownAnalyzer getTopDownAnalyzer()
	{
		return this.topDownAnalyzer;
	}

	public TopDownAnalysisContext getTopDownAnalysisContext()
	{
		return this.topDownAnalysisContext;
	}

	public BodyResolver getBodyResolver()
	{
		return this.bodyResolver;
	}

	public ControlFlowAnalyzer getControlFlowAnalyzer()
	{
		return this.controlFlowAnalyzer;
	}

	public DeclarationsChecker getDeclarationsChecker()
	{
		return this.declarationsChecker;
	}

	public DescriptorResolver getDescriptorResolver()
	{
		return this.descriptorResolver;
	}

	public Project getProject()
	{
		return this.project;
	}

	public TopDownAnalysisParameters getTopDownAnalysisParameters()
	{
		return this.topDownAnalysisParameters;
	}

	public BindingTrace getBindingTrace()
	{
		return this.bindingTrace;
	}

}
