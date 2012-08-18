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

package org.jetbrains.jet.lang.descriptors;

import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.name.FqNameUnsafe;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author svtk
 */
public class Visibilities
{
	public static final Visibility LOCAL = new Visibility("local", false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			DeclarationDescriptor parent = what;
			while(parent != null)
			{
				parent = parent.getContainingDeclaration();
				if((parent instanceof ClassDescriptor && !DescriptorUtils.isClassObject(parent)) || parent instanceof NamespaceDescriptor)
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
	};

	public static final Visibility COVERED = new Visibility("covered", true)
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

			FqNameUnsafe p1 = DescriptorUtils.getFQName(classDescriptor).parent();
			FqNameUnsafe p2 = DescriptorUtils.getFQName(fromClass).parent();
			if(p1.isRoot() && p2.isRoot())
				return true;

			return p2.getFqName().startsWith(p1.getFqName());
		}
	};

	public static final Visibility HERITABLE = new Visibility("heritable", true)
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
	};

	public static final Visibility PUBLIC = new Visibility("public", true)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			return true;
		}
	};

	@Deprecated
	public static final Visibility LOCAL2 = new Visibility("local", false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			throw new IllegalStateException(); //This method shouldn't be invoked for LOCAL visibility
		}
	};

	@Deprecated
	public static final Visibility INHERITED = new Visibility("inherited", false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			throw new IllegalStateException("Visibility is unknown yet"); //This method shouldn't be invoked for INHERITED visibility
		}
	};

	/* Visibility for fake override invisible members (they are created for better error reporting) */
	public static final Visibility INVISIBLE_FAKE = new Visibility("invisible_fake", false)
	{
		@Override
		protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from)
		{
			return false;
		}
	};

	public static final Set<Visibility> INTERNAL_VISIBILITIES = Sets.newHashSet(LOCAL, LOCAL2);

	private Visibilities()
	{
	}

	public static boolean isVisible(DeclarationDescriptorWithVisibility what, DeclarationDescriptor from)
	{
		DeclarationDescriptorWithVisibility parent = what;
		while(parent != null)
		{
			if(parent.getVisibility() == LOCAL2)
			{
				return true;
			}
			if(!parent.getVisibility().isVisible(parent, from))
			{
				return false;
			}
			parent = DescriptorUtils.getParentOfType(parent, DeclarationDescriptorWithVisibility.class);
		}
		return true;
	}

	private static final Map<Visibility, Integer> ORDERED_VISIBILITIES = Maps.newHashMap();

	static
	{
		ORDERED_VISIBILITIES.put(LOCAL, 0);
		ORDERED_VISIBILITIES.put(COVERED, 1);
		ORDERED_VISIBILITIES.put(HERITABLE, 1);
		ORDERED_VISIBILITIES.put(PUBLIC, 2);
	}

	/*package*/
	static Integer compareLocal(@NotNull Visibility first, @NotNull Visibility second)
	{
		if(first == second)
			return 0;
		Integer firstIndex = ORDERED_VISIBILITIES.get(first);
		Integer secondIndex = ORDERED_VISIBILITIES.get(second);
		if(firstIndex == null || secondIndex == null || firstIndex.equals(secondIndex))
		{
			return null;
		}
		return firstIndex - secondIndex;
	}

	@Nullable
	public static Integer compare(@NotNull Visibility first, @NotNull Visibility second)
	{
		Integer result = first.compareTo(second);
		if(result != null)
		{
			return result;
		}
		Integer oppositeResult = second.compareTo(first);
		if(oppositeResult != null)
		{
			return -oppositeResult;
		}
		return null;
	}
}
