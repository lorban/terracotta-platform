/*
 * Copyright Terracotta, Inc.
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
package org.terracotta.runnel.decoding.fields;

import org.terracotta.runnel.Struct;
import org.terracotta.runnel.utils.ReadBuffer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ludovic Orban
 */
public class CaseField<T> extends AbstractField {

  public static final String FIELD_NAME = "case";

  private final EnumField<T> enm;
  private final Map<T, Struct> cases;

  public CaseField(EnumField<T> enm, Map<T, Struct> cases) {
    super(FIELD_NAME, -1);
    this.enm = enm;
    this.cases = cases;
  }

  public List<? extends Field> caseFields(T t) {
    Struct struct = cases.get(t);
    if (struct == null) {
      throw new IllegalArgumentException("unknown case : " + t);
    }
    return struct.getRootSubFields();
  }

  @Override
  public void dump(ReadBuffer readBuffer, PrintStream out, int depth) {
    throw new UnsupportedOperationException("todo: implement me");
  }
}
