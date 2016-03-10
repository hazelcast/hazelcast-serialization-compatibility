/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.compatibilityV1.binary;


import com.hazelcast.core.EntryEventType;
import com.hazelcast.nio.serialization.Portable;

import java.io.Externalizable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

class ReferenceObjects {

    /**
     * PORTABLE IDS
     **/
    static int PORTABLE_FACTORY_ID = 1;
    static int PORTABLE_CLASS_ID = 1;
    static int INNER_PORTABLE_CLASS_ID = 2;

    /**
     * IDENTIFIED DATA SERIALIZABLE IDS
     **/
    static int IDENTIFIED_DATA_SERIALIZABLE_FACTORY_ID = 1;
    static int DATA_SERIALIZABLE_CLASS_ID = 1;

    /**
     * CUSTOM SERIALIZER IDS
     */
    static int CUSTOM_STREAM_SERILAZABLE_ID = 1;
    static int CUSTOM_BYTE_ARRAY_SERILAZABLE_ID = 2;

    /**
     * OBJECTS
     */
    static Object aNullObject = null;
    static boolean aBoolean = true;
    static byte aByte = 113;
    static char aChar = 'x';
    static double aDouble = -897543.3678909d;
    static short aShort = -500;
    static float aFloat = 900.5678f;
    static int anInt = 56789;
    static long aLong = -50992225L;
    static String aString = "this is main portable object created for testing!";
    static boolean[] booleans = {true, false, true};

    static byte[] bytes = {112, 4, -1, 4, 112, -35, 43};
    static char[] chars = {'a', 'b', 'c'};
    static double[] doubles = {-897543.3678909d, 11.1d, 22.2d, 33.3d};
    static short[] shorts = {-500, 2, 3};
    static float[] floats = {900.5678f, 1.0f, 2.1f, 3.4f};
    static int[] ints = {56789, 2, 3};
    static long[] longs = {-50992225L, 1231232141L, 2L, 3L};
    static String[] strings = {"item1", "item2", "item3"};

    static AnInnerPortable anInnerPortable = new AnInnerPortable(anInt, aFloat);
    static CustomStreamSerializable aCustomStreamSerializable = new CustomStreamSerializable(anInt, aFloat);
    static CustomByteArraySerializable aCustomByteArraySerializable = new CustomByteArraySerializable(anInt, aFloat);
    static Portable[] portables = {anInnerPortable, anInnerPortable, anInnerPortable};

    static AnIdentifiedDataSerializable anIdentifiedDataSerializable =
            new AnIdentifiedDataSerializable(aBoolean, aByte, aChar, aDouble, aShort, aFloat, anInt, aLong, aString,
                    booleans, bytes, chars, doubles, shorts, floats, ints, longs, strings,
                    anInnerPortable,
                    (AnIdentifiedDataSerializable) null,
                    aCustomStreamSerializable,
                    aCustomByteArraySerializable);
    static APortable aPortable =
            new APortable(aBoolean, aByte, aChar, aDouble, aShort, aFloat, anInt, aLong, aString, anInnerPortable,
                    booleans, bytes, chars, doubles, shorts, floats, ints, longs, strings, portables,
                    anIdentifiedDataSerializable,
                    aCustomStreamSerializable,
                    aCustomByteArraySerializable);

    static Date aDate = new Date(1990, 2, 1);
    static BigInteger aBigInteger = new BigInteger("1314432323232411");
    static BigDecimal aBigDecimal = new BigDecimal(31231);
    static Class aClass = BigDecimal.class;
    static Enum anEnum = EntryEventType.ADDED;

    static Serializable serializable = new AJavaSerialiazable(anInt, aFloat);
    static Externalizable externalizable = new AJavaExternalizable(anInt, aFloat);

    static ArrayList arrayList =
            new ArrayList(Arrays.asList(aNullObject, aBoolean, aByte, aChar, aDouble, aShort, aFloat, anInt, aLong, aString, anInnerPortable,
                    booleans, bytes, chars, doubles, shorts, floats, ints, longs, strings,
                    aCustomStreamSerializable, aCustomByteArraySerializable,
                    anIdentifiedDataSerializable, aPortable,
                    aDate, aBigInteger, aBigDecimal, aClass, anEnum,
                    serializable, externalizable));

    static LinkedList linkedList = new LinkedList(arrayList);

    static Object[] allTestObjects = {aNullObject, aBoolean, aByte, aChar, aDouble, aShort, aFloat, anInt, aLong, aString, anInnerPortable,
            booleans, bytes, chars, doubles, shorts, floats, ints, longs, strings,
            aCustomStreamSerializable, aCustomByteArraySerializable,
            anIdentifiedDataSerializable, aPortable,
            aDate, aBigInteger, aBigDecimal, aClass, anEnum,
            serializable, externalizable,
            arrayList, linkedList};

}