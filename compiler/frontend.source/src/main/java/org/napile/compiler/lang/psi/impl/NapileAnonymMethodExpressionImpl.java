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

package org.napile.compiler.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.lexer.NapileNodes;
import org.napile.compiler.lang.psi.NapileAnonymMethod;
import org.napile.compiler.lang.psi.NapileAnonymMethodExpression;
import org.napile.compiler.lang.psi.NapileExpressionImpl;
import org.napile.compiler.lang.psi.NapileVisitor;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import com.intellij.lang.ASTNode;

/**
 * @author max
 */
public class NapileAnonymMethodExpressionImpl extends NapileExpressionImpl implements NapileAnonymMethodExpression
{
	public NapileAnonymMethodExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitAnonymMethodExpression(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitAnonymMethodExpression(this, data);
	}

	@Override
	@NotNull
	public NapileAnonymMethod getAnonymMethod()
	{
		return (NapileAnonymMethod) findNotNullChildByType(NapileNodes.ANONYM_METHOD);
	}
}