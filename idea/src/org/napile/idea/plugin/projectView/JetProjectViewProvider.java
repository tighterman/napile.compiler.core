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

package org.napile.idea.plugin.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.napile.compiler.lang.psi.NapileClassBody;
import org.napile.compiler.psi.NapileClass;
import org.napile.compiler.psi.NapileClassLike;
import org.napile.compiler.psi.NapileDeclaration;
import org.napile.compiler.psi.NapileFile;
import org.napile.idea.plugin.util.FileRootUtil;
import com.intellij.ide.projectView.SelectableTreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * User: Alefas
 * Date: 15.02.12
 */
public class JetProjectViewProvider implements SelectableTreeStructureProvider, DumbAware
{
	private final Project myProject;

	public JetProjectViewProvider(Project project)
	{
		myProject = project;
	}

	@Override
	public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		List<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>(children.size());

		for(AbstractTreeNode child : children)
		{
			Object childValue = child.getValue();

			if(childValue instanceof NapileFile)
			{
				NapileFile file = (NapileFile) childValue;

				NapileClass mainClass = JetProjectViewUtil.getClassIfHeSingle(file);
				if(mainClass != null)
					result.add(new NapileClassTreeNode(file.getProject(), mainClass, settings));
				else
					result.add(new JetFileTreeNode(file.getProject(), file, settings));
			}
			else
			{
				result.add(child);
			}
		}

		return result;
	}

	@Override
	public Object getData(Collection<AbstractTreeNode> selected, String dataName)
	{
		return null;
	}

	@Override
	public PsiElement getTopLevelElement(PsiElement element)
	{
		PsiFile file = element.getContainingFile();
		if(file == null || !(file instanceof NapileFile))
			return null;

		VirtualFile virtualFile = file.getVirtualFile();
		assert virtualFile != null;

		if(!FileRootUtil.isNapileSourceFile(myProject, virtualFile))
			return file;

		PsiElement current = element;
		while(current != null)
		{
			if(isSelectable(current))
				break;
			current = current.getParent();
		}

		if(current instanceof NapileFile)
		{
			List<NapileClass> declarations = ((NapileFile) current).getDeclarations();
			String nameWithoutExtension = virtualFile != null ? virtualFile.getNameWithoutExtension() : file.getName();
			if(declarations.size() == 1 && declarations.get(0) != null &&
					nameWithoutExtension.equals(declarations.get(0).getName()))
			{
				current = declarations.get(0);
			}
		}

		return current != null ? current : file;
	}

	private static boolean isSelectable(PsiElement element)
	{
		if(element instanceof NapileFile)
			return true;
		if(element instanceof NapileDeclaration)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof NapileFile)
			{
				return true;
			}
			else if(parent instanceof NapileClassBody)
			{
				parent = parent.getParent();
				if(parent instanceof NapileClassLike)
				{
					return isSelectable(parent);
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
}
