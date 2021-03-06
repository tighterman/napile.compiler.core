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

package org.napile.compiler.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.lexer.NapileTokens;
import org.napile.compiler.lang.lexer.NapileToken;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author max
 */
public abstract class NapileQualifiedExpressionImpl extends NapileExpressionImpl
{
	public NapileQualifiedExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public NapileExpression getReceiverExpression()
	{
		return findChildByClass(NapileExpression.class);
	}

	@Nullable
	@IfNotParsed
	public NapileExpression getSelectorExpression()
	{
		ASTNode node = getOperationTokenNode();
		while(node != null)
		{
			PsiElement psi = node.getPsi();
			if(psi instanceof NapileExpression)
			{
				return (NapileExpression) psi;
			}
			node = node.getTreeNext();
		}

		return null;
	}

	@NotNull
	public ASTNode getOperationTokenNode()
	{
		ASTNode operationNode = getNode().findChildByType(NapileTokens.OPERATIONS);
		assert operationNode != null;
		return operationNode;
	}

	@NotNull
	public NapileToken getOperationSign()
	{
		return (NapileToken) getOperationTokenNode().getElementType();
	}
}
