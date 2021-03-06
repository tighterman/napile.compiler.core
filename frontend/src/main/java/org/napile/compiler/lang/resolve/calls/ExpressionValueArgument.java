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

package org.napile.compiler.lang.resolve.calls;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.ValueArgument;

/**
 * @author abreslav
 */
public class ExpressionValueArgument implements ResolvedValueArgument
{
	private final ValueArgument valueArgument;

	public ExpressionValueArgument(@Nullable ValueArgument valueArgument)
	{
		this.valueArgument = valueArgument;
	}

	// Nullable when something like f(a, , b) was in the source code
	@Nullable
	public ValueArgument getValueArgument()
	{
		return valueArgument;
	}

	@NotNull
	@Override
	public List<ValueArgument> getArguments()
	{
		if(valueArgument == null)
			return Collections.emptyList();
		return Collections.singletonList(valueArgument);
	}

	@Override
	public String toString()
	{
		NapileExpression expression = valueArgument.getArgumentExpression();
		return expression == null ? "no expression" : expression.getText();
	}
}
