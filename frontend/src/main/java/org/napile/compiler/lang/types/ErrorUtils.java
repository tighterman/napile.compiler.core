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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.asm.resolve.name.FqName;
import org.napile.asm.resolve.name.Name;
import org.napile.compiler.lang.descriptors.*;
import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
import org.napile.compiler.lang.resolve.scopes.NapileScope;
import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.napile.compiler.lang.types.error.ErrorSimpleMethodDescriptorImpl;
import org.napile.compiler.lang.types.impl.TypeConstructorImpl;

/**
 * @author abreslav
 */
public class ErrorUtils
{

	private static final ModuleDescriptor ERROR_MODULE = new ModuleDescriptor(Name.special("<ERROR MODULE>"));


	public static class ErrorScope implements NapileScope
	{
		private final String debugMessage;

		private ErrorScope(String debugMessage)
		{
			this.debugMessage = debugMessage;
		}

		@Override
		public ClassDescriptor getClass(@NotNull FqName fqName)
		{
			return ERROR_CLASS;
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
		public Set<VariableDescriptor> getVariables(@NotNull Name name)
		{
			return ERROR_PROPERTY_GROUP;
		}

		@Override
		public VariableDescriptor getLocalVariable(@NotNull Name name)
		{
			return ERROR_PROPERTY;
		}

		@Override
		public PackageDescriptor getPackage(@NotNull Name name)
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
		public Set<MethodDescriptor> getMethods(@NotNull Name name)
		{
			return Collections.<MethodDescriptor>singleton(createErrorFunction(this));
		}

		@NotNull
		@Override
		public DeclarationDescriptor getContainingDeclaration()
		{
			return ERROR_MODULE;
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

		@Override
		public String toString()
		{
			return debugMessage;
		}
	}

	private static final LightClassDescriptorImpl ERROR_CLASS = new LightClassDescriptorImpl(ERROR_MODULE, Collections.<AnnotationDescriptor>emptyList(), Modality.OPEN, Name.special("<ERROR CLASS>"), false)
	{
		@NotNull
		@Override
		public Set<ConstructorDescriptor> getConstructors()
		{
			return Collections.<ConstructorDescriptor>emptySet();
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
		ERROR_CLASS.initialize(true, Collections.<TypeParameterDescriptor>emptyList(), Collections.<NapileType>emptyList(), createErrorScope("ERROR_CLASS"), Collections.<ConstructorDescriptor>emptySet());
	}

	public static NapileScope createErrorScope(String debugMessage)
	{
		return new ErrorScope(debugMessage);
	}

	private static final NapileType ERROR_PROPERTY_TYPE = createErrorType("<ERROR PROPERTY TYPE>");
	private static final VariableDescriptor ERROR_PROPERTY = new VariableDescriptorImpl(ERROR_CLASS, Collections.<AnnotationDescriptor>emptyList(), Modality.OPEN, Visibility.PUBLIC, ReceiverDescriptor.NO_RECEIVER, Name.special("<ERROR PROPERTY>"), ERROR_PROPERTY_TYPE, CallableMemberDescriptor.Kind.DECLARATION, false, false, false);
	private static final Set<VariableDescriptor> ERROR_PROPERTY_GROUP = Collections.singleton(ERROR_PROPERTY);

	private static SimpleMethodDescriptor createErrorFunction(ErrorScope ownerScope)
	{
		ErrorSimpleMethodDescriptorImpl function = new ErrorSimpleMethodDescriptorImpl(ownerScope);
		function.initialize(ReceiverDescriptor.NO_RECEIVER, Collections.<TypeParameterDescriptorImpl>emptyList(), // TODO
				Collections.<CallParameterDescriptor>emptyList(), // TODO
				createErrorType("<ERROR METHOD RETURN TYPE>"), Modality.OPEN, Visibility.PUBLIC
                /*isInline = */);
		return function;
	}

	private static final NapileType ERROR_PARAMETER_TYPE = createErrorType("<ERROR VALUE_PARAMETER TYPE>");

	@NotNull
	public static NapileType createErrorType(String debugMessage)
	{
		return createErrorType(debugMessage, createErrorScope(debugMessage));
	}

	private static NapileType createErrorType(String debugMessage, NapileScope memberScope)
	{
		return createErrorTypeWithCustomDebugName(memberScope, "[ERROR : " + debugMessage + "]");
	}

	@NotNull
	public static NapileType createErrorTypeWithCustomDebugName(String debugName)
	{
		return createErrorTypeWithCustomDebugName(createErrorScope(debugName), debugName);
	}

	private static NapileType createErrorTypeWithCustomDebugName(NapileScope memberScope, String debugName)
	{
		return new ErrorTypeImpl(new TypeConstructorImpl(ERROR_CLASS, Collections.<AnnotationDescriptor>emptyList(), false, debugName, Collections.<TypeParameterDescriptorImpl>emptyList(), Collections.<NapileType>emptyList()), memberScope);
	}

	public static NapileType createWrongVarianceErrorType(NapileType value)
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

	public static boolean isErrorType(@NotNull NapileType type)
	{
		return type != TypeUtils.NO_EXPECTED_TYPE && !(type instanceof NamespaceType) &&
				((type instanceof DeferredType && (((DeferredType) type).getActualType() == null || isErrorType(((DeferredType) type).getActualType()))) ||
						type instanceof ErrorTypeImpl ||
						isError(type.getConstructor()));
	}

	public static boolean containsErrorType(@Nullable NapileType type)
	{
		if(type == null)
			return false;
		if(type instanceof NamespaceType)
			return false;
		if(isErrorType(type))
			return true;
		for(NapileType projection : type.getArguments())
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

	private static class ErrorTypeImpl implements NapileType
	{

		private final TypeConstructor constructor;

		private final NapileScope memberScope;

		private ErrorTypeImpl(TypeConstructor constructor, NapileScope memberScope)
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
		public List<NapileType> getArguments()
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
		public NapileScope getMemberScope()
		{
			return memberScope;
		}

		@Override
		public <A, R> R accept(@NotNull TypeConstructorVisitor<A, R> visitor, A arg)
		{
			return constructor.accept(this, visitor, arg);
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
