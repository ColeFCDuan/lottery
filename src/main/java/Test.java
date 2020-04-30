import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import sun.misc.Unsafe;

public class Test {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>(16_000_000);
        for (int i = 0; i < 16_000_000; i++) {
            list.add("" + i);
        }
        long s = System.nanoTime();
        int size = list.size();
        String tmp = null;
        for (int i = 0; i < size; i++) {
            tmp = list.get(i);
            if (tmp.contains("u")) {

            }
            tmp.contains("1");
            if (tmp.contains("s")) {
                
            }
            tmp.contains("1");
            if (tmp.contains("o")) {
                
            }
            tmp.contains("p");
        }
        System.out.println(System.nanoTime() - s);

        s = System.nanoTime();
        list.stream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        list.stream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        list.stream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        System.out.println(System.nanoTime() - s);
        
        s = System.nanoTime();
        list.parallelStream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        list.parallelStream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        list.parallelStream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        System.out.println(System.nanoTime() - s);
        
        s = System.nanoTime();
        list.parallelStream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        list.parallelStream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        list.parallelStream().filter(t -> t.contains("u")).map(t -> t.concat("1")).count();
        System.out.println(System.nanoTime() - s);
    }
}
