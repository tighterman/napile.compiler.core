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

package org.napile.idea.plugin.quickfix;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.diagnostics.Diagnostic;
import org.napile.compiler.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileMethod;
import org.napile.compiler.lexer.NapileTokens;
import org.napile.idea.plugin.JetBundle;
import com.intellij.extapi.psi.ASTDelegatePsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author svtk
 */
public class RemoveFunctionBodyFix extends JetIntentionAction<NapileMethod>
{

	public RemoveFunctionBodyFix(@NotNull NapileMethod element)
	{
		super(element);
	}

	@NotNull
	@Override
	public String getText()
	{
		return JetBundle.message("remove.function.body");
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return JetBundle.message("remove.function.body");
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		return super.isAvailable(project, editor, file) && element.getBodyExpression() != null;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		NapileMethod function = (NapileMethod) element.copy();
		assert function instanceof ASTDelegatePsiElement;
		ASTDelegatePsiElement functionElementWithAst = (ASTDelegatePsiElement) function;
		NapileExpression bodyExpression = function.getBodyExpression();
		assert bodyExpression != null;
		if(function.hasBlockBody())
		{
			PsiElement prevElement = bodyExpression.getPrevSibling();
			QuickFixUtil.removePossiblyWhiteSpace(functionElementWithAst, prevElement);
			functionElementWithAst.deleteChildInternal(bodyExpression.getNode());
		}
		else
		{
			PsiElement prevElement = bodyExpression.getPrevSibling();
			PsiElement prevPrevElement = prevElement.getPrevSibling();
			QuickFixUtil.removePossiblyWhiteSpace(functionElementWithAst, prevElement);
			removePossiblyEquationSign(functionElementWithAst, prevElement);
			removePossiblyEquationSign(functionElementWithAst, prevPrevElement);
			functionElementWithAst.deleteChildInternal(bodyExpression.getNode());
		}
		element.replace(function);
	}

	private static boolean removePossiblyEquationSign(@NotNull ASTDelegatePsiElement element, @Nullable PsiElement possiblyEq)
	{
		if(possiblyEq instanceof LeafPsiElement && ((LeafPsiElement) possiblyEq).getElementType() == NapileTokens.EQ)
		{
			QuickFixUtil.removePossiblyWhiteSpace(element, possiblyEq.getNextSibling());
			element.deleteChildInternal(possiblyEq.getNode());
			return true;
		}
		return false;
	}

	public static JetIntentionActionFactory createFactory()
	{
		return new JetIntentionActionFactory()
		{
			@Override
			public JetIntentionAction<NapileMethod> createAction(Diagnostic diagnostic)
			{
				NapileMethod function = QuickFixUtil.getParentElementOfType(diagnostic, NapileMethod.class);
				if(function == null)
					return null;
				return new RemoveFunctionBodyFix(function);
			}
		};
	}
}
