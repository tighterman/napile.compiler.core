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
import org.napile.compiler.lang.NapileLanguage;
import org.napile.compiler.lang.psi.NapileElement;
import org.napile.compiler.lang.psi.NapileFile;
import org.napile.compiler.lang.psi.NapileTreeVisitor;
import org.napile.compiler.lang.psi.NapileVisitor;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

/**
 * @author Nikolay Krasko
 */
public class NapileElementImplStub<T extends StubElement> extends StubBasedPsiElementBase<T> implements NapileElement, StubBasedPsiElement<T>
{
	public NapileElementImplStub(@NotNull T stub, @NotNull IStubElementType nodeType)
	{
		super(stub, nodeType);
	}

	public NapileElementImplStub(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@Override
	public Language getLanguage()
	{
		return NapileLanguage.INSTANCE;
	}

	@Override
	public String toString()
	{
		return getNode().getElementType().toString();
	}

	@Override
	public final void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof NapileVisitorVoid)
		{
			accept((NapileVisitorVoid) visitor);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	@Override
	public <D> void acceptChildren(@NotNull NapileTreeVisitor<D> visitor, D data)
	{
		PsiElement child = getFirstChild();
		while(child != null)
		{
			if(child instanceof NapileElement)
			{
				((NapileElement) child).accept(visitor, data);
			}
			child = child.getNextSibling();
		}
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitJetElement(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitJetElement(this, data);
	}

	@Override
	public NapileFile getContainingFile() throws PsiInvalidElementAccessException
	{
		return (NapileFile) super.getContainingFile();
	}
}
