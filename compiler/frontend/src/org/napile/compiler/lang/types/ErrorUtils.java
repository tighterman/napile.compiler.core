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

package org.napile.compiler.lang.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
import org.napile.compiler.lang.psi.NapileDelegationSpecifierListOwner;
import org.napile.compiler.lang.resolve.name.LabelName;
import org.napile.compiler.lang.resolve.name.Name;
import org.napile.compiler.lang.descriptors.*;
import org.napile.compiler.lang.resolve.scopes.JetScope;
import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.napile.compiler.lang.types.error.ErrorSimpleFunctionDescriptorImpl;

/**
 * @author abreslav
 */
public class ErrorUtils
{

	private static final ModuleDescriptor ERROR_MODULE = new ModuleDescriptor(Name.special("<ERROR MODULE>"));


	public static class ErrorScope implements JetScope
	{

		private final String debugMessage;

		private ErrorScope(String debugMessage)
		{
			this.debugMessage = debugMessage;
		}

		@Override
		public ClassifierDescriptor getClassifier(@NotNull Name name)
		{
			return ERROR_CLASS;
		}

		@Override
		public ClassDescriptor getObjectDescriptor(@NotNull Name name)
		{
			return ERROR_CLASS;
		}

		@NotNull
		@Override
		public Set<ClassDescriptor> getObjectDescriptors()
		{
			return Collections.emptySet();
		}

		@NotNull
		@Override
		public Set<VariableDescriptor> getProperties(@NotNull Name name)
		{
			return ERROR_PROPERTY_GROUP;
		}

		@Override
		public VariableDescriptor getLocalVariable(@NotNull Name name)
		{
			return ERROR_PROPERTY;
		}

		@Override
		public NamespaceDescriptor getNamespace(@NotNull Name name)
		{
			return null; // TODO : review
		}

		@NotNull
		@Override
		public ReceiverDescriptor getImplicitReceiver()
		{
			return ReceiverDescriptor.NO_RECEIVER;
		}

		@Override
		public void getImplicitReceiversHierarchy(@NotNull List<ReceiverDescriptor> result)
		{
		}

		@NotNull
		@Override
		public Set<FunctionDescriptor> getFunctions(@NotNull Name name)
		{
			return Collections.<FunctionDescriptor>singleton(createErrorFunction(this));
		}

		@NotNull
		@Override
		public DeclarationDescriptor getContainingDeclaration()
		{
			return ERROR_MODULE;
		}

		@NotNull
		@Override
		public Collection<DeclarationDescriptor> getDeclarationsByLabel(LabelName labelName)
		{
			return Collections.emptyList();
		}

		@Override
		public PropertyDescriptor getPropertyByFieldReference(@NotNull Name fieldName)
		{
			return null; // TODO : review
		}

		@NotNull
		@Override
		public Collection<DeclarationDescriptor> getAllDescriptors()
		{
			return Collections.emptyList();
		}

		@NotNull
		@Override
		public Collection<DeclarationDescriptor> getOwnDeclaredDescriptors()
		{
			return Collections.emptyList();
		}
	}

	private static final ClassDescriptorImpl ERROR_CLASS = new ClassDescriptorImpl(ERROR_MODULE, Collections.<AnnotationDescriptor>emptyList(), Modality.OPEN, Name.special("<ERROR CLASS>"), false)
	{
		@NotNull
		@Override
		public Map<NapileDelegationSpecifierListOwner, ConstructorDescriptor> getConstructors()
		{
			return Collections.<NapileDelegationSpecifierListOwner, ConstructorDescriptor>emptyMap();
		}

		@NotNull
		@Override
		public Modality getModality()
		{
			return Modality.OPEN;
		}

		@NotNull
		@Override
		public ClassDescriptor substitute(TypeSubstitutor substitutor)
		{
			return ERROR_CLASS;
		}
	};

	static
	{
		ERROR_CLASS.initialize(true, Collections.<TypeParameterDescriptor>emptyList(), Collections.<JetType>emptyList(), createErrorScope("ERROR_CLASS"), Collections.<NapileDelegationSpecifierListOwner, ConstructorDescriptor>emptyMap());
	}

	public static JetScope createErrorScope(String debugMessage)
	{
		return new ErrorScope(debugMessage);
	}

	private static final JetType ERROR_PROPERTY_TYPE = createErrorType("<ERROR PROPERTY TYPE>");
	private static final VariableDescriptor ERROR_PROPERTY = new PropertyDescriptor(ERROR_CLASS, Collections.<AnnotationDescriptor>emptyList(), Modality.OPEN, Visibilities.PUBLIC, true, false, null, ReceiverDescriptor.NO_RECEIVER, Name.special("<ERROR PROPERTY>"), ERROR_PROPERTY_TYPE, CallableMemberDescriptor.Kind.DECLARATION, false);
	private static final Set<VariableDescriptor> ERROR_PROPERTY_GROUP = Collections.singleton(ERROR_PROPERTY);

	private static SimpleFunctionDescriptor createErrorFunction(ErrorScope ownerScope)
	{
		ErrorSimpleFunctionDescriptorImpl function = new ErrorSimpleFunctionDescriptorImpl(ownerScope);
		function.initialize(null, ReceiverDescriptor.NO_RECEIVER, Collections.<TypeParameterDescriptorImpl>emptyList(), // TODO
				Collections.<ValueParameterDescriptor>emptyList(), // TODO
				createErrorType("<ERROR FUNCTION RETURN TYPE>"), Modality.OPEN, Visibilities.PUBLIC
                /*isInline = */);
		return function;
	}

	private static final JetType ERROR_PARAMETER_TYPE = createErrorType("<ERROR VALUE_PARAMETER TYPE>");

	private static List<ValueParameterDescriptor> getValueParameters(FunctionDescriptor functionDescriptor, List<JetType> argumentTypes)
	{
		List<ValueParameterDescriptor> result = new ArrayList<ValueParameterDescriptor>();
		for(int i = 0, argumentTypesSize = argumentTypes.size(); i < argumentTypesSize; i++)
		{
			result.add(new ValueParameterDescriptorImpl(functionDescriptor, i, Collections.<AnnotationDescriptor>emptyList(), Name.special("<ERROR VALUE_PARAMETER>"), true, ERROR_PARAMETER_TYPE, false, null));
		}
		return result;
	}

	@NotNull
	public static JetType createErrorType(String debugMessage)
	{
		return createErrorType(debugMessage, createErrorScope(debugMessage));
	}

	private static JetType createErrorType(String debugMessage, JetScope memberScope)
	{
		return createErrorTypeWithCustomDebugName(memberScope, "[ERROR : " + debugMessage + "]");
	}

	@NotNull
	public static JetType createErrorTypeWithCustomDebugName(String debugName)
	{
		return createErrorTypeWithCustomDebugName(createErrorScope(debugName), debugName);
	}

	private static JetType createErrorTypeWithCustomDebugName(JetScope memberScope, String debugName)
	{
		return new ErrorTypeImpl(new TypeConstructorImpl(ERROR_CLASS, Collections.<AnnotationDescriptor>emptyList(), false, debugName, Collections.<TypeParameterDescriptorImpl>emptyList(), Collections.<JetType>emptyList()), memberScope);
	}

	public static JetType createWrongVarianceErrorType(JetType value)
	{
		return createErrorType(value + " is not allowed here", value.getMemberScope());
	}

	public static ClassifierDescriptor getErrorClass()
	{
		return ERROR_CLASS;
	}

	public static boolean isError(@NotNull TypeConstructor typeConstructor)
	{
		return typeConstructor == ERROR_CLASS.getTypeConstructor();
	}

	public static boolean isErrorType(@NotNull JetType type)
	{
		return type != TypeUtils.NO_EXPECTED_TYPE && !(type instanceof NamespaceType) &&
				((type instanceof DeferredType && (((DeferredType) type).getActualType() == null || isErrorType(((DeferredType) type).getActualType()))) ||
						type instanceof ErrorTypeImpl ||
						isError(type.getConstructor()));
	}

	public static boolean containsErrorType(@Nullable JetType type)
	{
		if(type == null)
			return false;
		if(type instanceof NamespaceType)
			return false;
		if(isErrorType(type))
			return true;
		for(JetType projection : type.getArguments())
		{
			if(containsErrorType(projection))
				return true;
		}
		return false;
	}

	public static boolean isError(@NotNull DeclarationDescriptor candidate)
	{
		return candidate == getErrorClass() || candidate.getContainingDeclaration() == getErrorClass() || candidate == ERROR_MODULE;
	}

	private static class ErrorTypeImpl implements JetType
	{

		private final TypeConstructor constructor;

		private final JetScope memberScope;

		private ErrorTypeImpl(TypeConstructor constructor, JetScope memberScope)
		{
			this.constructor = constructor;
			this.memberScope = memberScope;
		}

		@NotNull
		@Override
		public TypeConstructor getConstructor()
		{
			return constructor;
		}

		@NotNull
		@Override
		public List<JetType> getArguments()
		{
			return Collections.emptyList();
		}

		@Override
		public boolean isNullable()
		{
			return false;
		}

		@NotNull
		@Override
		public JetScope getMemberScope()
		{
			return memberScope;
		}

		@Override
		public List<AnnotationDescriptor> getAnnotations()
		{
			return Collections.emptyList();
		}

		@Override
		public String toString()
		{
			return constructor.toString();
		}
	}

	private ErrorUtils()
	{
	}
}
