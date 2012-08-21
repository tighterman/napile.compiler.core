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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.NapileNodeTypes;
import com.intellij.lang.ASTNode;

/**
 * @author max
 */
public class NapileAnnotatedExpression extends NapileExpressionImpl
{
	public NapileAnnotatedExpression(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitAnnotatedExpression(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitAnnotatedExpression(this, data);
	}

	@Nullable
	public NapileExpression getBaseExpression()
	{
		return findChildByClass(NapileExpression.class);
	}

	public List<NapileAnnotation> getAttributeAnnotations()
	{
		return findChildrenByType(NapileNodeTypes.ANNOTATION);
	}

	public List<NapileAnnotationEntry> getAttributes()
	{
		List<NapileAnnotationEntry> answer = null;
		for(NapileAnnotation annotation : getAttributeAnnotations())
		{
			if(answer == null)
				answer = new ArrayList<NapileAnnotationEntry>();
			answer.addAll(annotation.getEntries());
		}
		return answer != null ? answer : Collections.<NapileAnnotationEntry>emptyList();
	}
}