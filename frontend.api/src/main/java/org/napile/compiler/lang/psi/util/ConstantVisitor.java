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

import org.napile.compiler.lang.psi.NapileConstantExpression;
import org.napile.compiler.lang.psi.NapileDelegationToSuperCall;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileVisitor;
import org.napile.compiler.lang.resolve.BindingTraceKeys;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.resolve.constants.CompileTimeConstant;
import org.napile.compiler.lang.resolve.scopes.NapileScope;
import org.napile.compiler.lang.types.TypeUtils;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 12:27/27.02.13
 */
public class ConstantVisitor extends NapileVisitor<Constant, Object>
{
	private final BindingTrace bindingTrace;

	public ConstantVisitor(BindingTrace bindingTrace)
	{
		this.bindingTrace = bindingTrace;
	}

	@Override
	public Constant visitConstantExpression(NapileConstantExpression expression, Object o)
	{
		NapileScope napileScope = bindingTrace.get(BindingTraceKeys.RESOLUTION_SCOPE, expression);
		if(napileScope == null)
		{
			NapileDelegationToSuperCall delegationToSuperCall = PsiTreeUtil.getParentOfType(expression, NapileDelegationToSuperCall.class);
			if(delegationToSuperCall != null)
			{
				napileScope = bindingTrace.get(BindingTraceKeys.TYPE_RESOLUTION_SCOPE, delegationToSuperCall.getTypeReference());
			}

			if(napileScope == null)
			{
				return Constant.ANY;
			}
		}

		final CompileTimeConstant<?> compileTimeConstant = bindingTrace.get(BindingTraceKeys.COMPILE_TIME_VALUE, expression);
		if(compileTimeConstant != null)
			return new Constant(TypeUtils.getFqName(compileTimeConstant.getType(napileScope)), compileTimeConstant.getValue());
		else
			return Constant.ANY;
	}

	@Override
	public Constant visitExpression(NapileExpression expression, Object data)
	{
		return Constant.ANY; //TODO [VISTALL]
	}
}
