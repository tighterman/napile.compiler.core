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

package org.napile.compiler.lang.psi.impl;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.asm.resolve.name.FqName;
import org.napile.asm.resolve.name.Name;
import org.napile.compiler.lang.lexer.NapileToken;
import org.napile.compiler.lang.psi.NXmlParentedElementBase;
import org.napile.compiler.lang.psi.NapileCallParameterAsVariable;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileModifierList;
import org.napile.compiler.lang.psi.NapilePsiUtil;
import org.napile.compiler.lang.psi.NapileTypeReference;
import org.napile.compiler.lang.psi.NapileVisitor;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import org.napile.compiler.util.NXmlMirrorUtil;
import org.napile.doc.lang.psi.NapileDoc;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 14:54/17.02.13
 */
public class NXmlCallParameterAsVariableImpl extends NXmlParentedElementBase implements NapileCallParameterAsVariable
{
	private NapileExpression defaultValue;
	private NXmlTypeReferenceImpl returnType;
	private boolean mutable;
	private boolean ref;
	private NXmlIdentifierImpl nameIdentifier;

	public NXmlCallParameterAsVariableImpl(PsiElement parent, PsiElement mirror)
	{
		super(parent, mirror);
	}

	@Override
	public void setMirror(@NotNull TreeElement element) throws InvalidMirrorException
	{
		NapileCallParameterAsVariable mirror = SourceTreeToPsiMap.treeToPsiNotNull(element);

		setMirrorCheckingType(element, null);

		returnType = new NXmlTypeReferenceImpl(this, mirror.getTypeReference());
		nameIdentifier = new NXmlIdentifierImpl(this, mirror.getNameIdentifier());
		mutable = mirror.isMutable();
		ref = mirror.isRef();
		final NapileExpression defaultValue = mirror.getDefaultValue();
		if(defaultValue != null)
		{
			this.defaultValue = NXmlMirrorUtil.mirrorExpression(this, defaultValue);
		}
	}

	@Nullable
	@Override
	public NapileTypeReference getTypeReference()
	{
		return returnType;
	}

	@Nullable
	@Override
	public ASTNode getVarOrValNode()
	{
		return null;
	}

	@Override
	public boolean isMutable()
	{
		return mutable;
	}

	@Override
	public boolean isRef()
	{
		return ref;
	}

	@Override
	public NapileExpression getDefaultValue()
	{
		return defaultValue;
	}

	@NotNull
	@Override
	public Name getNameAsSafeName()
	{
		return Name.identifier(getName());
	}

	@Nullable
	@Override
	public NapileDoc getDocComment()
	{
		return null;
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitCallParameterAsVariable(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitCallParameterAsVariable(this, data);
	}

	@Nullable
	@Override
	public NapileModifierList getModifierList()
	{
		return null;
	}

	@Override
	public boolean hasModifier(NapileToken modifier)
	{
		return false;
	}

	@Nullable
	@Override
	public ASTNode getModifierNode(NapileToken token)
	{
		return null;
	}

	@Nullable
	@Override
	public Name getNameAsName()
	{
		return Name.identifier(getName());
	}

	@Override
	public String getName()
	{
		return getNameIdentifier().getText();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return nameIdentifier;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}

	@NotNull
	@Override
	public PsiElement[] getChildren()
	{
		return NXmlMirrorUtil.getAllToPsiArray(defaultValue, returnType, nameIdentifier);
	}

	@NotNull
	@Override
	public FqName getFqName()
	{
		return NapilePsiUtil.getFQNameImpl(this);
	}
}
