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
package org.terracotta.runnel.utils;

import java.nio.ByteBuffer;

/**
 * @author Ludovic Orban
 */
public class VLQ {

  public static void encode(int value, ByteBuffer out) {
    encode(value, false, out);
  }

  public static void encode(int value, boolean pad, ByteBuffer out) {
    if (value < 0) {
      throw new IllegalArgumentException("Cannot encode negative values");
    }

    int b;
    boolean msbFound = false;
    b = ((value & 0x70000000) >> 28); // 0111 0000 0000 0000 0000 0000 0000 0000
    if (b != 0 || pad) {
      out.put(((byte) (b | 0x80)));
      msbFound = true;
    }
    b = ((value & 0xFE00000) >> 21);  // 0000 1111 1110 0000 0000 0000 0000 0000
    if (msbFound || b != 0) {
      out.put(((byte) (b | 0x80)));
      msbFound = true;
    }
    b = ((value & 0x1FC000) >> 14);   // 0000 0000 0001 1111 1100 0000 0000 0000
    if (msbFound || b != 0) {
      out.put(((byte) (b | 0x80)));
      msbFound = true;
    }
    b = ((value & 0x3F80) >> 7);      // 0000 0000 0000 0000 0011 1111 1000 0000
    if (msbFound || b != 0) {
      out.put(((byte) (b | 0x80)));
    }
    b = (value & 0x7F);               // 0000 0000 0000 0000 0000 0000 0111 1111
    out.put((byte) (b));
  }

  public static int decode(ReadBuffer in) {
    int b;
    int value = 0;

    for (int i = 0; i < 5; i++) {
      b = in.getByte();
      value |= b & 0x7F;
      if ((b & 0x80) == 0) {
        return value;
      }
      value <<= 7;
    }

    throw new IllegalArgumentException("Cannot decode value greater than Integer.MAX_VALUE");
  }

  public static int encodedSize(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Cannot encode negative values");
    }
    int i = 1;
    while ((value & 0xFFFFFF80) != 0L) {
      value >>>= 7;
      i++;
    }
    return i;
  }

}
