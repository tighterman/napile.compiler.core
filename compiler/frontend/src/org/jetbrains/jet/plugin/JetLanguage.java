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

/*
 * @author max
 */
package org.jetbrains.jet.plugin;

import org.jetbrains.jet.lang.resolve.ImportPath;
import com.intellij.lang.Language;

public class JetLanguage extends Language
{
	public static final ImportPath[] DEFAULT_IMPORTS = new ImportPath[]
	{
		new ImportPath("napile.lang.*")
	};

	public static JetLanguage INSTANCE = new JetLanguage();
	public static String NAME = "Napile";

	private JetLanguage()
	{
		super("NAPILE");
	}

	@Override
	public String getDisplayName()
	{
		return NAME;
	}
}
