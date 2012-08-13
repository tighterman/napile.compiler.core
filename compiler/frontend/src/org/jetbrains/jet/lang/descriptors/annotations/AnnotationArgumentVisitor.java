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

package org.jetbrains.jet.lang.descriptors.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.constants.BoolValue;
import org.jetbrains.jet.lang.resolve.constants.ByteValue;
import org.jetbrains.jet.lang.resolve.constants.CharValue;
import org.jetbrains.jet.lang.resolve.constants.DoubleValue;
import org.jetbrains.jet.lang.resolve.constants.ErrorValue;
import org.jetbrains.jet.lang.resolve.constants.FloatValue;
import org.jetbrains.jet.lang.resolve.constants.IntValue;
import org.jetbrains.jet.lang.resolve.constants.LongValue;
import org.jetbrains.jet.lang.resolve.constants.NullValue;
import org.jetbrains.jet.lang.resolve.constants.ShortValue;
import org.jetbrains.jet.lang.resolve.constants.StringValue;

/**
 * @author abreslav
 */
public interface AnnotationArgumentVisitor<R, D>
{
	R visitLongValue(@NotNull LongValue value, D data);

	R visitIntValue(IntValue value, D data);

	R visitErrorValue(ErrorValue value, D data);

	R visitShortValue(ShortValue value, D data);

	R visitByteValue(ByteValue value, D data);

	R visitDoubleValue(DoubleValue value, D data);

	R visitFloatValue(FloatValue value, D data);

	R visitBooleanValue(BoolValue value, D data);

	R visitCharValue(CharValue value, D data);

	R visitStringValue(StringValue value, D data);

	R visitNullValue(NullValue value, D data);
}
