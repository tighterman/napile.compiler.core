/*
 * Copyright 2010-2013 napile.org
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

package org.napile.compiler.lang.psi.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.asm.lib.NapileLangPackage;
import org.napile.asm.resolve.name.FqName;

/**
 * @author VISTALL
 * @since 12:27/27.02.13
 */
public class Constant
{
	public static final Constant[] EMPTY_ARRAY = new Constant[0];
	public static final Constant ANY = new Constant(NapileLangPackage.ANY, null);

	private final FqName fqName;
	private final Object value;

	public Constant(@NotNull FqName fqName, @Nullable Object value)
	{
		this.fqName = fqName;
		this.value = value;
	}

	@NotNull
	public FqName getFqName()
	{
		return fqName;
	}

	@Nullable
	public Object getValue()
	{
		return value;
	}
}
