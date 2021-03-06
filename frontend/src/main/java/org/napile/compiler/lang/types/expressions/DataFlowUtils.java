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

package org.napile.compiler.lang.types.expressions;

import static org.napile.compiler.lang.diagnostics.Errors.AUTOCAST_IMPOSSIBLE;
import static org.napile.compiler.lang.diagnostics.Errors.EXPECTED_TYPE_MISMATCH;
import static org.napile.compiler.lang.diagnostics.Errors.EXPRESSION_EXPECTED;
import static org.napile.compiler.lang.diagnostics.Errors.IMPLICIT_CAST_TO_UNIT_OR_ANY;
import static org.napile.compiler.lang.diagnostics.Errors.TYPE_MISMATCH;
import static org.napile.compiler.lang.resolve.BindingTraceKeys.AUTOCAST;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.asm.lib.NapileLangPackage;
import org.napile.compiler.lang.psi.NapileBinaryExpression;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileIsExpression;
import org.napile.compiler.lang.psi.NapileParenthesizedExpression;
import org.napile.compiler.lang.psi.NapileUnaryExpression;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import org.napile.compiler.lang.resolve.BindingTraceKeys;
import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowInfo;
import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowValue;
import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowValueFactory;
import org.napile.compiler.lang.types.ErrorUtils;
import org.napile.compiler.lang.types.NapileType;
import org.napile.compiler.lang.types.NapileTypeInfo;
import org.napile.compiler.lang.types.TypeUtils;
import org.napile.compiler.lang.types.checker.NapileTypeChecker;
import org.napile.compiler.lang.lexer.NapileTokens;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.tree.IElementType;

/**
 * @author abreslav
 */
public class DataFlowUtils
{
	private DataFlowUtils()
	{
	}

	@NotNull
	public static DataFlowInfo extractDataFlowInfoFromCondition(@Nullable NapileExpression condition, final boolean conditionValue, final ExpressionTypingContext context)
	{
		if(condition == null)
			return context.dataFlowInfo;
		final Ref<DataFlowInfo> result = new Ref<DataFlowInfo>(null);
		condition.accept(new NapileVisitorVoid()
		{
			@Override
			public void visitIsExpression(NapileIsExpression expression)
			{
				if(conditionValue && !expression.isNegated() || !conditionValue && expression.isNegated())
				{
					result.set(context.trace.get(BindingTraceKeys.DATAFLOW_INFO_AFTER_CONDITION, expression));
				}
			}

			@Override
			public void visitBinaryExpression(NapileBinaryExpression expression)
			{
				IElementType operationToken = expression.getOperationToken();
				if(OperatorConventions.BOOLEAN_OPERATIONS.contains(operationToken))
				{
					DataFlowInfo dataFlowInfo = extractDataFlowInfoFromCondition(expression.getLeft(), conditionValue, context);
					NapileExpression expressionRight = expression.getRight();
					if(expressionRight != null)
					{
						DataFlowInfo rightInfo = extractDataFlowInfoFromCondition(expressionRight, conditionValue, context);
						DataFlowInfo.CompositionOperator operator;
						if(operationToken == NapileTokens.ANDAND)
						{
							operator = conditionValue ? DataFlowInfo.AND : DataFlowInfo.OR;
						}
						else
						{
							operator = conditionValue ? DataFlowInfo.OR : DataFlowInfo.AND;
						}
						dataFlowInfo = operator.compose(dataFlowInfo, rightInfo);
					}
					result.set(dataFlowInfo);
				}
				else
				{
					NapileExpression left = expression.getLeft();
					NapileExpression right = expression.getRight();
					if(right == null)
						return;

					NapileType lhsType = context.trace.get(BindingTraceKeys.EXPRESSION_TYPE, left);
					if(lhsType == null)
						return;
					NapileType rhsType = context.trace.get(BindingTraceKeys.EXPRESSION_TYPE, right);
					if(rhsType == null)
						return;

					DataFlowValue leftValue = DataFlowValueFactory.INSTANCE.createDataFlowValue(left, lhsType, context.trace);
					DataFlowValue rightValue = DataFlowValueFactory.INSTANCE.createDataFlowValue(right, rhsType, context.trace);

					Boolean equals = null;
					if(operationToken == NapileTokens.EQEQ)
					{
						equals = true;
					}
					else if(operationToken == NapileTokens.EXCLEQ)
					{
						equals = false;
					}
					if(equals != null)
					{
						if(equals == conditionValue)
						{ // this means: equals && conditionValue || !equals && !conditionValue
							result.set(context.dataFlowInfo.equate(leftValue, rightValue));
						}
						else
						{
							result.set(context.dataFlowInfo.disequate(leftValue, rightValue));
						}
					}
				}
			}

			@Override
			public void visitUnaryExpression(NapileUnaryExpression expression)
			{
				IElementType operationTokenType = expression.getOperationReference().getReferencedNameElementType();
				if(operationTokenType == NapileTokens.EXCL)
				{
					NapileExpression baseExpression = expression.getBaseExpression();
					if(baseExpression != null)
					{
						result.set(extractDataFlowInfoFromCondition(baseExpression, !conditionValue, context));
					}
				}
			}

			@Override
			public void visitParenthesizedExpression(NapileParenthesizedExpression expression)
			{
				NapileExpression body = expression.getExpression();
				if(body != null)
				{
					body.accept(this);
				}
			}
		});
		if(result.get() == null)
		{
			return context.dataFlowInfo;
		}
		return context.dataFlowInfo.and(result.get());
	}

	@NotNull
	public static NapileTypeInfo checkType(@Nullable NapileType expressionType, @NotNull NapileExpression expression, @NotNull ExpressionTypingContext context, @NotNull DataFlowInfo dataFlowInfo)
	{
		return NapileTypeInfo.create(checkType(expressionType, expression, context), dataFlowInfo);
	}

	@Nullable
	public static NapileType checkType(@Nullable NapileType expressionType, @NotNull NapileExpression expression, @NotNull ExpressionTypingContext context)
	{
		if(expressionType == null || context.expectedType == null || context.expectedType == TypeUtils.NO_EXPECTED_TYPE ||
				NapileTypeChecker.INSTANCE.isSubtypeOf(expressionType, context.expectedType))
		{
			return expressionType;
		}

		DataFlowValue dataFlowValue = DataFlowValueFactory.INSTANCE.createDataFlowValue(expression, expressionType, context.trace);
		for(NapileType possibleType : context.dataFlowInfo.getPossibleTypes(dataFlowValue))
		{
			if(NapileTypeChecker.INSTANCE.isSubtypeOf(possibleType, context.expectedType))
			{
				if(dataFlowValue.isStableIdentifier())
				{
					context.trace.record(AUTOCAST, expression, possibleType);
				}
				else
				{
					context.trace.report(AUTOCAST_IMPOSSIBLE.on(expression, possibleType, expression.getText()));
				}
				return possibleType;
			}
		}
		context.trace.report(TYPE_MISMATCH.on(expression, context.expectedType, expressionType));
		return expressionType;
	}

	@NotNull
	public static NapileTypeInfo checkStatementType(@NotNull NapileExpression expression, @NotNull ExpressionTypingContext context, @NotNull DataFlowInfo dataFlowInfo)
	{
		return NapileTypeInfo.create(checkStatementType(expression, context), dataFlowInfo);
	}

	@Nullable
	public static NapileType checkStatementType(@NotNull NapileExpression expression, @NotNull ExpressionTypingContext context)
	{
		if(context.expectedType != TypeUtils.NO_EXPECTED_TYPE &&
				!TypeUtils.isEqualFqName(context.expectedType, NapileLangPackage.NULL) &&
				!ErrorUtils.isErrorType(context.expectedType))
		{
			context.trace.report(EXPECTED_TYPE_MISMATCH.on(expression, context.expectedType));
			return null;
		}
		return TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL);
	}

	@NotNull
	public static NapileTypeInfo checkImplicitCast(@Nullable NapileType expressionType, @NotNull NapileExpression expression, @NotNull ExpressionTypingContext context, boolean isStatement, DataFlowInfo dataFlowInfo)
	{
		return NapileTypeInfo.create(checkImplicitCast(expressionType, expression, context, isStatement), dataFlowInfo);
	}

	@Nullable
	public static NapileType checkImplicitCast(@Nullable NapileType expressionType, @NotNull NapileExpression expression, @NotNull ExpressionTypingContext context, boolean isStatement)
	{
		if(expressionType != null && context.expectedType == TypeUtils.NO_EXPECTED_TYPE && !isStatement &&
				(TypeUtils.isEqualFqName(expressionType, NapileLangPackage.NULL) || TypeUtils.isEqualFqName(expressionType, NapileLangPackage.ANY)))
		{
			context.trace.report(IMPLICIT_CAST_TO_UNIT_OR_ANY.on(expression, expressionType));
		}
		return expressionType;
	}

	@NotNull
	public static NapileTypeInfo illegalStatementType(@NotNull NapileExpression expression, @NotNull ExpressionTypingContext context, @NotNull ExpressionTypingInternals facade)
	{
		facade.checkStatementType(expression, context.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE));
		context.trace.report(EXPRESSION_EXPECTED.on(expression, expression));
		return NapileTypeInfo.create(null, context.dataFlowInfo);
	}
}
