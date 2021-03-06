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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.CallableMemberDescriptor;
import org.napile.compiler.lang.descriptors.ClassDescriptor;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.PackageDescriptor;
import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
import org.napile.compiler.lang.descriptors.VariableAccessorDescriptor;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.psi.*;
import org.napile.compiler.util.slicedmap.ReadOnlySlice;
import org.napile.compiler.util.slicedmap.Slices;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;

/**
 * @author abreslav
 * @author svtk
 */
public class BindingTraceUtil
{
	private BindingTraceUtil()
	{
	}

	@NotNull
	public static BindingTrace getLastParent(@NotNull final BindingTrace reader)
	{
		BindingTrace last = reader;
		while(true)
		{
			BindingTrace parentReader = last.getParent();

			if(parentReader == null)
			{
				return last;
			}

			last = parentReader;
		}
	}

	private static final Slices.KeyNormalizer<DeclarationDescriptor> DECLARATION_DESCRIPTOR_NORMALIZER = new Slices.KeyNormalizer<DeclarationDescriptor>()
	{
		@Override
		public DeclarationDescriptor normalize(DeclarationDescriptor declarationDescriptor)
		{
			if(declarationDescriptor instanceof CallableMemberDescriptor)
			{
				CallableMemberDescriptor callable = (CallableMemberDescriptor) declarationDescriptor;
				if(callable.getKind() != CallableMemberDescriptor.Kind.DECLARATION)
				{
					throw new IllegalStateException("non-declaration descriptors should be filtered out earlier: " + callable);
				}
			}
			//if (declarationDescriptor instanceof VariableAsFunctionDescriptor) {
			//    VariableAsFunctionDescriptor descriptor = (VariableAsFunctionDescriptor) declarationDescriptor;
			//    if (descriptor.getOriginal() != descriptor) {
			//        throw new IllegalStateException("original should be resolved earlier: " + descriptor);
			//    }
			//}
			return declarationDescriptor.getOriginal();
		}
	};

	public static final ReadOnlySlice<DeclarationDescriptor, PsiElement> DESCRIPTOR_TO_DECLARATION = Slices.<DeclarationDescriptor, PsiElement>sliceBuilder().setKeyNormalizer(DECLARATION_DESCRIPTOR_NORMALIZER).setDebugName("DESCRIPTOR_TO_DECLARATION").build();

	@Nullable
	public static PsiElement resolveToDeclarationPsiElement(@NotNull BindingTrace bindingTrace, @Nullable NapileReferenceExpression referenceExpression)
	{
		DeclarationDescriptor declarationDescriptor = bindingTrace.get(BindingTraceKeys.REFERENCE_TARGET, referenceExpression);
		if(declarationDescriptor == null)
		{
			return bindingTrace.get(BindingTraceKeys.LABEL_TARGET, referenceExpression);
		}

		PsiElement element = descriptorToDeclaration(bindingTrace, declarationDescriptor);
		if(element != null)
		{
			return element;
		}

		return null;
	}

	@NotNull
	public static List<PsiElement> resolveToDeclarationPsiElements(@NotNull BindingTrace bindingTrace, @Nullable NapileReferenceExpression referenceExpression)
	{
		DeclarationDescriptor declarationDescriptor = bindingTrace.get(BindingTraceKeys.REFERENCE_TARGET, referenceExpression);
		if(declarationDescriptor == null)
		{
			return Lists.newArrayList(bindingTrace.get(BindingTraceKeys.LABEL_TARGET, referenceExpression));
		}

		List<PsiElement> elements = descriptorToDeclarations(bindingTrace, declarationDescriptor);
		if(elements.size() > 0)
		{
			return elements;
		}

		return Lists.newArrayList();
	}


	@Nullable
	public static VariableDescriptor extractVariableDescriptorIfAny(@NotNull BindingTrace bindingTrace, @Nullable NapileElement element, boolean onlyReference)
	{
		DeclarationDescriptor descriptor = null;
		if(!onlyReference && (element instanceof NapileVariable || element instanceof NapileCallParameterAsVariable))
		{
			descriptor = bindingTrace.get(BindingTraceKeys.DECLARATION_TO_DESCRIPTOR, element);
		}
		else if(element instanceof NapileSimpleNameExpression)
		{
			descriptor = bindingTrace.get(BindingTraceKeys.REFERENCE_TARGET, (NapileSimpleNameExpression) element);
		}
		else if(element instanceof NapileQualifiedExpressionImpl)
		{
			descriptor = extractVariableDescriptorIfAny(bindingTrace, ((NapileQualifiedExpressionImpl) element).getSelectorExpression(), onlyReference);
		}
		if(descriptor instanceof VariableDescriptor)
			return (VariableDescriptor) descriptor;
		else if(descriptor instanceof VariableAccessorDescriptor)
			return ((VariableAccessorDescriptor) descriptor).getVariable();
		return null;
	}

	// TODO these helper methods are added as a workaround to some compiler bugs in Kotlin...

	// NOTE this is used by KDoc
	@Nullable
	public static PackageDescriptor namespaceDescriptor(@NotNull BindingTrace context, @NotNull NapileFile source)
	{
		return context.get(BindingTraceKeys.FILE_TO_NAMESPACE, source);
	}

	@Nullable
	private static PsiElement doGetDescriptorToDeclaration(@NotNull BindingTrace context, @NotNull DeclarationDescriptor descriptor)
	{
		return context.get(DESCRIPTOR_TO_DECLARATION, descriptor);
	}

	// NOTE this is also used by KDoc
	@Nullable
	public static PsiElement descriptorToDeclaration(@NotNull BindingTrace context, @NotNull DeclarationDescriptor descriptor)
	{
		if(descriptor instanceof CallableMemberDescriptor)
		{
			return callableDescriptorToDeclaration(context, (CallableMemberDescriptor) descriptor);
		}
		else if(descriptor instanceof ClassDescriptor)
		{
			return classDescriptorToDeclaration(context, (ClassDescriptor) descriptor);
		}
		else
		{
			return doGetDescriptorToDeclaration(context, descriptor);
		}
	}

	@NotNull
	public static List<PsiElement> descriptorToDeclarations(@NotNull BindingTrace context, @NotNull DeclarationDescriptor descriptor)
	{
		if(descriptor instanceof CallableMemberDescriptor)
		{
			return callableDescriptorToDeclarations(context, (CallableMemberDescriptor) descriptor);
		}
		else
		{
			PsiElement psiElement = descriptorToDeclaration(context, descriptor);
			if(psiElement != null)
			{
				return Lists.newArrayList(psiElement);
			}
			else
			{
				return Lists.newArrayList();
			}
		}
	}

	@Nullable
	public static PsiElement callableDescriptorToDeclaration(@NotNull BindingTrace context, @NotNull CallableMemberDescriptor callable)
	{
		if(callable.getKind() == CallableMemberDescriptor.Kind.CREATED_BY_PLUGIN)
		{
			return context.get(BindingTraceKeys.CREATED_BY_PLUGIN, callable);
		}
		if(callable.getKind() != CallableMemberDescriptor.Kind.DECLARATION)
		{
			Set<? extends CallableMemberDescriptor> overriddenDescriptors = callable.getOverriddenDescriptors();
			if(overriddenDescriptors.size() != 1)
			{
				// TODO evil code
				//throw new IllegalStateException("cannot find declaration: fake descriptor" +
				//		" has more then one overridden descriptor: " + callable);
				return null;
			}

			return callableDescriptorToDeclaration(context, overriddenDescriptors.iterator().next());
		}

		return doGetDescriptorToDeclaration(context, callable.getOriginal());
	}

	private static List<PsiElement> callableDescriptorToDeclarations(@NotNull BindingTrace bindingTrace, @NotNull CallableMemberDescriptor callable)
	{
		if(callable.getKind() != CallableMemberDescriptor.Kind.DECLARATION)
		{
			List<PsiElement> r = new ArrayList<PsiElement>();
			Set<? extends CallableMemberDescriptor> overriddenDescriptors = callable.getOverriddenDescriptors();
			for(CallableMemberDescriptor overridden : overriddenDescriptors)
			{
				r.addAll(callableDescriptorToDeclarations(bindingTrace, overridden));
			}
			return r;
		}
		PsiElement psiElement = doGetDescriptorToDeclaration(bindingTrace, callable);
		return psiElement != null ? Lists.newArrayList(psiElement) : Lists.<PsiElement>newArrayList();
	}

	@Nullable
	public static PsiElement classDescriptorToDeclaration(@NotNull BindingTrace context, @NotNull ClassDescriptor clazz)
	{
		return doGetDescriptorToDeclaration(context, clazz);
	}

	public static void recordMethodDeclarationToDescriptor(@NotNull BindingTrace trace, @NotNull PsiElement psiElement, @NotNull SimpleMethodDescriptor method)
	{
		if(method.getKind() != CallableMemberDescriptor.Kind.DECLARATION)
			throw new IllegalArgumentException("function of kind " + method.getKind() + " cannot have declaration");

		trace.record(BindingTraceKeys.METHOD, psiElement, method);
	}
}
