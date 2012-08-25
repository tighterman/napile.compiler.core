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

package org.napile.compiler.lang.resolve.constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.annotations.AnnotationArgumentVisitor;
import org.napile.compiler.lang.resolve.scopes.JetScope;
import org.napile.compiler.lang.types.JetType;
import org.napile.compiler.lang.types.TypeUtils;
import org.napile.compiler.lang.rt.NapileLangPackage;
import com.google.common.base.Function;

/**
 * @author abreslav
 */
public class ShortValue implements CompileTimeConstant<Short>
{
	public static final Function<Long, ShortValue> CREATE = new Function<Long, ShortValue>()
	{
		@Override
		public ShortValue apply(@Nullable Long input)
		{
			assert input != null;
			return new ShortValue(input.shortValue());
		}
	};

	private final short value;

	public ShortValue(short value)
	{
		this.value = value;
	}

	@Override
	public Short getValue()
	{
		return value;
	}

	@Nullable
	@Override
	public JetType getType(@NotNull JetScope jetScope)
	{
		return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.SHORT, false);
	}

	@Override
	public <R, D> R accept(AnnotationArgumentVisitor<R, D> visitor, D data)
	{
		return visitor.visitShortValue(this, data);
	}

	@Override
	public String toString()
	{
		return value + ".toShort()";
	}
}
