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

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.internal.serialization.impl.HeapData;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.serialization.SerializationService;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static junit.framework.TestCase.assertTrue;

@RunWith(Parameterized.class)
@Category(QuickTest.class)
public class BinaryCompatibilityTest {

    public static byte version = 1;
    private static final int NULL_OBJECT = -1;

    @Parameterized.Parameter(0)
    public boolean allowUnsafe;
    @Parameterized.Parameter(1)
    public Object object;
    @Parameterized.Parameter(2)
    public ByteOrder byteOrder;
    @Parameterized.Parameter(3)
    public boolean enableSharedObject;
    @Parameterized.Parameter(4)
    public boolean enableCompression;
    @Parameterized.Parameter(5)
    public boolean useNativeByteOrder;

    @Parameterized.Parameters(name = "allowUnsafe:{0} , object:{1}, isBigEndian:{2}, enableSharedObject:{3}, enableCompression:{4}, useNativeByteOrder:{5}")
    public static Iterable<Object[]> parameters() {

        Object[] objects = ReferenceObjects.allTestObjects;

        boolean[] unsafeAllowedOpts = {true, false};
        boolean[] enableSharedObjectOpts = {true, false};
        boolean[] enableCompressionOpts = {true, false};
        boolean[] useNativeByteOrderOpts = {true, false};

        ByteOrder[] byteOrders = {ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN};
        LinkedList<Object[]> parameters = new LinkedList<Object[]>();
        for (boolean unsafeAllowed : unsafeAllowedOpts) {
            for (Object object : objects) {
                for (ByteOrder byteOrder : byteOrders) {
                    for (boolean enableSharedObject : enableSharedObjectOpts) {
                        for (boolean enableCompression : enableCompressionOpts) {
                            for (boolean useNativeByteOrder : useNativeByteOrderOpts) {
                                parameters.add(new Object[]{unsafeAllowed, object, byteOrder, enableSharedObject, enableCompression, useNativeByteOrder});
                            }
                        }
                    }
                }
            }
        }

        return parameters;
    }

    private String createFileName() {
        return version + "-" +
                allowUnsafe + "-" +
                (object == null ? "NULL" : object.getClass().getSimpleName()) + "-" +
                byteOrder + "-" +
                enableSharedObject + "-" +
                enableCompression + "-" +
                useNativeByteOrder + ".binary";
    }

    private SerializationService createSerializationService() {
        SerializationConfig config = new SerializationConfig();
        {
            SerializerConfig serializerConfig = new SerializerConfig();
            serializerConfig.setImplementation(new CustomByteArraySerializer()).setTypeClass(CustomByteArraySerializable.class);
            config.addSerializerConfig(serializerConfig);
        }
        {
            SerializerConfig serializerConfig = new SerializerConfig();
            serializerConfig.setImplementation(new CustomStreamSerializer()).setTypeClass(CustomStreamSerializable.class);
            config.addSerializerConfig(serializerConfig);
        }
        ClassDefinition classDefinition =
                new ClassDefinitionBuilder(ReferenceObjects.PORTABLE_FACTORY_ID, ReferenceObjects.INNER_PORTABLE_CLASS_ID)
                        .addIntField("i").addFloatField("f").build();

        return new DefaultSerializationServiceBuilder()
                .setVersion(version)
                .setByteOrder(byteOrder)
                .setAllowUnsafe(allowUnsafe)
                .setEnableCompression(enableCompression)
                .setEnableSharedObject(enableSharedObject)
                .setUseNativeByteOrder(useNativeByteOrder)
                .addPortableFactory(ReferenceObjects.PORTABLE_FACTORY_ID, new APortableFactory())
                .addDataSerializableFactory(ReferenceObjects.IDENTIFIED_DATA_SERIALIZABLE_FACTORY_ID,
                        new ADataSerializableFactory())
                .setConfig(config)
                .addClassDefinition(classDefinition)
                .build();
    }

    @Test
    public void readAndVerifyCreatedBinaryFiles() throws IOException {
        InputStream input = BinaryCompatibilityTest.class.getResourceAsStream("/" + createFileName());
        DataInputStream inputStream = new DataInputStream(input);
        int length = inputStream.readInt();
        HeapData data = null;
        if (length != NULL_OBJECT) {
            byte[] bytes = new byte[length];
            inputStream.read(bytes);
            data = new HeapData(bytes);
        }
        inputStream.close();
        SerializationService serializationService = createSerializationService();
        Object readObject = serializationService.toObject(data);

        assertTrue(equals(object, readObject));
    }


    /**
     * This method is used for generating binary files to be committed at the beginning of
     * introducing a new serialization service. Run this method once and move the created files to resources
     * directory.
     * <p/>
     * mv *binary src/test/resources/
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void generateBinaryFiles() throws IOException {
        SerializationService serializationService = createSerializationService();
        Data data = serializationService.toData(object);
        OutputStream out = new FileOutputStream(createFileName());
        DataOutputStream outputStream = new DataOutputStream(out);
        if (data == null) {
            outputStream.writeInt(NULL_OBJECT);
            out.close();
            return;
        }
        byte[] bytes = data.toByteArray();
        outputStream.writeInt(bytes.length);
        out.write(bytes);
        out.close();
    }

    @Test
    public void basicSerializeDeserialize() throws IOException {
        SerializationService serializationService = createSerializationService();
        Data data = serializationService.toData(object);
        Object readObject = serializationService.toObject(data);
        assertTrue(equals(object, readObject));
    }


    public static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass().isArray() && b.getClass().isArray()) {

            int length = Array.getLength(a);
            if (length > 0 && !a.getClass().getComponentType().equals(b.getClass().getComponentType())) {
                return false;
            }
            if (Array.getLength(b) != length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!equals(Array.get(a, i), Array.get(b, i))) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof List && b instanceof List) {
            ListIterator e1 = ((List) a).listIterator();
            ListIterator e2 = ((List) b).listIterator();
            while (e1.hasNext() && e2.hasNext()) {
                Object o1 = e1.next();
                Object o2 = e2.next();
                if (!equals(o1, o2)) {
                    return false;
                }
            }
            return !(e1.hasNext() || e2.hasNext());
        }
        return a.equals(b);
    }


}
