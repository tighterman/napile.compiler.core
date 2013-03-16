/*
 * Copyright 2010-2012 napile.org
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
import org.napile.compiler.lang.psi.NapileTypeReference;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 15:22/01.10.12
 */
public class NapileClassOfExpression extends NapileExpressionImpl
{
	public NapileClassOfExpression(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public NapileTypeReference getTypeReference()
	{
		return findChildByClass(NapileTypeReference.class);
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitClassOfExpression(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitClassOfExpression(this, data);
	}
}
