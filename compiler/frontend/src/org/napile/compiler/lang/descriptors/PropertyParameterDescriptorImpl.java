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

package org.napile.compiler.lang.descriptors;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
import org.napile.compiler.lang.resolve.name.Name;
import org.napile.compiler.lang.types.JetType;
import org.napile.compiler.lang.types.TypeSubstitutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author abreslav
 */
public class PropertyParameterDescriptorImpl extends VariableDescriptorImpl implements MutableParameterDescriptor
{
	private Boolean hasDefaultValue;
	private final boolean declaresDefaultValue;

	private final JetType varargElementType;
	private final PropertyKind propertyKind;
	private final int index;
	private final ParameterDescriptor original;

	private final Set<ParameterDescriptor> overriddenDescriptors = Sets.newLinkedHashSet(); // Linked is essential
	private boolean overriddenDescriptorsLocked = false;
	private final Set<? extends ParameterDescriptor> readOnlyOverriddenDescriptors = Collections.unmodifiableSet(overriddenDescriptors);

	public PropertyParameterDescriptorImpl(@NotNull DeclarationDescriptor containingDeclaration, int index, @NotNull List<AnnotationDescriptor> annotations, @NotNull Name name, PropertyKind propertyKind, @NotNull JetType outType, boolean declaresDefaultValue, @Nullable JetType varargElementType)
	{
		super(containingDeclaration, annotations, name, outType, false);
		this.original = this;
		this.index = index;
		this.declaresDefaultValue = declaresDefaultValue;
		this.varargElementType = varargElementType;
		this.propertyKind = propertyKind;
	}

	public PropertyParameterDescriptorImpl(@NotNull DeclarationDescriptor containingDeclaration, @NotNull ParameterDescriptor original, @NotNull List<AnnotationDescriptor> annotations, PropertyKind propertyKind, @NotNull JetType outType, @Nullable JetType varargElementType)
	{
		super(containingDeclaration, annotations, original.getName(), outType, false);
		this.original = original;
		this.index = original.getIndex();
		this.declaresDefaultValue = original.declaresDefaultValue();
		this.varargElementType = varargElementType;
		this.propertyKind = propertyKind;
	}

	@Override
	public void setType(@NotNull JetType type)
	{
		setOutType(type);
	}

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public boolean hasDefaultValue()
	{
		computeDefaultValuePresence();
		return hasDefaultValue;
	}

	@Override
	public boolean declaresDefaultValue()
	{
		return declaresDefaultValue && ((CallableMemberDescriptor) getContainingDeclaration()).getKind().isReal();
	}

	private void computeDefaultValuePresence()
	{
		if(hasDefaultValue != null)
			return;
		overriddenDescriptorsLocked = true;
		if(declaresDefaultValue)
		{
			hasDefaultValue = true;
		}
		else
		{
			for(ParameterDescriptor descriptor : overriddenDescriptors)
			{
				if(descriptor.hasDefaultValue())
				{
					hasDefaultValue = true;
					return;
				}
			}
			hasDefaultValue = false;
		}
	}

	@Nullable
	public JetType getVarargElementType()
	{
		return varargElementType;
	}

	@NotNull
	@Override
	public ParameterDescriptor getOriginal()
	{
		return original == this ? this : original.getOriginal();
	}

	@NotNull
	@Override
	public ParameterDescriptor substitute(TypeSubstitutor substitutor)
	{
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public <R, D> R accept(DeclarationDescriptorVisitor<R, D> visitor, D data)
	{
		return visitor.visitPropertyParameterDescriptor(this, data);
	}

	@NotNull
	@Override
	public PropertyKind getPropertyKind()
	{
		return propertyKind;
	}

	@NotNull
	@Override
	public ParameterDescriptor copy(@NotNull DeclarationDescriptor newOwner)
	{
		return new PropertyParameterDescriptorImpl(newOwner, index, Lists.newArrayList(getAnnotations()), getName(), propertyKind, getType(), hasDefaultValue, varargElementType);
	}

	@NotNull
	@Override
	public Visibility getVisibility()
	{
		return Visibility.LOCAL2;
	}

	@NotNull
	@Override
	public Set<? extends ParameterDescriptor> getOverriddenDescriptors()
	{
		return readOnlyOverriddenDescriptors;
	}

	@Override
	public void addOverriddenDescriptor(@NotNull ParameterDescriptor overridden)
	{
		assert !overriddenDescriptorsLocked : "Adding more overridden descriptors is not allowed at this point: " + "the presence of the default value has already been calculated";
		overriddenDescriptors.add(overridden);
	}
}