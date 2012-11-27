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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.lexer.NapileNodes;
import com.intellij.lang.ASTNode;

/**
 * @author max
 */
public class NapileUserTypeImpl extends NapileElementImpl implements NapileUserType
{
	public NapileUserTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitUserType(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitUserType(this, data);
	}

	@Override
	public NapileTypeArgumentList getTypeArgumentList()
	{
		return (NapileTypeArgumentList) findChildByType(NapileNodes.TYPE_ARGUMENT_LIST);
	}

	@NotNull
	@Override
	public List<NapileTypeReference> getTypeArguments()
	{
		NapileTypeArgumentList typeArgumentList = getTypeArgumentList();
		return typeArgumentList == null ? Collections.<NapileTypeReference>emptyList() : typeArgumentList.getArguments();
	}

	@Override
	@Nullable
	@IfNotParsed
	public NapileSimpleNameExpression getReferenceExpression()
	{
		return (NapileSimpleNameExpression) findChildByType(NapileNodes.REFERENCE_EXPRESSION);
	}

	@Override
	@Nullable
	public NapileUserType getQualifier()
	{
		return (NapileUserType) findChildByType(NapileNodes.USER_TYPE);
	}

	@Override
	@Nullable
	public String getReferencedName()
	{
		NapileSimpleNameExpression referenceExpression = getReferenceExpression();
		return referenceExpression == null ? null : referenceExpression.getReferencedName();
	}
}
