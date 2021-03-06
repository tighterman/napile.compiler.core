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
import org.napile.compiler.lang.resolve.scopes.NapileScope;
import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.napile.compiler.lang.types.NapileType;
import org.napile.compiler.lang.types.TypeSubstitutor;

/**
 * @author abreslav
 */
public interface ClassDescriptor extends ClassifierDescriptor, DeclarationDescriptorNonRoot, DeclarationDescriptorWithVisibility
{

	@NotNull
	NapileScope getMemberScope(List<NapileType> typeArguments);


	@Override
	@NotNull
	DeclarationDescriptor getContainingDeclaration();

	/**
	 * @return type A&lt;T&gt; for the class A&lt;T&gt;
	 */
	@NotNull
	@Override
	NapileType getDefaultType();

	@NotNull
	@Override
	ClassDescriptor substitute(TypeSubstitutor substitutor);

	@NotNull
	ClassKind getKind();

	@Override
	@NotNull
	Modality getModality();

	@Override
	@NotNull
	Visibility getVisibility();

	@NotNull
	ReceiverDescriptor getImplicitReceiver();

	boolean isTraited();
}
