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

import org.junit.Test;
import org.terracotta.runnel.decoding.ArrayDecoder;
import org.terracotta.runnel.decoding.Enm;
import org.terracotta.runnel.decoding.StructDecoder;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Ludovic Orban
 */
public class SwitchTest {

  enum MyEnum {
    OFF, ON,
  }

  @Test
  public void testStructWithSwitch() throws Exception {
    EnumMapping<MyEnum> myEnumMapping = EnumMappingBuilder.newEnumMappingBuilder(MyEnum.class)
        .mapping(MyEnum.OFF, 0)
        .mapping(MyEnum.ON, 1)
        .build();

    Struct onStruct = StructBuilder.newStructBuilder()
        .string("name", 10)
        .build();

    Struct offStruct = StructBuilder.newStructBuilder()
        .int32("age", 10)
        .build();

    Struct struct = StructBuilder.newStructBuilder()
        ._switch("x", 10, myEnumMapping)
        ._case(MyEnum.ON, onStruct)
        ._case(MyEnum.OFF, offStruct)
        .build();

    {
      ByteBuffer encoded = struct.encoder()
              ._switch("x", MyEnum.ON)
              .string("name", "joe")
              .encode();

      encoded.rewind();

      StructDecoder decoder = struct.decoder(encoded);

      Enm<MyEnum> x = decoder.enm("x");
      System.out.println(x.get());
      String name = decoder.string("name");
      System.out.println(name);
    }
    {
      ByteBuffer encoded = struct.encoder()
              ._switch("x", MyEnum.OFF)
              .int32("age", 12)
              .encode();

      encoded.rewind();

      StructDecoder decoder = struct.decoder(encoded);

      Enm<MyEnum> x = decoder.enm("x");
      System.out.println(x.get());
      Integer age = decoder.int32("age");
      System.out.println(age);
    }

  }

}
