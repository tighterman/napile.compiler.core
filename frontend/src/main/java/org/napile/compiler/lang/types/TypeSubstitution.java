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

import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.TypeParameterDescriptor;

/**
 * @author abreslav
 */
public interface TypeSubstitution
{
	TypeSubstitution EMPTY = new TypeSubstitution()
	{
		@Override
		public NapileType get(TypeConstructor key)
		{
			return null;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public String toString()
		{
			return "Empty TypeSubstitution";
		}
	};

	TypeSubstitution DEFAULT_TYPE_FOR_TYPE_PARAMETERS = new TypeSubstitution()
	{
		@Override
		public NapileType get(TypeConstructor key)
		{
			DeclarationDescriptor declarationDescriptor = key.getDeclarationDescriptor();
			if(declarationDescriptor instanceof TypeParameterDescriptor)
				return ((TypeParameterDescriptor) declarationDescriptor).getUpperBoundsAsType();
			return null;
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public String toString()
		{
			return "Default Type For Type Parameter";
		}
	};

	@Nullable
	NapileType get(TypeConstructor key);

	boolean isEmpty();
}
