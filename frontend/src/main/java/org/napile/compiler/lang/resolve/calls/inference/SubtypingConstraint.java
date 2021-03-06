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

package org.napile.compiler.lang.resolve.calls.inference;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.types.NapileType;

/**
 * @author abreslav
 */
public class SubtypingConstraint
{
	private final ConstraintType type;
	private final NapileType subtype;
	private final NapileType supertype;

	public SubtypingConstraint(@NotNull ConstraintType type, @NotNull NapileType subtype, @NotNull NapileType supertype)
	{
		this.type = type;
		this.subtype = subtype;
		this.supertype = supertype;
	}

	@NotNull
	public NapileType getSubtype()
	{
		return subtype;
	}

	@NotNull
	public NapileType getSupertype()
	{
		return supertype;
	}

	@NotNull
	public ConstraintType getType()
	{
		return type;
	}

	@NotNull
	public String getErrorMessage()
	{
		return type.makeErrorMessage(this);
	}

	@Override
	public String toString()
	{
		return getSubtype() + " :< " + getSupertype();
	}
}
