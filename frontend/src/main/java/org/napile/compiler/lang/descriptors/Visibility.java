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
import org.jetbrains.annotations.Nullable;
import org.napile.asm.resolve.name.FqNameUnsafe;
import org.napile.asm.resolve.name.Name;
import org.napile.compiler.lang.lexer.NapileKeywordToken;
import org.napile.compiler.lang.lexer.NapileTokens;
import org.napile.compiler.lang.psi.NapileModifierListOwner;
import org.napile.compiler.lang.resolve.DescriptorUtils;

/**
 * @author svtk
 */
public enum  Visibility
{
	LOCAL(NapileTokens.LOCAL_KEYWORD, false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			DeclarationDescriptor parent = what;
			while(parent != null)
			{
				parent = parent.getContainingDeclaration();
				if((parent instanceof ClassDescriptor) || parent instanceof PackageDescriptor)
				{
					break;
				}
			}
			DeclarationDescriptor fromParent = from;
			while(fromParent != null)
			{
				if(parent == fromParent)
				{
					return true;
				}
				fromParent = fromParent.getContainingDeclaration();
			}
			return false;
		}
	},

	COVERED(NapileTokens.COVERED_KEYWORD, true)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			PackageDescriptor whatPackage = DescriptorUtils.getParentOfType(what, PackageDescriptor.class, false);
			if(whatPackage == null)
				return false;

			PackageDescriptor fromPackage = DescriptorUtils.getParentOfType(from, PackageDescriptor.class, false);
			if(fromPackage == null)
				return false;

			FqNameUnsafe p1 = DescriptorUtils.getFQName(whatPackage);
			FqNameUnsafe p2 = DescriptorUtils.getFQName(fromPackage);
			if(p1.isRoot() && p2.isRoot())
				return true;

			List<Name> paths1 = p1.pathSegments();
			List<Name> paths2 = p2.pathSegments();

			if(paths2.size() < paths1.size())
				return false;
			for(int i = 0; i < paths1.size(); i++)
			{
				Name name1 = paths1.get(i);
				Name name2 = paths2.get(i);
				if(!name1.equals(name2))
					return false;
			}
			return true;
		}
	},

	HERITABLE(NapileTokens.HERITABLE_KEYWORD, true)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			ClassDescriptor classDescriptor = DescriptorUtils.getParentOfType(what, ClassDescriptor.class);
			if(classDescriptor == null)
				return false;

			ClassDescriptor fromClass = DescriptorUtils.getParentOfType(from, ClassDescriptor.class, false);
			if(fromClass == null)
				return false;
			if(DescriptorUtils.isSubclass(fromClass, classDescriptor))
			{
				return true;
			}
			return isVisible(what, fromClass.getContainingDeclaration());
		}
	},

	PUBLIC(null, true)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			return true;
		}
	},

	@Deprecated
	LOCAL2(null, false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			throw new IllegalStateException(); //This method shouldn't be invoked for LOCAL visibility
		}
	},

	/* Visibility for fake override invisible members (they are created for better error reporting) */
	INVISIBLE_FAKE(null, false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			return false;
		}
	};

	private final boolean isPublicAPI;
	private final NapileKeywordToken keyword;

	Visibility(@Nullable NapileKeywordToken keyword, boolean isPublicAPI)
	{
		this.isPublicAPI = isPublicAPI;
		this.keyword = keyword;
	}

	@NotNull
	public static Visibility resolve(@NotNull NapileModifierListOwner modifierListOwner)
	{
		if(modifierListOwner.hasModifier(NapileTokens.LOCAL_KEYWORD))
			return LOCAL;
		if(modifierListOwner.hasModifier(NapileTokens.COVERED_KEYWORD))
			return COVERED;
		if(modifierListOwner.hasModifier(NapileTokens.HERITABLE_KEYWORD))
			return HERITABLE;
		return PUBLIC;
	}

	public boolean isPublicAPI()
	{
		return isPublicAPI;
	}

	@NotNull
	public NapileKeywordToken getKeyword()
	{
		if(keyword == null)
			throw new IllegalArgumentException("Keyword is null. Don't use this visibility for rendering or etc. This: " + name());
		return keyword;
	}

	@Override
	public String toString()
	{
		return keyword == null ? "" : keyword.toString();
	}

	protected abstract boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from);
}
