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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.napile.asm.resolve.name.Name;
import org.napile.compiler.lang.descriptors.annotations.AnnotatedImpl;
import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
import org.napile.compiler.render.DescriptorRenderer;

/**
 * @author Stepan Koltsov
 */
public abstract class DeclarationDescriptorImpl extends AnnotatedImpl implements Named, DeclarationDescriptor
{

	@NotNull
	private final Name name;

	public DeclarationDescriptorImpl(@NotNull List<AnnotationDescriptor> annotations, @NotNull Name name)
	{
		super(annotations);
		this.name = name;
	}

	@NotNull
	@Override
	public Name getName()
	{
		return name;
	}

	@NotNull
	@Override
	public DeclarationDescriptor getOriginal()
	{
		return this;
	}

	@Override
	public void acceptVoid(DeclarationDescriptorVisitor<Void, Void> visitor)
	{
		accept(visitor, null);
	}

	@Override
	public String toString()
	{
		try
		{
			return DescriptorRenderer.DEBUG_TEXT.render(this) +
					"[" +
					getClass().getSimpleName() +
					"@" +
					Integer.toHexString(System.identityHashCode(this)) +
					"]";
		}
		catch(Throwable e)
		{
			// DescriptionRenderer may throw if this is not yet completely initialized
			// It is very inconvenient while debugging
			return this.getClass().getName() + "@" + System.identityHashCode(this);
		}
	}
}
