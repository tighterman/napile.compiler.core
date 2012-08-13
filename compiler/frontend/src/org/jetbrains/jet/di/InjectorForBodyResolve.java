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


package org.jetbrains.jet.di;

import javax.annotation.PreDestroy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.AnnotationResolver;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.BodyResolver;
import org.jetbrains.jet.lang.resolve.ControlFlowAnalyzer;
import org.jetbrains.jet.lang.resolve.DeclarationsChecker;
import org.jetbrains.jet.lang.resolve.DescriptorResolver;
import org.jetbrains.jet.lang.resolve.QualifiedExpressionResolver;
import org.jetbrains.jet.lang.resolve.ScriptBodyResolver;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisContext;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisParameters;
import org.jetbrains.jet.lang.resolve.TypeResolver;
import org.jetbrains.jet.lang.resolve.calls.CallResolver;
import org.jetbrains.jet.lang.resolve.calls.OverloadingConflictResolver;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingServices;
import com.intellij.openapi.project.Project;

/* This file is generated by org.jetbrains.jet.di.AllInjectorsGenerator. DO NOT EDIT! */
public class InjectorForBodyResolve
{

	private BodyResolver bodyResolver;
	private final Project project;
	private final TopDownAnalysisParameters topDownAnalysisParameters;
	private final BindingTrace bindingTrace;
	private CallResolver callResolver;
	private DescriptorResolver descriptorResolver;
	private AnnotationResolver annotationResolver;
	private ExpressionTypingServices expressionTypingServices;
	private TypeResolver typeResolver;
	private QualifiedExpressionResolver qualifiedExpressionResolver;
	private OverloadingConflictResolver overloadingConflictResolver;
	private ControlFlowAnalyzer controlFlowAnalyzer;
	private DeclarationsChecker declarationsChecker;
	private ScriptBodyResolver scriptBodyResolver;
	private TopDownAnalysisContext topDownAnalysisContext;

	public InjectorForBodyResolve(@NotNull Project project, @NotNull TopDownAnalysisParameters topDownAnalysisParameters, @NotNull BindingTrace bindingTrace)
	{
		this.bodyResolver = new BodyResolver();
		this.project = project;
		this.topDownAnalysisParameters = topDownAnalysisParameters;
		this.bindingTrace = bindingTrace;
		this.callResolver = new CallResolver();
		this.descriptorResolver = new DescriptorResolver();
		this.annotationResolver = new AnnotationResolver();
		this.expressionTypingServices = new ExpressionTypingServices();
		this.typeResolver = new TypeResolver();
		this.qualifiedExpressionResolver = new QualifiedExpressionResolver();
		this.overloadingConflictResolver = new OverloadingConflictResolver();
		this.controlFlowAnalyzer = new ControlFlowAnalyzer();
		this.declarationsChecker = new DeclarationsChecker();
		this.scriptBodyResolver = new ScriptBodyResolver();
		this.topDownAnalysisContext = new TopDownAnalysisContext();

		this.bodyResolver.setCallResolver(callResolver);
		this.bodyResolver.setControlFlowAnalyzer(controlFlowAnalyzer);
		this.bodyResolver.setDeclarationsChecker(declarationsChecker);
		this.bodyResolver.setDescriptorResolver(descriptorResolver);
		this.bodyResolver.setExpressionTypingServices(expressionTypingServices);
		this.bodyResolver.setScriptBodyResolverResolver(scriptBodyResolver);
		this.bodyResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
		this.bodyResolver.setTrace(bindingTrace);

		callResolver.setDescriptorResolver(descriptorResolver);
		callResolver.setExpressionTypingServices(expressionTypingServices);
		callResolver.setOverloadingConflictResolver(overloadingConflictResolver);
		callResolver.setTypeResolver(typeResolver);

		descriptorResolver.setAnnotationResolver(annotationResolver);
		descriptorResolver.setExpressionTypingServices(expressionTypingServices);
		descriptorResolver.setTypeResolver(typeResolver);

		annotationResolver.setCallResolver(callResolver);
		annotationResolver.setExpressionTypingServices(expressionTypingServices);

		expressionTypingServices.setCallResolver(callResolver);
		expressionTypingServices.setDescriptorResolver(descriptorResolver);
		expressionTypingServices.setProject(project);
		expressionTypingServices.setTypeResolver(typeResolver);

		typeResolver.setAnnotationResolver(annotationResolver);
		typeResolver.setDescriptorResolver(descriptorResolver);
		typeResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);

		controlFlowAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
		controlFlowAnalyzer.setTrace(bindingTrace);

		declarationsChecker.setTrace(bindingTrace);

		scriptBodyResolver.setContext(topDownAnalysisContext);
		scriptBodyResolver.setExpressionTypingServices(expressionTypingServices);
		scriptBodyResolver.setTrace(bindingTrace);

		topDownAnalysisContext.setTopDownAnalysisParameters(topDownAnalysisParameters);

	}

	@PreDestroy
	public void destroy()
	{
	}

	public BodyResolver getBodyResolver()
	{
		return this.bodyResolver;
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
