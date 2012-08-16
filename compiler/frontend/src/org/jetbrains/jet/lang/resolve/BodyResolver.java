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

package org.jetbrains.jet.lang.resolve;

import static org.jetbrains.jet.lang.diagnostics.Errors.ANONYMOUS_INITIALIZER_WITHOUT_CONSTRUCTOR;
import static org.jetbrains.jet.lang.diagnostics.Errors.FINAL_SUPERTYPE;
import static org.jetbrains.jet.lang.diagnostics.Errors.SUPERTYPE_APPEARS_TWICE;
import static org.jetbrains.jet.lang.diagnostics.Errors.SUPERTYPE_NOT_INITIALIZED;
import static org.jetbrains.jet.lang.diagnostics.Errors.SUPERTYPE_NOT_INITIALIZED_DEFAULT;
import static org.jetbrains.jet.lang.resolve.BindingContext.DEFERRED_TYPE;
import static org.jetbrains.jet.lang.types.TypeUtils.NO_EXPECTED_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.calls.CallMaker;
import org.jetbrains.jet.lang.resolve.calls.CallResolver;
import org.jetbrains.jet.lang.resolve.calls.OverloadResolutionResults;
import org.jetbrains.jet.lang.resolve.calls.autocasts.DataFlowInfo;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.jetbrains.jet.lang.types.DeferredType;
import org.jetbrains.jet.lang.types.ErrorUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.TypeConstructor;
import org.jetbrains.jet.lang.types.TypeUtils;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingServices;
import org.jetbrains.jet.lexer.JetTokens;
import org.jetbrains.jet.util.Box;
import org.jetbrains.jet.util.lazy.ReenteringLazyValueComputationException;
import org.jetbrains.jet.util.slicedmap.WritableSlice;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.Queue;

/**
 * @author abreslav
 */
public class BodyResolver
{
	@NotNull
	private BodiesResolveContext context;
	@NotNull
	private TopDownAnalysisParameters topDownAnalysisParameters;
	@NotNull
	private DescriptorResolver descriptorResolver;
	@NotNull
	private ExpressionTypingServices expressionTypingServices;
	@NotNull
	private CallResolver callResolver;
	@NotNull
	private ObservableBindingTrace trace;
	@NotNull
	private ControlFlowAnalyzer controlFlowAnalyzer;
	@NotNull
	private DeclarationsChecker declarationsChecker;

	@Inject
	public void setTopDownAnalysisParameters(@NotNull TopDownAnalysisParameters topDownAnalysisParameters)
	{
		this.topDownAnalysisParameters = topDownAnalysisParameters;
	}

	@Inject
	public void setDescriptorResolver(@NotNull DescriptorResolver descriptorResolver)
	{
		this.descriptorResolver = descriptorResolver;
	}

	@Inject
	public void setExpressionTypingServices(@NotNull ExpressionTypingServices expressionTypingServices)
	{
		this.expressionTypingServices = expressionTypingServices;
	}

	@Inject
	public void setCallResolver(@NotNull CallResolver callResolver)
	{
		this.callResolver = callResolver;
	}

	@Inject
	public void setTrace(@NotNull BindingTrace trace)
	{
		this.trace = new ObservableBindingTrace(trace);
	}

	@Inject
	public void setControlFlowAnalyzer(@NotNull ControlFlowAnalyzer controlFlowAnalyzer)
	{
		this.controlFlowAnalyzer = controlFlowAnalyzer;
	}

	@Inject
	public void setDeclarationsChecker(@NotNull DeclarationsChecker declarationsChecker)
	{
		this.declarationsChecker = declarationsChecker;
	}


	private void resolveBehaviorDeclarationBodies(@NotNull BodiesResolveContext bodiesResolveContext)
	{
		// Initialize context
		context = bodiesResolveContext;

		resolveDelegationSpecifierLists();
		resolveClassAnnotations();

		resolvePropertyDeclarationBodies();
		resolveAnonymousInitializers();

		resolveFunctionBodies();

		if(!topDownAnalysisParameters.isDeclaredLocally())
		{
			computeDeferredTypes();
		}
	}

	public void resolveBodies(@NotNull BodiesResolveContext bodiesResolveContext)
	{
		resolveBehaviorDeclarationBodies(bodiesResolveContext);
		controlFlowAnalyzer.process(bodiesResolveContext);
		declarationsChecker.process(bodiesResolveContext);
	}

	private void resolveDelegationSpecifierLists()
	{
		// TODO : Make sure the same thing is not initialized twice
		for(Map.Entry<JetClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
		{
			resolveDelegationSpecifierList(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<JetObjectDeclaration, MutableClassDescriptor> entry : context.getObjects().entrySet())
		{
			resolveDelegationSpecifierList(entry.getKey(), entry.getValue());
		}
	}

	private void resolveDelegationSpecifierList(final JetClassOrObject jetClass, final MutableClassDescriptor descriptor)
	{
		if(!context.completeAnalysisNeeded(jetClass))
			return;
		final ConstructorDescriptor primaryConstructor = descriptor.getUnsubstitutedPrimaryConstructor();
		final JetScope scopeForConstructor = primaryConstructor == null ? null : FunctionDescriptorUtil.getFunctionInnerScope(descriptor.getScopeForSupertypeResolution(), primaryConstructor, trace);
		final ExpressionTypingServices typeInferrer = expressionTypingServices; // TODO : flow

		final Map<JetTypeReference, JetType> supertypes = Maps.newLinkedHashMap();
		JetVisitorVoid visitor = new JetVisitorVoid()
		{
			private void recordSupertype(JetTypeReference typeReference, JetType supertype)
			{
				if(supertype == null)
					return;
				supertypes.put(typeReference, supertype);
			}

			@Override
			public void visitDelegationToSuperCallSpecifier(JetDelegatorToSuperCall call)
			{
				JetValueArgumentList valueArgumentList = call.getValueArgumentList();
				PsiElement elementToMark = valueArgumentList == null ? call : valueArgumentList;

				JetTypeReference typeReference = call.getTypeReference();
				if(typeReference == null)
					return;
				if(descriptor.getUnsubstitutedPrimaryConstructor() == null)
				{
					recordSupertype(typeReference, trace.getBindingContext().get(BindingContext.TYPE, typeReference));
					return;
				}
				OverloadResolutionResults<FunctionDescriptor> results = callResolver.resolveFunctionCall(trace, scopeForConstructor, CallMaker.makeCall(ReceiverDescriptor.NO_RECEIVER, null, call), NO_EXPECTED_TYPE, DataFlowInfo.EMPTY);
				if(results.isSuccess())
				{
					JetType supertype = results.getResultingDescriptor().getReturnType();
					recordSupertype(typeReference, supertype);
				}
				else
				{
					recordSupertype(typeReference, trace.getBindingContext().get(BindingContext.TYPE, typeReference));
				}
			}

			@Override
			public void visitDelegationToSuperClassSpecifier(JetDelegatorToSuperClass specifier)
			{
				if(descriptor.getKind() == ClassKind.CLASS || descriptor.getKind() == ClassKind.ENUM_CLASS)
					return;

				JetTypeReference typeReference = specifier.getTypeReference();
				JetType supertype = trace.getBindingContext().get(BindingContext.TYPE, typeReference);
				recordSupertype(typeReference, supertype);
				if(supertype == null)
					return;
				ClassDescriptor classDescriptor = TypeUtils.getClassDescriptor(supertype);
				if(classDescriptor == null)
					return;
				if(!classDescriptor.getConstructors().isEmpty() && !ErrorUtils.isError(classDescriptor.getTypeConstructor()) )
				{
					boolean hasConstructorWithoutParams = false;
					for(ConstructorDescriptor constructor : classDescriptor.getConstructors())
					{
						if(constructor.getValueParameters().isEmpty())
						{
							hasConstructorWithoutParams = true;
						}
					}
					if(!hasConstructorWithoutParams)
					{
						trace.report(SUPERTYPE_NOT_INITIALIZED.on(specifier));
					}
					else
					{
						trace.report(SUPERTYPE_NOT_INITIALIZED_DEFAULT.on(specifier));
					}
				}
			}

			@Override
			public void visitDelegationToThisCall(JetDelegatorToThisCall thisCall)
			{
				throw new IllegalStateException("This-calls should be prohibited by the parser");
			}

			@Override
			public void visitJetElement(JetElement element)
			{
				throw new UnsupportedOperationException(element.getText() + " : " + element);
			}
		};

		for(JetDelegationSpecifier delegationSpecifier : jetClass.getDelegationSpecifiers())
		{
			delegationSpecifier.accept(visitor);
		}


		Set<TypeConstructor> parentEnum = Collections.emptySet();
		if(jetClass instanceof JetEnumEntry)
		{
			parentEnum = Collections.singleton(((ClassDescriptor) descriptor.getContainingDeclaration().getContainingDeclaration()).getTypeConstructor());
		}

		checkSupertypeList(descriptor, supertypes, parentEnum);
	}

	// allowedFinalSupertypes typically contains a enum type of which supertypeOwner is an entry
	private void checkSupertypeList(@NotNull MutableClassDescriptor supertypeOwner, @NotNull Map<JetTypeReference, JetType> supertypes, Set<TypeConstructor> allowedFinalSupertypes)
	{
		Set<TypeConstructor> typeConstructors = Sets.newHashSet();
		boolean classAppeared = false;
		for(Map.Entry<JetTypeReference, JetType> entry : supertypes.entrySet())
		{
			JetTypeReference typeReference = entry.getKey();
			JetType supertype = entry.getValue();

			TypeConstructor constructor = supertype.getConstructor();
			if(!typeConstructors.add(constructor))
			{
				trace.report(SUPERTYPE_APPEARS_TWICE.on(typeReference));
			}

			if(constructor.isSealed() && !allowedFinalSupertypes.contains(constructor))
			{
				trace.report(FINAL_SUPERTYPE.on(typeReference));
			}
		}
	}

	private void resolveClassAnnotations()
	{
	}

	private void resolveAnonymousInitializers()
	{
		for(Map.Entry<JetClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
		{
			resolveAnonymousInitializers(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<JetObjectDeclaration, MutableClassDescriptor> entry : context.getObjects().entrySet())
		{
			resolveAnonymousInitializers(entry.getKey(), entry.getValue());
		}
	}

	private void resolveAnonymousInitializers(JetClassOrObject jetClassOrObject, MutableClassDescriptor classDescriptor)
	{
		if(!context.completeAnalysisNeeded(jetClassOrObject))
			return;
		List<JetClassInitializer> anonymousInitializers = jetClassOrObject.getAnonymousInitializers();
		if(classDescriptor.getUnsubstitutedPrimaryConstructor() != null)
		{
			ConstructorDescriptor primaryConstructor = classDescriptor.getUnsubstitutedPrimaryConstructor();
			assert primaryConstructor != null;
			final JetScope scopeForInitializers = classDescriptor.getScopeForInitializers();
			for(JetClassInitializer anonymousInitializer : anonymousInitializers)
			{
				expressionTypingServices.getType(scopeForInitializers, anonymousInitializer.getBody(), NO_EXPECTED_TYPE, DataFlowInfo.EMPTY, trace);
			}
		}
		else
		{
			for(JetClassInitializer anonymousInitializer : anonymousInitializers)
			{
				trace.report(ANONYMOUS_INITIALIZER_WITHOUT_CONSTRUCTOR.on(anonymousInitializer));
			}
		}
	}


	private void resolvePropertyDeclarationBodies()
	{

		// Member properties
		Set<JetProperty> processed = Sets.newHashSet();
		for(Map.Entry<JetClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
		{
			JetClass jetClass = entry.getKey();
			if(!context.completeAnalysisNeeded(jetClass))
				continue;
			MutableClassDescriptor classDescriptor = entry.getValue();

			for(JetProperty property : jetClass.getProperties())
			{
				final PropertyDescriptor propertyDescriptor = this.context.getProperties().get(property);
				assert propertyDescriptor != null;

				computeDeferredType(propertyDescriptor.getReturnType());

				JetExpression initializer = property.getInitializer();
				if(initializer != null)
				{
					ConstructorDescriptor primaryConstructor = classDescriptor.getUnsubstitutedPrimaryConstructor();
					if(primaryConstructor != null)
					{
						JetScope declaringScopeForPropertyInitializer = this.context.getDeclaringScopes().get(property);
						resolvePropertyInitializer(property, propertyDescriptor, initializer, declaringScopeForPropertyInitializer);
					}
				}

				resolvePropertyAccessors(property, propertyDescriptor);
				processed.add(property);
			}
		}

		// Top-level properties & properties of objects
		for(Map.Entry<JetProperty, PropertyDescriptor> entry : this.context.getProperties().entrySet())
		{
			JetProperty property = entry.getKey();
			if(!context.completeAnalysisNeeded(property))
				continue;
			if(processed.contains(property))
				continue;

			final PropertyDescriptor propertyDescriptor = entry.getValue();

			computeDeferredType(propertyDescriptor.getReturnType());

			JetScope declaringScope = this.context.getDeclaringScopes().get(property);

			JetExpression initializer = property.getInitializer();
			if(initializer != null)
			{
				resolvePropertyInitializer(property, propertyDescriptor, initializer, declaringScope);
			}

			resolvePropertyAccessors(property, propertyDescriptor);
		}
	}

	private JetScope makeScopeForPropertyAccessor(@NotNull JetPropertyAccessor accessor, PropertyDescriptor propertyDescriptor)
	{
		JetScope declaringScope = context.getDeclaringScopes().get(accessor);

		JetScope propertyDeclarationInnerScope = descriptorResolver.getPropertyDeclarationInnerScope(declaringScope, propertyDescriptor.getTypeParameters(), propertyDescriptor.getReceiverParameter(), trace);
		WritableScope accessorScope = new WritableScopeImpl(propertyDeclarationInnerScope, declaringScope.getContainingDeclaration(), new TraceBasedRedeclarationHandler(trace), "Accessor scope");
		accessorScope.changeLockLevel(WritableScope.LockLevel.READING);

		return accessorScope;
	}

	private void resolvePropertyAccessors(JetProperty property, PropertyDescriptor propertyDescriptor)
	{
		ObservableBindingTrace fieldAccessTrackingTrace = createFieldTrackingTrace(propertyDescriptor);

		JetPropertyAccessor getter = property.getGetter();
		PropertyGetterDescriptor getterDescriptor = propertyDescriptor.getGetter();
		if(getter != null && getterDescriptor != null)
		{
			JetScope accessorScope = makeScopeForPropertyAccessor(getter, propertyDescriptor);
			resolveFunctionBody(fieldAccessTrackingTrace, getter, getterDescriptor, accessorScope);
		}

		JetPropertyAccessor setter = property.getSetter();
		PropertySetterDescriptor setterDescriptor = propertyDescriptor.getSetter();
		if(setter != null && setterDescriptor != null)
		{
			JetScope accessorScope = makeScopeForPropertyAccessor(setter, propertyDescriptor);
			resolveFunctionBody(fieldAccessTrackingTrace, setter, setterDescriptor, accessorScope);
		}
	}

	private ObservableBindingTrace createFieldTrackingTrace(final PropertyDescriptor propertyDescriptor)
	{
		return new ObservableBindingTrace(trace).addHandler(BindingContext.REFERENCE_TARGET, new ObservableBindingTrace.RecordHandler<JetReferenceExpression, DeclarationDescriptor>()
		{
			@Override
			public void handleRecord(WritableSlice<JetReferenceExpression, DeclarationDescriptor> slice, JetReferenceExpression expression, DeclarationDescriptor descriptor)
			{
				if(expression instanceof JetSimpleNameExpression)
				{
					JetSimpleNameExpression simpleNameExpression = (JetSimpleNameExpression) expression;
					if(simpleNameExpression.getReferencedNameElementType() == JetTokens.FIELD_IDENTIFIER)
					{
						// This check may be considered redundant as long as $x is only accessible from accessors to $x
						if(descriptor == propertyDescriptor)
						{ // TODO : original?
							trace.record(BindingContext.BACKING_FIELD_REQUIRED, propertyDescriptor); // TODO: this trace?
						}
					}
				}
			}
		});
	}

	private void resolvePropertyInitializer(JetProperty property, PropertyDescriptor propertyDescriptor, JetExpression initializer, JetScope scope)
	{
		//JetFlowInformationProvider flowInformationProvider = context.getDescriptorResolver().computeFlowData(property, initializer); // TODO : flow JET-15
		JetType expectedTypeForInitializer = property.getPropertyTypeRef() != null ? propertyDescriptor.getType() : NO_EXPECTED_TYPE;
		JetScope propertyDeclarationInnerScope = descriptorResolver.getPropertyDeclarationInnerScope(scope, propertyDescriptor.getTypeParameters(), ReceiverDescriptor.NO_RECEIVER, trace);
		JetType type = expressionTypingServices.getType(propertyDeclarationInnerScope, initializer, expectedTypeForInitializer, DataFlowInfo.EMPTY, trace);
		//
		//        JetType expectedType = propertyDescriptor.getInType();
		//        if (expectedType == null) {
		//            expectedType = propertyDescriptor.getType();
		//        }
		//        if (type != null && expectedType != null
		//            && !context.getSemanticServices().getTypeChecker().isSubtypeOf(type, expectedType)) {
		////            trace.report(TYPE_MISMATCH.on(initializer, expectedType, type));
		//        }
	}

	private void resolveFunctionBodies()
	{
		for(Map.Entry<JetNamedFunction, SimpleFunctionDescriptor> entry : this.context.getFunctions().entrySet())
		{
			JetNamedFunction declaration = entry.getKey();
			SimpleFunctionDescriptor descriptor = entry.getValue();

			computeDeferredType(descriptor.getReturnType());

			JetScope declaringScope = this.context.getDeclaringScopes().get(declaration);
			assert declaringScope != null;

			resolveFunctionBody(trace, declaration, descriptor, declaringScope);

			assert descriptor.getReturnType() != null;
		}
	}

	private void resolveFunctionBody(@NotNull BindingTrace trace, @NotNull JetDeclarationWithBody function, @NotNull FunctionDescriptor functionDescriptor, @NotNull JetScope declaringScope)
	{
		if(!context.completeAnalysisNeeded(function))
			return;

		JetExpression bodyExpression = function.getBodyExpression();
		JetScope functionInnerScope = FunctionDescriptorUtil.getFunctionInnerScope(declaringScope, functionDescriptor, trace);
		if(bodyExpression != null)
		{
			expressionTypingServices.checkFunctionReturnType(functionInnerScope, function, functionDescriptor, DataFlowInfo.EMPTY, null, trace);
		}

		List<JetParameter> valueParameters = function.getValueParameters();
		List<ValueParameterDescriptor> valueParameterDescriptors = functionDescriptor.getValueParameters();

		checkDefaultParameterValues(valueParameters, valueParameterDescriptors, functionInnerScope);

		assert functionDescriptor.getReturnType() != null;
	}

	private void checkDefaultParameterValues(List<JetParameter> valueParameters, List<ValueParameterDescriptor> valueParameterDescriptors, JetScope declaringScope)
	{
		for(int i = 0; i < valueParameters.size(); i++)
		{
			ValueParameterDescriptor valueParameterDescriptor = valueParameterDescriptors.get(i);
			if(valueParameterDescriptor.hasDefaultValue())
			{
				JetParameter jetParameter = valueParameters.get(i);
				JetExpression defaultValue = jetParameter.getDefaultValue();
				if(defaultValue != null)
				{
					expressionTypingServices.getType(declaringScope, defaultValue, valueParameterDescriptor.getType(), DataFlowInfo.EMPTY, trace);
				}
			}
		}
	}

	private static void computeDeferredType(JetType type)
	{
		// handle type inference loop: function or property body contains a reference to itself
		// fun f() = { f() }
		// val x = x
		// type resolution must be started before body resolution
		if(type instanceof DeferredType)
		{
			DeferredType deferredType = (DeferredType) type;
			if(!deferredType.isComputed())
			{
				deferredType.getActualType();
			}
		}
	}

	private void computeDeferredTypes()
	{
		Collection<Box<DeferredType>> deferredTypes = trace.getKeys(DEFERRED_TYPE);
		if(deferredTypes != null)
		{
			// +1 is a work around agains new Queue(0).addLast(...) bug // stepan.koltsov@ 2011-11-21
			final Queue<DeferredType> queue = new Queue<DeferredType>(deferredTypes.size() + 1);
			trace.addHandler(DEFERRED_TYPE, new ObservableBindingTrace.RecordHandler<Box<DeferredType>, Boolean>()
			{
				@Override
				public void handleRecord(WritableSlice<Box<DeferredType>, Boolean> deferredTypeKeyDeferredTypeWritableSlice, Box<DeferredType> key, Boolean value)
				{
					queue.addLast(key.getData());
				}
			});
			for(Box<DeferredType> deferredType : deferredTypes)
			{
				queue.addLast(deferredType.getData());
			}
			while(!queue.isEmpty())
			{
				DeferredType deferredType = queue.pullFirst();
				if(!deferredType.isComputed())
				{
					try
					{
						deferredType.getActualType(); // to compute
					}
					catch(ReenteringLazyValueComputationException e)
					{
						// A problem should be reported while computing the type
					}
				}
			}
		}
	}
}
