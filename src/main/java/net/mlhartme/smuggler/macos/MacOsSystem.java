package net.mlhartme.smuggler.macos;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.jffi.ByteBufferMemoryIO;
import jnr.ffi.types.pid_t;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

// see also: https://llvm.org/svn/llvm-project/lldb/trunk/tools/darwin-threads/examine-threads.c
public class MacOsSystem {
    public interface LibSystem  {
        @pid_t
        long getpid();
        int proc_get_dirty(@pid_t int pid, Pointer flags);
        int proc_name(@pid_t int pid, Pointer buffer, int buffersize);
        int proc_listchildpids(@pid_t int ppid, Pointer buffer, int buffersize);
        int proc_listpgrppids(@pid_t int pgrpid, Pointer buffer, int buffersize);
        int proc_pidinfo(@pid_t long pgrpid, long flavor, long arg, Pointer buffer, int buffersize);

        int thread_info(@pid_t long tread, long flavor, Pointer buffer, Pointer buffersize);

        // /usr/include/mach/task.defs
        long mach_task_self();
        int task_threads(@pid_t long task, Pointer buffer, Pointer buffersize);

    }

    private final LibSystem system;

    public MacOsSystem() {
        system = LibraryLoader.create(LibSystem.class).load("System");
    }

    public long getpid() {
        return system.getpid();
    }

    //-- proc_info

    public List<Long> proc_pidlistthreads(long pid) {
        ByteBuffer buffer;
        Pointer pointer;
        int result;
        java.util.List<Long> ids;

        buffer = ByteBuffer.allocate(1024);
        pointer = Pointer.wrap(Runtime.getSystemRuntime(), buffer);
        result = system.proc_pidinfo(pid, 6 /* PROC_PIDLISTTHREADS */, 1, pointer, 1024);
        ids = new ArrayList<>();
        if (result % 8 != 0) {
            throw new IllegalStateException();
        }
        for (int i = 0; i < result / 8; i++) {
            ids.add(pointer.getLong(i));
        }
        return ids;
    }

    public thread_identifier_info thread_identifier_info(long thread) {
        ByteBuffer tidentBuffer;
        Pointer tident;
        ByteBuffer countBuffer;
        Pointer count;
        int kr;

        tidentBuffer = ByteBuffer.allocate(1024);
        tident = Pointer.wrap(Runtime.getSystemRuntime(), tidentBuffer);
        countBuffer = ByteBuffer.allocate(8);
        count = Pointer.wrap(Runtime.getSystemRuntime(), countBuffer);
        kr = system.thread_info(thread, 4 /* THREAD_IDENTIFIER_INFO */, tident, count);
        if (kr != 0) {
            throw new RuntimeException("thread_info failed");
        }
        return thread_identifier_info.of(tident);
    }

    //-- mach

    /** @return task */
    public long mach_task_self() {
        return system.mach_task_self();
    }

    public List<Long> mach_task_threads() {
        long me;
        Pointer thread_array;
        Pointer count;
        int kr;
        int max;
        List<Long> result;

        me = system.mach_task_self();

        thread_array = longPointer();
        count = longPointer();
        kr = system.task_threads(me, thread_array, count);
        if (kr != 0) {
            throw new RuntimeException();
        }
        max = count.getInt(0);
        System.out.println("count: " + max);
        result = new ArrayList<>();

        Pointer resolved = thread_array.getPointer(0);
        for (int i = 0; i < max; i++) {
            result.add(resolved.getLong(i));
        }
        return result;
    }

    private Pointer longPointer() {
        return arrayPointer(8);
    }

    private Pointer arrayPointer(int bytes) {
        ByteBuffer buffer;

        buffer = ByteBuffer.allocate(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return Pointer.wrap(Runtime.getSystemRuntime(), buffer);
    }
}
