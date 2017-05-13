package net.mlhartme.smuggler.macos;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.types.pid_t;

import java.io.IOException;
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

        int thread_info(@pid_t int tread, long flavor, Pointer buffer, Pointer buffersize);

        int pid_for_task(@pid_t long task, Pointer pid);

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

    //-- mach

    /** @return task */
    public long mach_task_self() {
        return system.mach_task_self();
    }

    public List<Integer> mach_task_threads(long task) {
        Pointer thread_array;
        Pointer count;
        int kr;
        List<Integer> result;

        thread_array = longPointer();
        count = longPointer();
        kr = system.task_threads(task, thread_array, count);
        if (kr != 0) {
            throw new RuntimeException();
        }
        result = new ArrayList<>();

        Pointer resolved = thread_array.getPointer(0);
        for (int i = 0; i < count.getInt(0); i++) {
            result.add((resolved.getInt(i * 4)));
        }
        return result;
    }

    public thread_identifier_info thread_identifier_info(int thread) {
        Pointer tident;
        Pointer count;
        int kr;
        thread_identifier_info result;

        tident = arrayPointer(10240);
        count = longPointer();
        count.putLong(0, 6);
        kr = system.thread_info(thread, 4 /* THREAD_IDENTIFIER_INFO */, tident, count);
        if (kr != 0) {
            throw new RuntimeException("thread_info failed");
        }
        return thread_identifier_info.of(tident);
    }

    public long pid_for_task(long task) throws IOException {
        Pointer pid;
        int kr;

        pid = longPointer();
        kr = system.pid_for_task(task, pid);
        if (kr != 0) {
            throw new IOException("pid_for_task failed");
        }
        return pid.getLong(0);
    }

    //--

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
