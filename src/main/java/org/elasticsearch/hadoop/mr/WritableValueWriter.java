/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.hadoop.mr;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.AbstractMapWritable;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.VLongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.elasticsearch.hadoop.serialization.Generator;
import org.elasticsearch.hadoop.serialization.ValueWriter;

@SuppressWarnings("deprecation")
public class WritableValueWriter implements ValueWriter<Writable> {

    private final boolean writeUnknownTypes;

    public WritableValueWriter() {
        writeUnknownTypes = false;
    }

    public WritableValueWriter(boolean writeUnknownTypes) {
        this.writeUnknownTypes = writeUnknownTypes;
    }

    @SuppressWarnings("unchecked")
    public boolean write(Writable writable, Generator generator) {
        if (writable == null || writable instanceof NullWritable) {
            generator.writeNull();
        }
        else if (writable instanceof Text) {
            Text text = (Text) writable;
            generator.writeUTF8String(text.getBytes(), 0, text.getLength());
        }
        else if (writable instanceof UTF8) {
            UTF8 utf8 = (UTF8) writable;
            generator.writeUTF8String(utf8.getBytes(), 0, utf8.getLength());
        }
        else if (writable instanceof IntWritable) {
            generator.writeNumber(((IntWritable) writable).get());
        }
        else if (writable instanceof LongWritable) {
            generator.writeNumber(((LongWritable) writable).get());
        }
        else if (writable instanceof VLongWritable) {
            generator.writeNumber(((VLongWritable) writable).get());
        }
        else if (writable instanceof VIntWritable) {
            generator.writeNumber(((VIntWritable) writable).get());
        }
        else if (writable instanceof ByteWritable) {
            generator.writeNumber(((ByteWritable) writable).get());
        }
        else if (writable instanceof DoubleWritable) {
            generator.writeNumber(((DoubleWritable) writable).get());
        }
        else if (writable instanceof FloatWritable) {
            generator.writeNumber(((FloatWritable) writable).get());
        }
        else if (writable instanceof BooleanWritable) {
            generator.writeBoolean(((BooleanWritable) writable).get());
        }
        else if (writable instanceof BytesWritable) {
            BytesWritable bw = (BytesWritable) writable;
            generator.writeBinary(bw.getBytes(), 0, bw.getLength());
        }
        else if (writable instanceof MD5Hash) {
            generator.writeString(writable.toString());
        }

        else if (writable instanceof ArrayWritable) {
            generator.writeBeginArray();
            for (Writable wrt : ((ArrayWritable) writable).get()) {
                if (!write(wrt, generator)) {
                    return false;
                }
            }
            generator.writeEndArray();
        }

        else if (writable instanceof AbstractMapWritable) {
            Map<Writable, Writable> map = (Map<Writable, Writable>) writable;

            generator.writeBeginObject();
            // ignore handling sets (which are just maps with null values)
            for (Entry<Writable, Writable> entry : map.entrySet()) {
                generator.writeFieldName(entry.getKey().toString());
                if (!write(entry.getValue(), generator)) {
                    return false;
                }
            }
            generator.writeEndObject();
        }
        else {
            if (writeUnknownTypes) {
                return handleUnknown(writable, generator);
            }
            return false;
        }
        return true;
    }

    protected boolean handleUnknown(Writable value, Generator generator) {
        generator.writeBinary(WritableUtils.toByteArray(value));
        return true;
    }
}