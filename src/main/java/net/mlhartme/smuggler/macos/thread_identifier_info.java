package net.mlhartme.smuggler.macos;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class thread_identifier_info extends Struct {
    protected thread_identifier_info(Runtime runtime) {
        super(runtime);
    }
    u_int64_t thread_id = new u_int64_t();      /* system-wide unique 64-bit thread id */
    u_int64_t thread_handle = new u_int64_t();  /* handle to be used by libproc */
    u_int64_t dispatch_qaddr = new u_int64_t(); /* libdispatch queue address */

    public static thread_identifier_info of(jnr.ffi.Pointer pointer) {
        thread_identifier_info info = new thread_identifier_info(Runtime.getSystemRuntime());
        info.useMemory(pointer);
        return info;
    }
}
