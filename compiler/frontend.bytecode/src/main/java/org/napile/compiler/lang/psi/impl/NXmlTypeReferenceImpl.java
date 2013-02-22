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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.psi.NXmlParentedElementBase;
import org.napile.compiler.lang.psi.NapileAnnotation;
import org.napile.compiler.lang.psi.NapileTypeElement;
import org.napile.compiler.lang.psi.NapileTypeReference;
import org.napile.compiler.lang.psi.NapileVisitor;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import org.napile.compiler.util.NXmlMirrorUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;

/**
 * @author VISTALL
 * @date 16:18/16.02.13
 */
public class NXmlTypeReferenceImpl extends NXmlParentedElementBase implements NapileTypeReference
{
	private List<NapileAnnotation> annotations = Collections.emptyList();
	private NapileTypeElement typeElement;

	public NXmlTypeReferenceImpl(PsiElement parent)
	{
		super(parent);
	}

	@Override
	public void setMirror(@NotNull TreeElement element) throws InvalidMirrorException
	{
		NapileTypeReference mirror = SourceTreeToPsiMap.treeToPsiNotNull(element);

		setMirrorCheckingType(element, null);

		typeElement = NXmlMirrorUtil.mirrorTypeElement(this, mirror.getTypeElement());

		setMirror(getTypeElement(), mirror.getTypeElement());

		final List<NapileAnnotation> mirrorAnnotations = mirror.getAnnotations();
		annotations = new ArrayList<NapileAnnotation>(mirrorAnnotations.size());
		for(NapileAnnotation annotation : mirrorAnnotations)
		{
			NXmlAnnotationImpl nXmlAnnotation = new NXmlAnnotationImpl(this);
			nXmlAnnotation.setMirror(annotation);

			annotations.add(nXmlAnnotation);
		}
	}

	@NotNull
	@Override
	public PsiElement[] getChildren()
	{
		return NXmlMirrorUtil.getAllToPsiArray(typeElement, annotations);
	}

	@Nullable
	@Override
	public NapileTypeElement getTypeElement()
	{
		return typeElement;
	}

	@Override
	public List<NapileAnnotation> getAnnotations()
	{
		return annotations;
	}

	@Override
	public void accept(@NotNull NapileVisitorVoid visitor)
	{
		visitor.visitTypeReference(this);
	}

	@Override
	public <R, D> R accept(@NotNull NapileVisitor<R, D> visitor, D data)
	{
		return visitor.visitTypeReference(this, data);
	}
}