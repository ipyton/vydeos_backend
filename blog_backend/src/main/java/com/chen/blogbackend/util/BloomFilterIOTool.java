package com.chen.blogbackend.util;

import com.datastax.oss.driver.shaded.guava.common.hash.BloomFilter;
import com.datastax.oss.driver.shaded.guava.common.hash.Funnels;
import org.apache.kafka.common.protocol.types.Field;

import java.io.*;
import java.nio.charset.Charset;


@Deprecated
public class BloomFilterIOTool {
    public static <T> BloomFilter<T> readBloomFilter(String filePath, Class<T> clazz) {
        BloomFilter<T> bloomFilter = null;
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

            // 使用 readFrom 方法加载 Bloom Filter，第二个参数需要指定 Funnel

            if (clazz.equals(String.class)) {
                bloomFilter = (BloomFilter<T>) BloomFilter.readFrom(objectIn, Funnels.stringFunnel(Charset.defaultCharset()));
            }
            else if (clazz.equals(Integer.class)) {
                bloomFilter = (BloomFilter<T>) BloomFilter.readFrom(objectIn, Funnels.integerFunnel());
            }
            else {
                System.out.println("unsupported bloom filter type: " + clazz);
            }

            // 测试元素是否存在
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bloomFilter;
    }

    public static boolean writeBloomFilter(BloomFilter<String> bloomFilter,String filePath) {
        try (FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            bloomFilter.writeTo(objectOut);
            System.out.println("Bloom Filter has been persisted to bloomFilter.bin");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
}
