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
import org.napile.compiler.NapileNodeTypes;
import org.napile.compiler.lang.psi.stubs.PsiJetFunctionStub;
import org.napile.compiler.lang.psi.stubs.elements.JetStubElementTypes;
import org.napile.compiler.lang.resolve.name.FqName;
import org.napile.compiler.lexer.JetTokens;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author max
 */
public class NapileNamedFunction extends NapileTypeParameterListOwnerStub<PsiJetFunctionStub> implements NapileMethod, NapileWithExpressionInitializer
{
	public NapileNamedFunction(@NotNull ASTNode node)
	{
		super(node);
	}

	public NapileNamedFunction(@NotNull PsiJetFunctionStub stub)
	{
		super(stub, JetStubElementTypes.METHOD);
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitNamedMethod(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitNamedFunction(this, data);
	}

	public boolean hasTypeParameterListBeforeFunctionName()
	{
		NapileTypeParameterList typeParameterList = getTypeParameterList();
		if(typeParameterList == null)
		{
			return false;
		}
		PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier == null)
		{
			return false;
		}
		return nameIdentifier.getTextOffset() > typeParameterList.getTextOffset();
	}

	@Override
	public boolean hasBlockBody()
	{
		return getEqualsToken() == null;
	}

	@Nullable
	public PsiElement getEqualsToken()
	{
		return findChildByType(JetTokens.EQ);
	}

	@Override
	@Nullable
	public NapileExpression getInitializer()
	{
		return PsiTreeUtil.getNextSiblingOfType(getEqualsToken(), NapileExpression.class);
	}

	/**
	 * Returns full qualified name for function "package_fqn.function_name"
	 * Not null for top level functions.
	 *
	 * @return
	 */
	@Nullable
	public FqName getQualifiedName()
	{
		final PsiJetFunctionStub stub = getStub();
		if(stub != null)
		{
			return stub.getTopFQName();
		}

		PsiElement parent = getParent();
		if(parent instanceof NapileFile)
		{
			// fqname is different in scripts
			if(((NapileFile) parent).getNamespaceHeader() == null)
			{
				return null;
			}
			NapileFile jetFile = (NapileFile) parent;
			final FqName fileFQN = NapilePsiUtil.getFQName(jetFile);
			return fileFQN.child(getNameAsName());
		}

		return null;
	}

	@NotNull
	@Override
	public IStubElementType getElementType()
	{
		return JetStubElementTypes.METHOD;
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	@Nullable
	public NapileParameterList getValueParameterList()
	{
		return (NapileParameterList) findChildByType(NapileNodeTypes.VALUE_PARAMETER_LIST);
	}

	@Override
	@NotNull
	public List<NapileElement> getValueParameters()
	{
		NapileParameterList list = getValueParameterList();
		return list != null ? list.getParameters() : Collections.<NapileElement>emptyList();
	}

	@Override
	@Nullable
	public NapileExpression getBodyExpression()
	{
		return findChildByClass(NapileExpression.class);
	}

	@Override
	public boolean hasDeclaredReturnType()
	{
		return getReturnTypeRef() != null;
	}

	@Override
	@Nullable
	public NapileTypeReference getReceiverTypeRef()
	{
		PsiElement child = getFirstChild();
		while(child != null)
		{
			IElementType tt = child.getNode().getElementType();
			if(tt == JetTokens.LPAR || tt == JetTokens.COLON)
				break;
			if(child instanceof NapileTypeReference)
			{
				return (NapileTypeReference) child;
			}
			child = child.getNextSibling();
		}

		return null;
	}

	@Override
	@Nullable
	public NapileTypeReference getReturnTypeRef()
	{
		boolean colonPassed = false;
		PsiElement child = getFirstChild();
		while(child != null)
		{
			IElementType tt = child.getNode().getElementType();
			if(tt == JetTokens.COLON)
			{
				colonPassed = true;
			}
			if(colonPassed && child instanceof NapileTypeReference)
			{
				return (NapileTypeReference) child;
			}
			child = child.getNextSibling();
		}

		return null;
	}

	@NotNull
	@Override
	public NapileElement asElement()
	{
		return this;
	}

	@Override
	public boolean isLocal()
	{
		PsiElement parent = getParent();
		return !(parent instanceof NapileFile || parent instanceof NapileClassBody || parent instanceof NapileNamespaceBody);
	}
}
