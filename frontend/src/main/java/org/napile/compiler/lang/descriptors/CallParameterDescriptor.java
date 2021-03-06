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

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.descriptors.annotations.Annotated;
import org.napile.compiler.lang.types.NapileType;

/**
 * @author abreslav
 */
public interface CallParameterDescriptor extends VariableDescriptor, Annotated
{
	/**
	 * Returns the 0-based index of the value parameter in the parameter list of its containing function.
	 *
	 * @return the parameter index
	 */
	int getIndex();

	/**
	 * The font-end relies on this property when resolving function calls
	 *
	 * @return {@code true} iff the parameter has a default value, i.e. declares it or inherits
	 *         by overriding a parameter in an overridden function.
	 */
	boolean hasDefaultValue();

	/**
	 * The back-end should relies on this property when generating function signatures
	 *
	 * @return {@code true} iff the parameter declares a default value, i.e. explicitly specifies it in the function header
	 */
	boolean declaresDefaultValue();

	boolean isRef();

	@Override
	@NotNull
	NapileType getType();

	@NotNull
	@Override
	CallParameterDescriptor getOriginal();

	@NotNull
	CallParameterDescriptor copy(DeclarationDescriptor newOwner);

	/**
	 * Parameter p1 overrides p2 iff
	 * a) their respective owners (function declarations) f1 override f2
	 * b) p1 and p2 have the same indices in the owners' parameter lists
	 */
	@NotNull
	@Override
	Set<? extends CallParameterDescriptor> getOverriddenDescriptors();

	void addOverriddenDescriptor(@NotNull CallParameterDescriptor overridden);
}
