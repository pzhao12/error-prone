/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates an {@code @BugPattern} annotation for wellformedness.
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class BugPatternValidator {

  private static final Joiner COMMA_JOINER = Joiner.on(", ");

  public static void validate(BugPattern pattern) throws ValidationException {
    if (pattern == null) {
      throw new ValidationException("No @BugPattern provided");
    }

    // linkType must be consistent with link element.
    switch (pattern.linkType()) {
      case CUSTOM:
        if (pattern.link().isEmpty()) {
          throw new ValidationException("Expected a custom link but none was provided");
        }
        break;
      case AUTOGENERATED:
      case NONE:
        if (!pattern.link().isEmpty()) {
          throw new ValidationException("Expected no custom link but found: " + pattern.link());
        }
        break;
    }

    // suppressibility must be consistent with customSuppressionAnnotations.
    Set<Class<? extends Annotation>> customSuppressionAnnotations =
        new HashSet<>(Arrays.asList(pattern.customSuppressionAnnotations()));
    switch (pattern.suppressibility()) {
      case CUSTOM_ANNOTATION:
        if (customSuppressionAnnotations.isEmpty()) {
          throw new ValidationException(
              "Expected a custom suppression annotation but none was provided");
        } else if (customSuppressionAnnotations.contains(SuppressWarnings.class)) {
          throw new ValidationException(
              "Custom suppression annotation may not use @SuppressWarnings");
        }
        break;
      case SUPPRESS_WARNINGS:
      case UNSUPPRESSIBLE:
        if (!customSuppressionAnnotations.isEmpty()) {
          throw new ValidationException(
              String.format(
                  "Expected no custom suppression annotations but found these: %s",
                  COMMA_JOINER.join(
                      Collections2.transform(
                          customSuppressionAnnotations, Class::getCanonicalName))));
        }
        break;
    }
  }
}
