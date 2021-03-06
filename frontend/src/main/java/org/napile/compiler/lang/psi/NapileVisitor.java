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

import com.intellij.psi.PsiElementVisitor;

/**
 * @author svtk
 */
public class NapileVisitor<R, D> extends PsiElementVisitor
{
	public R visitNapileFile(NapileFile dcl, D data)
	{
		return visitJetElement(dcl, data);
	}

	public R visitJetElement(NapileElement element, D data)
	{
		visitElement(element);
		return null;
	}

	public R visitDeclaration(NapileDeclaration dcl, D data)
	{
		return visitExpression(dcl, data);
	}

	public R visitClass(NapileClass klass, D data)
	{
		return visitNamedDeclaration(klass, data);
	}

	public R visitConstructor(NapileConstructor constructor, D data)
	{
		return visitNamedDeclaration(constructor, data);
	}

	public R visitNamedMethod(NapileNamedMethodOrMacro function, D data)
	{
		return visitNamedMethodOrMacro(function, data);
	}

	public R visitNamedMacro(NapileNamedMethodOrMacro function, D data)
	{
		return visitNamedMethodOrMacro(function, data);
	}

	public R visitNamedMethodOrMacro(NapileNamedMethodOrMacro function, D data)
	{
		return visitNamedDeclaration(function, data);
	}

	public R visitVariable(NapileVariable property, D data)
	{
		return visitNamedDeclaration(property, data);
	}

	public R visitEnumValue(NapileEnumValue value, D data)
	{
		return visitNamedDeclaration(value, data);
	}

	public R visitImportDirective(NapileImportDirective importDirective, D data)
	{
		return visitJetElement(importDirective, data);
	}

	public R visitClassBody(NapileClassBody classBody, D data)
	{
		return visitJetElement(classBody, data);
	}

	public R visitModifierList(NapileModifierList list, D data)
	{
		return visitJetElement(list, data);
	}

	public R visitAnnotation(NapileAnnotation annotationEntry, D data)
	{
		return visitJetElement(annotationEntry, data);
	}

	public R visitTypeParameterList(NapileTypeParameterList list, D data)
	{
		return visitJetElement(list, data);
	}

	public R visitTypeParameter(NapileTypeParameter parameter, D data)
	{
		return visitNamedDeclaration(parameter, data);
	}

	public R visitCallParameterList(NapileCallParameterList list, D data)
	{
		return visitJetElement(list, data);
	}

	public R visitCallParameterAsVariable(NapileCallParameterAsVariable parameter, D data)
	{
		return visitNamedDeclaration(parameter, data);
	}

	public R visitDelegationSpecifierList(NapileDelegationSpecifierList list, D data)
	{
		return visitJetElement(list, data);
	}

	public R visitDelegationToSuperCallSpecifier(NapileDelegationToSuperCall call, D data)
	{
		return visitJetElement(call, data);
	}

	public R visitTypeReference(NapileTypeReference typeReference, D data)
	{
		return visitJetElement(typeReference, data);
	}

	public R visitValueArgumentList(NapileValueArgumentList list, D data)
	{
		return visitJetElement(list, data);
	}

	public R visitArgument(NapileValueArgument argument, D data)
	{
		return visitJetElement(argument, data);
	}

	public R visitExpression(NapileExpression expression, D data)
	{
		return visitJetElement(expression, data);
	}

	public R visitLoopExpression(NapileLoopExpression loopExpression, D data)
	{
		return visitExpression(loopExpression, data);
	}

	public R visitConstantExpression(NapileConstantExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitSimpleNameExpression(NapileSimpleNameExpression expression, D data)
	{
		return visitReferenceExpression(expression, data);
	}

	public R visitReferenceExpression(NapileReferenceExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitPrefixExpression(NapilePrefixExpression expression, D data)
	{
		return visitUnaryExpression(expression, data);
	}

	public R visitPostfixExpression(NapilePostfixExpression expression, D data)
	{
		return visitUnaryExpression(expression, data);
	}

	public R visitUnaryExpression(NapileUnaryExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitBinaryExpression(NapileBinaryExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitReturnExpression(NapileReturnExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitThrowExpression(NapileThrowExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitBreakExpression(NapileBreakExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitContinueExpression(NapileContinueExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitIfExpression(NapileIfExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitLabelExpression(NapileLabelExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitWhenExpression(NapileWhenExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitTryExpression(NapileTryExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitForExpression(NapileForExpression expression, D data)
	{
		return visitLoopExpression(expression, data);
	}

	public R visitWhileExpression(NapileWhileExpression expression, D data)
	{
		return visitLoopExpression(expression, data);
	}

	public R visitDoWhileExpression(NapileDoWhileExpression expression, D data)
	{
		return visitLoopExpression(expression, data);
	}

	public R visitAnonymMethodExpression(NapileAnonymMethodExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitCallExpression(NapileCallExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitArrayAccessExpression(NapileArrayAccessExpressionImpl expression, D data)
	{
		return visitReferenceExpression(expression, data);
	}

	public R visitQualifiedExpression(NapileQualifiedExpressionImpl expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitLinkMethodExpression(NapileLinkMethodExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitDotQualifiedExpression(NapileDotQualifiedExpressionImpl expression, D data)
	{
		return visitQualifiedExpression(expression, data);
	}

	public R visitSafeQualifiedExpression(NapileSafeQualifiedExpressionImpl expression, D data)
	{
		return visitQualifiedExpression(expression, data);
	}

	public R visitAnonymClassExpression(NapileAnonymClassExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitBlockExpression(NapileBlockExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitIdeTemplate(NapileIdeTemplate expression, D data)
	{
		return null;
	}

	public R visitCatchSection(NapileCatchClause catchClause, D data)
	{
		return visitJetElement(catchClause, data);
	}

	public R visitFinallySection(NapileFinallySection finallySection, D data)
	{
		return visitJetElement(finallySection, data);
	}

	public R visitTypeArgumentList(NapileTypeArgumentList typeArgumentList, D data)
	{
		return visitJetElement(typeArgumentList, data);
	}

	public R visitThisExpression(NapileThisExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitSuperExpression(NapileSuperExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitParenthesizedExpression(NapileParenthesizedExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	private R visitTypeElement(NapileTypeElement type, D data)
	{
		return visitJetElement(type, data);
	}

	public R visitUserType(NapileUserType type, D data)
	{
		return visitTypeElement(type, data);
	}

	public R visitMethodType(NapileMethodType type, D data)
	{
		return visitTypeElement(type, data);
	}

	public R visitSelfType(NapileSelfType type, D data)
	{
		return visitTypeElement(type, data);
	}

	public R visitBinaryWithTypeRHSExpression(NapileBinaryExpressionWithTypeRHS expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitNamedDeclaration(NapileNamedDeclaration declaration, D data)
	{
		return visitDeclaration(declaration, data);
	}

	public R visitNullableType(NapileNullableType nullableType, D data)
	{
		return visitTypeElement(nullableType, data);
	}

	public R visitWhenEntry(NapileWhenEntry jetWhenEntry, D data)
	{
		return visitJetElement(jetWhenEntry, data);
	}

	public R visitIsExpression(NapileIsExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitWhenConditionIsPattern(NapileWhenConditionIsPattern condition, D data)
	{
		return visitJetElement(condition, data);
	}

	public R visitWhenConditionInRange(NapileWhenConditionInRange condition, D data)
	{
		return visitJetElement(condition, data);
	}

	public R visitWhenConditionExpression(NapileWhenConditionWithExpression condition, D data)
	{
		return visitJetElement(condition, data);
	}

	public R visitAnonymClass(NapileAnonymClass element, D data)
	{
		return visitNamedDeclaration(element, data);
	}

	public R visitCallParameterAsReference(NapileCallParameterAsReference napileReferenceParameter, D data)
	{
		return visitJetElement(napileReferenceParameter, data);
	}

	public R visitClassOfExpression(NapileClassOfExpression classOfExpression, D data)
	{
		return visitExpression(classOfExpression, data);
	}

	public R visitTypeOfExpression(NapileTypeOfExpression typeOfExpression, D data)
	{
		return visitExpression(typeOfExpression, data);
	}

	public R visitInjectionExpression(NapileInjectionExpression codeInjection, D data)
	{
		return visitExpression(codeInjection, data);
	}

	public R visitArrayOfExpression(NapileArrayOfExpression arrayExpression, D data)
	{
		return visitExpression(arrayExpression, data);
	}

	public R visitVariableAccessor(NapileVariableAccessor accessor, D data)
	{
		return visitNamedDeclaration(accessor, data);
	}

	public R visitMultiType(NapileMultiType multiType, D data)
	{
		return visitTypeElement(multiType, data);
	}

	public R visitMultiTypeExpression(NapileMultiTypeExpression expression, D data)
	{
		return visitExpression(expression, data);
	}

	public R visitDoubleArrowExpression(NapileDoubleArrowExpression expression, D data)
	{
		return visitExpression(expression, data);
	}
}
