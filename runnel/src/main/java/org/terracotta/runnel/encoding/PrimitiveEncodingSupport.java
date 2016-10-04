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
package org.terracotta.runnel.encoding;

import java.nio.ByteBuffer;

/**
 * @author Ludovic Orban
 */
public interface PrimitiveEncodingSupport<T> {

  T bool(String name, boolean value);

  T chr(String name, char value);

  <E> T enm(String name, E value);

  T int32(String name, int value);

  T int64(String name, long value);

  T fp64(String name, double value);

  T string(String name, String value);

  T byteBuffer(String name, ByteBuffer value);

}