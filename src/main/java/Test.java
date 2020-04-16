import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import sun.misc.Unsafe;

public class Test {
    
    public static void main(String[] args) {
//        Buffer buffer = ByteBuffer.allocateDirect(1 << 10 << 10 << 10);
//        
//        final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
//            public Unsafe run() throws Exception {
//                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
//                theUnsafe.setAccessible(true);
//                return (Unsafe) theUnsafe.get(null);
//            }
//        };
//        try {
//            Unsafe unsafe = AccessController.doPrivileged(action);
//            unsafe.allocateMemory(1 << 10 << 10 << 10);
//        } catch (PrivilegedActionException e) {
//            e.printStackTrace();
//        }
        
    }
}
