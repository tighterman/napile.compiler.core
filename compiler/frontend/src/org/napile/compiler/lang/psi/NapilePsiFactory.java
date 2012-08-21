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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.resolve.ImportPath;
import org.napile.compiler.lexer.NapileKeywordToken;
import org.napile.compiler.lexer.JetTokens;
import org.napile.compiler.plugin.JetFileType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.LocalTimeCounter;

/**
 * @author max
 */
public class NapilePsiFactory
{
	public static ASTNode createValNode(Project project)
	{
		NapileProperty property = createProperty(project, "val x = 1");
		return property.getValOrVarNode();
	}

	public static ASTNode createVarNode(Project project)
	{
		NapileProperty property = createProperty(project, "var x = 1");
		return property.getValOrVarNode();
	}

	public static NapileExpression createExpression(Project project, String text)
	{
		NapileProperty property = createProperty(project, "val x = " + text);
		return property.getInitializer();
	}

	public static NapileValueArgumentList createCallArguments(Project project, String text)
	{
		NapileProperty property = createProperty(project, "val x = foo" + text);
		NapileExpression initializer = property.getInitializer();
		NapileCallExpression callExpression = (NapileCallExpression) initializer;
		return callExpression.getValueArgumentList();
	}

	public static NapileTypeReference createType(Project project, String type)
	{
		NapileProperty property = createProperty(project, "val x : " + type);
		return property.getPropertyTypeRef();
	}

	//the pair contains the first and the last elements of a range
	public static Pair<PsiElement, PsiElement> createColon(Project project)
	{
		NapileProperty property = createProperty(project, "val x : Int");
		return Pair.create(property.findElementAt(5), property.findElementAt(7));
	}

	public static ASTNode createColonNode(Project project)
	{
		NapileProperty property = createProperty(project, "val x: Int");
		return property.getNode().findChildByType(JetTokens.COLON);
	}

	public static PsiElement createWhiteSpace(Project project)
	{
		return createWhiteSpace(project, " ");
	}

	public static PsiElement createWhiteSpace(Project project, String text)
	{
		NapileProperty property = createProperty(project, "val" + text + "x");
		return property.findElementAt(3);
	}

	public static NapileClass createClass(Project project, String text)
	{
		return createDeclaration(project, text, NapileClass.class);
	}

	@NotNull
	public static NapileFile createFile(Project project, String text)
	{
		return createFile(project, "dummy.idea", text);
	}

	@NotNull
	public static NapileFile createFile(Project project, String fileName, String text)
	{
		return (NapileFile) PsiFileFactory.getInstance(project).createFileFromText(fileName, JetFileType.INSTANCE, text, LocalTimeCounter.currentTime(), false);
	}

	public static NapileProperty createProperty(Project project, String name, String type, boolean isVar, @Nullable String initializer)
	{
		String text = (isVar ? "var " : "val ") + name + (type != null ? ":" + type : "") + (initializer == null ? "" : " = " + initializer);
		return createProperty(project, text);
	}

	public static NapileProperty createProperty(Project project, String name, String type, boolean isVar)
	{
		return createProperty(project, name, type, isVar, null);
	}

	public static NapileProperty createProperty(Project project, String text)
	{
		return createClassDeclaration(project, text);
	}

	private static <T> T createClassDeclaration(Project project, String text)
	{
		NapileFile file = createFile(project, "class A {" + text + "}");
		List<NapileClass> dcls = file.getDeclarations();
		assert dcls.size() == 1 : dcls.size();

		return (T) dcls.get(0).getDeclarations().get(0);
	}

	private static <T> T createDeclaration(Project project, String text, Class<T> clazz)
	{
		NapileFile file = createFile(project, text);
		List<NapileClass> dcls = file.getDeclarations();
		assert dcls.size() == 1 : dcls.size();
		@SuppressWarnings("unchecked") T result = (T) dcls.get(0);
		return result;
	}

	public static PsiElement createNameIdentifier(Project project, String name)
	{
		return createProperty(project, name, null, false).getNameIdentifier();
	}

	public static NapileSimpleNameExpression createSimpleName(Project project, String name)
	{
		return (NapileSimpleNameExpression) createProperty(project, name, null, false, name).getInitializer();
	}

	public static NapileNamedFunction createFunction(Project project, String funDecl)
	{
		return createDeclaration(project, funDecl, NapileNamedFunction.class);
	}

	public static NapileModifierList createModifier(Project project, NapileKeywordToken modifier)
	{
		String text = modifier.getValue() + " val x";
		NapileProperty property = createProperty(project, text);
		return property.getModifierList();
	}

	public static NapileExpression createEmptyBody(Project project)
	{
		NapileNamedFunction function = createFunction(project, "fun foo() {}");
		return function.getBodyExpression();
	}

	public static NapileParameter createParameter(Project project, String name, String type)
	{
		NapileNamedFunction function = createFunction(project, "fun foo(" + name + " : " + type + ") {}");
		return function.getValueParameters().get(0);
	}

	@NotNull
	public static NapileImportDirective createImportDirective(Project project, @NotNull String path)
	{
		return createImportDirective(project, new ImportPath(path));
	}

	@NotNull
	public static NapileImportDirective createImportDirective(Project project, @NotNull ImportPath importPath)
	{
		return createImportDirective(project, importPath, null);
	}

	@NotNull
	public static NapileImportDirective createImportDirective(Project project, @NotNull ImportPath importPath, @Nullable String aliasName)
	{
		if(importPath.fqnPart().isRoot())
		{
			throw new IllegalArgumentException("import path must not be empty");
		}

		StringBuilder importDirectiveBuilder = new StringBuilder("import ");
		importDirectiveBuilder.append(importPath.getPathStr());

		if(aliasName != null)
		{
			if(aliasName.isEmpty())
			{
				throw new IllegalArgumentException("Alias must not be empty");
			}

			importDirectiveBuilder.append(" as ").append(aliasName);
		}

		NapileFile namespace = createFile(project, importDirectiveBuilder.toString());
		return namespace.getImportDirectives().iterator().next();
	}

	public static PsiElement createPrimaryConstructor(Project project)
	{
		NapileClass aClass = createClass(project, "class A()");
		return aClass.findElementAt(7).getParent();
	}
}