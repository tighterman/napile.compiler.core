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

package org.napile.idea.plugin.editor;

import org.napile.compiler.lexer.NapileTokens;
import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.tree.IElementType;

/**
 * @author Alefas
 * @since 03.04.12
 */
public class KotlinQuoteHandler implements QuoteHandler
{
	@Override
	public boolean isClosingQuote(HighlighterIterator iterator, int offset)
	{
		IElementType tokenType = iterator.getTokenType();

		if(tokenType == NapileTokens.CHARACTER_LITERAL)
		{
			int start = iterator.getStart();
			int end = iterator.getEnd();
			return end - start >= 1 && offset == end - 1;
		}
		else if(tokenType == NapileTokens.CLOSING_QUOTE)
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isOpeningQuote(HighlighterIterator iterator, int offset)
	{
		final IElementType tokenType = iterator.getTokenType();

		if(tokenType == NapileTokens.OPEN_QUOTE)
		{
			int start = iterator.getStart();
			return offset == start;
		}
		return false;
	}

	@Override
	public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset)
	{
		return true;
	}

	@Override
	public boolean isInsideLiteral(HighlighterIterator iterator)
	{
		final IElementType tokenType = iterator.getTokenType();
		return tokenType == NapileTokens.REGULAR_STRING_PART ||
				tokenType == NapileTokens.OPEN_QUOTE ||
				tokenType == NapileTokens.CLOSING_QUOTE ||
				tokenType == NapileTokens.SHORT_TEMPLATE_ENTRY_START ||
				tokenType == NapileTokens.LONG_TEMPLATE_ENTRY_END ||
				tokenType == NapileTokens.LONG_TEMPLATE_ENTRY_START;
	}
}
