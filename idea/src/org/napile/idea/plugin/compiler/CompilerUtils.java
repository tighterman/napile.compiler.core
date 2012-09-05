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

package org.napile.idea.plugin.compiler;

import static com.intellij.openapi.compiler.CompilerMessageCategory.ERROR;
import static com.intellij.openapi.compiler.CompilerMessageCategory.INFORMATION;
import static com.intellij.openapi.compiler.CompilerMessageCategory.STATISTICS;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

/**
 * @author Pavel Talanov
 */
public final class CompilerUtils
{
	private static final Logger LOG = Logger.getInstance(CompilerUtils.class);

	private CompilerUtils()
	{
	}

	static void handleProcessTermination(int exitCode, CompileContext compileContext)
	{
		if(exitCode != 0 && exitCode != 1)
		{
			compileContext.addMessage(ERROR, "Compiler terminated with exit code: " + exitCode, "", -1, -1);
		}
	}

	public static void parseCompilerMessagesFromReader(CompileContext compileContext, final Reader reader)
	{
		// Sometimes the compiler can't output valid XML
		// Example: error in command line arguments passed to the compiler
		// having no -tags key (arguments are not parsed), the compiler doesn't know
		// if it should put any tags in the output, so it will simply print the usage
		// and the SAX parser will break.
		// In this case, we want to read everything from this stream
		// and report it as an IDE error.
		final StringBuilder stringBuilder = new StringBuilder();
		//noinspection IOResourceOpenedButNotSafelyClosed
		Reader wrappingReader = new Reader()
		{

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException
			{
				int read = reader.read(cbuf, off, len);
				stringBuilder.append(cbuf, off, len);
				return read;
			}

			@Override
			public void close() throws IOException
			{
				// Do nothing:
				// If the SAX parser sees a syntax error, it throws an exception
				// and calls close() on the reader.
				// We prevent hte reader from being closed here, and close it later,
				// when all the text is read from it
			}
		};
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(wrappingReader), new CompilerOutputSAXHandler(compileContext));
		}
		catch(Throwable e)
		{

			// Load all the text into the stringBuilder
			try
			{
				// This will not close the reader (see the wrapper above)
				FileUtil.loadTextAndClose(wrappingReader);
			}
			catch(IOException ioException)
			{
				LOG.error(ioException);
			}
			String message = stringBuilder.toString();
			LOG.error(message);
			LOG.error(e);
			compileContext.addMessage(ERROR, message, null, -1, -1);
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch(IOException e)
			{
				LOG.error(e);
			}
		}
	}


	private static class CompilerOutputSAXHandler extends DefaultHandler
	{
		private static final Map<String, CompilerMessageCategory> CATEGORIES = ImmutableMap.<String, CompilerMessageCategory>builder().put("error", CompilerMessageCategory.ERROR).put("warning", CompilerMessageCategory.WARNING).put("logging", CompilerMessageCategory.STATISTICS).put("exception", CompilerMessageCategory.ERROR).put("info", CompilerMessageCategory.INFORMATION).put("messages", CompilerMessageCategory.INFORMATION) // Root XML element
				.build();

		private final CompileContext compileContext;

		private final StringBuilder message = new StringBuilder();
		private Stack<String> tags = new Stack<String>();
		private String path;
		private int line;
		private int column;

		public CompilerOutputSAXHandler(CompileContext compileContext)
		{
			this.compileContext = compileContext;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			tags.push(qName);

			message.setLength(0);

			String rawPath = attributes.getValue("path");
			path = rawPath == null ? null : "file://" + rawPath;
			line = safeParseInt(attributes.getValue("line"), -1);
			column = safeParseInt(attributes.getValue("column"), -1);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			if(tags.size() == 1)
			{
				// We're directly inside the root tag: <MESSAGES>
				String message = new String(ch, start, length);
				if(!message.trim().isEmpty())
				{
					compileContext.addMessage(ERROR, "Unhandled compiler output: " + message, null, -1, -1);
				}
			}
			else
			{
				message.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if(tags.size() == 1)
			{
				// We're directly inside the root tag: <MESSAGES>
				return;
			}
			String qNameLowerCase = qName.toLowerCase();
			CompilerMessageCategory category = CATEGORIES.get(qNameLowerCase);
			if(category == null)
			{
				compileContext.addMessage(ERROR, "Unknown compiler message tag: " + qName, null, -1, -1);
				category = INFORMATION;
			}
			String text = message.toString();

			if("exception".equals(qNameLowerCase))
			{
				LOG.error(text);
			}

			if(category == STATISTICS)
			{
				compileContext.getProgressIndicator().setText(text);
			}
			else
			{
				compileContext.addMessage(category, text, path, line, column);
			}
			tags.pop();
		}

		private static int safeParseInt(@Nullable String value, int defaultValue)
		{
			if(value == null)
			{
				return defaultValue;
			}
			try
			{
				return Integer.parseInt(value.trim());
			}
			catch(NumberFormatException e)
			{
				return defaultValue;
			}
		}
	}

	public static void outputCompilerMessagesAndHandleExitCode(@NotNull CompileContext context, @NotNull Function1<PrintStream, Integer> compilerRun)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outputStream);

		int exitCode = compilerRun.invoke(out);

		BufferedReader reader = new BufferedReader(new StringReader(outputStream.toString()));
		parseCompilerMessagesFromReader(context, reader);
		handleProcessTermination(exitCode, context);
	}
}

