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
package org.terracotta.runnel;

import org.terracotta.runnel.decoding.fields.CaseField;
import org.terracotta.runnel.decoding.fields.EnumField;
import org.terracotta.runnel.decoding.fields.Field;
import org.terracotta.runnel.decoding.fields.StructField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ludovic Orban
 */
public class SwitchedStructBuilder<T> {

  private final List<Field> fields;
  private final Set<String> names;
  private final int lastIndex;
  private final Map<T, Struct> cases = new HashMap<T, Struct>();
  private final EnumField<T> enm;

  SwitchedStructBuilder(List<Field> fields, Set<String> names, int lastIndex) {
    this.fields = fields;
    this.names = names;
    this.lastIndex = lastIndex;
    this.enm = (EnumField<T>) fields.get(fields.size() - 1);
  }

  public Struct build() {
    fields.add(new CaseField<T>(enm, cases));
    return new Struct(new StructField("root", -1, fields));
  }


  public SwitchedStructBuilder<T> _case(T t, Struct struct) {
    //todo: is that really needed? in that case, name duplicates should be tested too
//    if (!struct.getRootSubFields().isEmpty() && struct.getRootSubFields().get(0).index() <= lastIndex) {
//      throw new IllegalArgumentException("cannot case struct of " + t + " as its beginning index is <= to the prepending struct's last index : " + lastIndex);
//    }

    if (cases.put(t, struct) != null) {
      throw new IllegalArgumentException("duplicate detected: " + t);
    }
    return this;
  }

}
