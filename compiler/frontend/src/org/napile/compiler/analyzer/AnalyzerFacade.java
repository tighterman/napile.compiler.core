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

package org.napile.compiler.analyzer;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileFile;
import org.napile.compiler.lang.resolve.AnalyzerScriptParameter;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.resolve.BodiesResolveContext;
import com.google.common.base.Predicate;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * @author Pavel Talanov
 */
public interface AnalyzerFacade
{

	@NotNull
	AnalyzeExhaust analyzeFiles(@NotNull Project project, @NotNull Collection<NapileFile> files, @NotNull List<AnalyzerScriptParameter> scriptParameters, @NotNull Predicate<PsiFile> filesToAnalyzeCompletely);

	@NotNull
	AnalyzeExhaust analyzeBodiesInFiles(@NotNull Project project, @NotNull List<AnalyzerScriptParameter> scriptParameters, @NotNull Predicate<PsiFile> filesForBodiesResolve, @NotNull BindingTrace traceContext, @NotNull BodiesResolveContext bodiesResolveContext);
}