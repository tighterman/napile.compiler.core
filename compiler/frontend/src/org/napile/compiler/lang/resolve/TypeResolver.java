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

package org.napile.compiler.lang.resolve;

import static org.napile.compiler.lang.diagnostics.Errors.UNSUPPORTED;
import static org.napile.compiler.lang.diagnostics.Errors.WRONG_NUMBER_OF_TYPE_ARGUMENTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.ClassDescriptor;
import org.napile.compiler.lang.descriptors.ClassifierDescriptor;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.TypeParameterDescriptor;
import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
import org.napile.compiler.lang.resolve.scopes.JetScope;
import org.napile.compiler.lang.psi.*;
import org.napile.compiler.lang.resolve.scopes.LazyScopeAdapter;
import org.napile.compiler.lang.types.ErrorUtils;
import org.napile.compiler.lang.types.JetType;
import org.napile.compiler.lang.types.JetTypeImpl;
import org.napile.compiler.lang.types.TypeConstructor;
import org.napile.compiler.lang.types.TypeSubstitutor;
import org.napile.compiler.lang.types.TypeUtils;
import org.napile.compiler.lang.types.lang.JetStandardClasses;
import org.napile.compiler.util.lazy.LazyValue;

/**
 * @author abreslav
 */
public class TypeResolver
{

	private AnnotationResolver annotationResolver;
	private DescriptorResolver descriptorResolver;
	private QualifiedExpressionResolver qualifiedExpressionResolver;

	@Inject
	public void setDescriptorResolver(DescriptorResolver descriptorResolver)
	{
		this.descriptorResolver = descriptorResolver;
	}

	@Inject
	public void setAnnotationResolver(AnnotationResolver annotationResolver)
	{
		this.annotationResolver = annotationResolver;
	}

	@Inject
	public void setQualifiedExpressionResolver(QualifiedExpressionResolver qualifiedExpressionResolver)
	{
		this.qualifiedExpressionResolver = qualifiedExpressionResolver;
	}

	@NotNull
	public JetType resolveType(@NotNull final JetScope scope, @NotNull final NapileTypeReference typeReference, BindingTrace trace, boolean checkBounds)
	{
		JetType cachedType = trace.getBindingContext().get(BindingContext.TYPE, typeReference);
		if(cachedType != null)
			return cachedType;

		final List<AnnotationDescriptor> annotations = annotationResolver.createAnnotationStubs(typeReference.getAnnotations(), trace);

		NapileTypeElement typeElement = typeReference.getTypeElement();
		JetType type = resolveTypeElement(scope, annotations, typeElement, false, trace, checkBounds);
		trace.record(BindingContext.TYPE, typeReference, type);
		trace.record(BindingContext.TYPE_RESOLUTION_SCOPE, typeReference, scope);

		return type;
	}

	@NotNull
	private JetType resolveTypeElement(final JetScope scope, final List<AnnotationDescriptor> annotations, NapileTypeElement typeElement, final boolean nullable, final BindingTrace trace, final boolean checkBounds)
	{

		final JetType[] result = new JetType[1];
		if(typeElement != null)
		{
			typeElement.accept(new NapileVisitorVoid()
			{
				@Override
				public void visitUserType(NapileUserType type)
				{
					NapileSimpleNameExpression referenceExpression = type.getReferenceExpression();
					String referencedName = type.getReferencedName();
					if(referenceExpression == null || referencedName == null)
					{
						return;
					}

					ClassifierDescriptor classifierDescriptor = resolveClass(scope, type, trace);
					if(classifierDescriptor == null)
					{
						resolveTypes(scope, type.getTypeArguments(), trace, checkBounds);
						return;
					}

					if(classifierDescriptor instanceof TypeParameterDescriptor)
					{
						TypeParameterDescriptor typeParameterDescriptor = (TypeParameterDescriptor) classifierDescriptor;

						trace.record(BindingContext.REFERENCE_TARGET, referenceExpression, typeParameterDescriptor);

						JetScope scopeForTypeParameter = getScopeForTypeParameter(typeParameterDescriptor, checkBounds);
						if(scopeForTypeParameter instanceof ErrorUtils.ErrorScope)
						{
							result[0] = ErrorUtils.createErrorType("?");
						}
						else
						{
							result[0] = new JetTypeImpl(annotations, typeParameterDescriptor.getTypeConstructor(), nullable || TypeUtils.hasNullableLowerBound(typeParameterDescriptor), Collections.<JetType>emptyList(), scopeForTypeParameter);
						}

						resolveTypes(scope, type.getTypeArguments(), trace, checkBounds);
					}
					else if(classifierDescriptor instanceof ClassDescriptor)
					{
						ClassDescriptor classDescriptor = (ClassDescriptor) classifierDescriptor;

						trace.record(BindingContext.REFERENCE_TARGET, referenceExpression, classifierDescriptor);
						TypeConstructor typeConstructor = classifierDescriptor.getTypeConstructor();
						List<JetType> arguments = resolveTypes(scope, type.getTypeArguments(), trace, checkBounds);
						List<TypeParameterDescriptor> parameters = typeConstructor.getParameters();
						int expectedArgumentCount = parameters.size();
						int actualArgumentCount = arguments.size();
						if(ErrorUtils.isError(typeConstructor))
						{
							result[0] = ErrorUtils.createErrorType("[Error type: " + typeConstructor + "]");
						}
						else
						{
							if(actualArgumentCount != expectedArgumentCount)
							{
								if(actualArgumentCount == 0)
								{
									trace.report(WRONG_NUMBER_OF_TYPE_ARGUMENTS.on(type, expectedArgumentCount));
								}
								else
								{
									trace.report(WRONG_NUMBER_OF_TYPE_ARGUMENTS.on(type.getTypeArgumentList(), expectedArgumentCount));
								}
							}
							else
							{
								result[0] = new JetTypeImpl(annotations, typeConstructor, nullable, arguments, classDescriptor.getMemberScope(arguments));
								if(checkBounds)
								{
									TypeSubstitutor substitutor = TypeSubstitutor.create(result[0]);
									for(int i = 0, parametersSize = parameters.size(); i < parametersSize; i++)
									{
										TypeParameterDescriptor parameter = parameters.get(i);
										JetType argument = arguments.get(i);
										NapileTypeReference typeReference = type.getTypeArguments().get(i);

										if(typeReference != null)
										{
											descriptorResolver.checkBounds(typeReference, argument, parameter, substitutor, trace);
										}
									}
								}
							}
						}
					}
				}

				@Override
				public void visitNullableType(NapileNullableType nullableType)
				{
					result[0] = resolveTypeElement(scope, annotations, nullableType.getInnerType(), true, trace, checkBounds);
				}

				@Override
				public void visitTupleType(NapileTupleType type)
				{
					// TODO labels
					result[0] = JetStandardClasses.getTupleType(resolveTypes(scope, type.getTypeArguments(), trace, checkBounds));
				}

				@Override
				public void visitFunctionType(NapileFunctionType type)
				{
					NapileTypeReference receiverTypeRef = type.getReceiverTypeRef();
					JetType receiverType = receiverTypeRef == null ? null : resolveType(scope, receiverTypeRef, trace, checkBounds);

					List<JetType> parameterTypes = new ArrayList<JetType>();
					for(NapileParameter parameter : type.getParameters())
					{
						parameterTypes.add(resolveType(scope, parameter.getTypeReference(), trace, checkBounds));
					}

					NapileTypeReference returnTypeRef = type.getReturnTypeRef();
					JetType returnType;
					if(returnTypeRef != null)
					{
						returnType = resolveType(scope, returnTypeRef, trace, checkBounds);
					}
					else
					{
						returnType = JetStandardClasses.getUnitType();
					}
					result[0] = JetStandardClasses.getFunctionType(annotations, receiverType, parameterTypes, returnType);
				}

				@Override
				public void visitSelfType(NapileSelfType type)
				{
					trace.report(UNSUPPORTED.on(type, "Self-types are not supported yet"));
					//                    throw new IllegalArgumentException("Unsupported type: " + element);
				}
			});
		}
		if(result[0] == null)
		{
			return ErrorUtils.createErrorType(typeElement == null ? "No type element" : typeElement.getText());
		}
		if(nullable)
		{
			return TypeUtils.makeNullable(result[0]);
		}
		return result[0];
	}

	private JetScope getScopeForTypeParameter(final TypeParameterDescriptor typeParameterDescriptor, boolean checkBounds)
	{
		if(checkBounds)
		{
			return typeParameterDescriptor.getUpperBoundsAsType().getMemberScope();
		}
		else
		{
			return new LazyScopeAdapter(new LazyValue<JetScope>()
			{
				@Override
				protected JetScope compute()
				{
					return typeParameterDescriptor.getUpperBoundsAsType().getMemberScope();
				}
			});
		}
	}

	private List<JetType> resolveTypes(JetScope scope, List<NapileTypeReference> argumentElements, BindingTrace trace, boolean checkBounds)
	{
		final List<JetType> arguments = new ArrayList<JetType>();
		for(NapileTypeReference argumentElement : argumentElements)
		{
			arguments.add(resolveType(scope, argumentElement, trace, checkBounds));
		}
		return arguments;
	}

	@Nullable
	public ClassifierDescriptor resolveClass(JetScope scope, NapileUserType userType, BindingTrace trace)
	{
		Collection<? extends DeclarationDescriptor> descriptors = qualifiedExpressionResolver.lookupDescriptorsForUserType(userType, scope, trace);
		for(DeclarationDescriptor descriptor : descriptors)
		{
			if(descriptor instanceof ClassifierDescriptor)
			{
				return (ClassifierDescriptor) descriptor;
			}
		}
		return null;
	}
}