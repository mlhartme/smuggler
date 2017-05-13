package net.mlhartme.smuggler.macos;

import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

import java.io.IOException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        MacOsSystem system;
        List<Long> pthreads;
        List<Integer> mach_threads;

        system = new MacOsSystem();
/*        for (int i = 0; i < 10; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.setDaemon(true);
            t.start();
        }
  */
        System.out.println("pid=" + system.getpid());
        long task = system.mach_task_self();
        System.out.println("task: " + task);
        System.out.println("pid_for_task:" + system.pid_for_task(task));
        pthreads = system.proc_pidlistthreads(system.getpid());
        System.out.println("proc_threads=" + pthreads);
        mach_threads = system.mach_task_threads(task);
        System.out.println("task_threads=" + mach_threads);
        for (int thread : mach_threads) {
            thread_identifier_info ti;
            ti = system.thread_identifier_info(thread);
            System.out.println("  handle " + ti.thread_handle.get());
            System.out.println("  id " + ti.thread_id.get());
        }
    }

    // /usr/include/sys/proc_info
    public static class proc_bsdinfo extends Struct {
        public final u_int32_t pbi_flags = new u_int32_t();
        public final u_int32_t pbi_status = new u_int32_t();
        public final u_int32_t pbi_xstatus = new u_int32_t();
        public final u_int32_t pbi_pid = new u_int32_t();
        public final u_int32_t pbi_ppid = new u_int32_t();
        public final uid_t pbi_uid = new uid_t();
        public final gid_t pbi_gid = new gid_t();
        public final uid_t pbi_ruid = new uid_t();
        public final gid_t pbi_rgid = new gid_t();
        public final uid_t pbi_svuid = new uid_t();
        public final gid_t pbi_svgid = new gid_t();
        public final u_int32_t rfu_1 = new u_int32_t();
        public final Padding pbi_comm = new Padding(NativeType.UCHAR, 16);
        public final Padding pbi_name = new Padding(NativeType.UCHAR, 2 * 16);
        public final u_int32_t pbi_nfiles = new u_int32_t();
        public final u_int32_t pbi_pgid = new u_int32_t();
        public final u_int32_t pbi_pjobc = new u_int32_t();
        public final u_int32_t e_tdev = new u_int32_t();
        public final u_int32_t e_tpgid = new u_int32_t();
        public final int32_t pbi_nice = new int32_t();
        public final u_int64_t pbi_start_tvsec = new u_int64_t();
        public final u_int64_t pbi_start_tvusec = new u_int64_t();

        public proc_bsdinfo(Runtime runtime) {
            super(runtime);
        }

        public static proc_bsdinfo of(jnr.ffi.Pointer pointer) {
            proc_bsdinfo bsdinfo = new proc_bsdinfo(Runtime.getSystemRuntime());
            bsdinfo.useMemory(pointer);
            return bsdinfo;
        }
    }

    // /usr/include/sys/proc_info
    public static class proc_bsdshortinfo extends Struct {
        public final u_int32_t pbi_pid = new u_int32_t();
        public final u_int32_t pbi_ppid = new u_int32_t();
        public final u_int32_t pbi_pgid = new u_int32_t();
        public final u_int32_t pbi_status = new u_int32_t();

        public proc_bsdshortinfo(Runtime runtime) {
            super(runtime);
        }

        public static proc_bsdshortinfo of(jnr.ffi.Pointer pointer) {
            proc_bsdshortinfo bsdinfo = new proc_bsdshortinfo(Runtime.getSystemRuntime());
            bsdinfo.useMemory(pointer);
            return bsdinfo;
        }
    }
}
