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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.napile.asm.resolve.name.FqName;
import org.napile.compiler.lang.lexer.NapileNodes;
import org.napile.compiler.lang.psi.*;
import org.napile.compiler.lang.psi.stubs.NapilePsiClassStub;
import org.napile.compiler.lang.psi.stubs.elements.NapileStubElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;

/**
 * @author max
 */
public class NapileClassImpl extends NapileTypeParameterListOwnerStub<NapilePsiClassStub> implements NapileClass
{
	public NapileClassImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public NapileClassImpl(@NotNull final NapilePsiClassStub stub)
	{
		super(stub, NapileStubElementTypes.CLASS);
	}

	@NotNull
	@Override
	public NapileDeclaration[] getDeclarations()
	{
		NapileClassBody body = (NapileClassBody) findChildByType(NapileNodes.CLASS_BODY);
		if(body == null)
			return NapileDeclaration.EMPTY_ARRAY;

		return body.getDeclarations();
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitClass(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitClass(this, data);
	}

	@Override
	@NotNull
	public NapileConstructor[] getConstructors()
	{
		NapileClassBody body = (NapileClassBody) findChildByType(NapileNodes.CLASS_BODY);
		if(body == null)
			return NapileConstructor.EMPTY_ARRAY;

		return body.getConstructors();
	}

	@NotNull
	@Override
	public List<? extends NapileTypeReference> getSuperTypes()
	{
		NapileTypeListImpl ex = (NapileTypeListImpl)findChildByType(NapileNodes.EXTEND_TYPE_LIST);
		return ex == null ? Collections.<NapileTypeReference>emptyList() : ex.getTypeList();
	}

	@Override
	public NapileElement getSuperTypesElement()
	{
		return (NapileElement)findChildByType(NapileNodes.EXTEND_TYPE_LIST);
	}

	@Override
	public NapileClassBody getBody()
	{
		return (NapileClassBody) findChildByType(NapileNodes.CLASS_BODY);
	}

	@NotNull
	@Override
	public IStubElementType getElementType()
	{
		return NapileStubElementTypes.CLASS;
	}

	@Override
	public void delete() throws IncorrectOperationException
	{
		NapilePsiUtil.deleteClass(this);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		if(super.isEquivalentTo(another))
		{
			return true;
		}
		if(another instanceof NapileClass)
		{
			FqName fq1 = getFqName();
			FqName fq2 = ((NapileClass) another).getFqName();
			return fq1 != null && fq2 != null && fq1.equals(fq2);
		}
		return false;
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}
}
