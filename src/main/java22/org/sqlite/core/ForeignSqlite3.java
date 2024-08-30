package org.sqlite.core;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class ForeignSqlite3 {

    private static final Linker linker;
    private static final SymbolLookup symbols;

    static {
        linker = Linker.nativeLinker();
        symbols = linker.defaultLookup();
    }

    static final MethodHandle bindParameterCount = _bindParameterCount();
    static final MethodHandle busyHandler = _busyHandler();
    static final MethodHandle busyTimeout = _busyTime();
    static final MethodHandle clearBindings = _clearBindings();
    static final MethodHandle closeV2 = _closeV2();
    static final MethodHandle exec = _exec();
    static final MethodHandle finalize = _finalize();
    static final MethodHandle interrupt = _interrupt();
    static final MethodHandle openV2 = _openV2();
    static final MethodHandle prepareV2 = _prepareV2();
    static final MethodHandle reset = _reset();
    static final MethodHandle step = _step();

    private static MethodHandle _clearBindings() {
        return null;
    }

    private static MethodHandle _bindParameterCount() {
        return null;
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/busy_handler.html">busy handler</a>
     *
     * <pre>
     *     int sqlite3_busy_handler(sqlite3*,int(*)(void*,int),void*);
     * </pre>
     */
    private static MethodHandle _busyHandler() {
        var addr = resolveSymbol("sqlite3_busy_handler");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS,  // int(*)(void*,int)
                ValueLayout.ADDRESS   // void*
        );
        return linker.downcallHandle(addr, descriptor);
    }

    static class NativeBusyHandlerCallback {

        /**
         * {@link FunctionDescriptor} declaration for `int(*)(void*,int)`
         */
        static final FunctionDescriptor descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS
        );
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/busy_timeout.html">busy timeout</a>
     * <pre>
     *     int sqlite3_busy_timeout(sqlite3*, int ms);
     * </pre>
     */
    private static MethodHandle _busyTime() {
        var addr = resolveSymbol("sqlite3_busy_timeout");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS,  // sqlite3*
                ValueLayout.JAVA_INT  // int ms
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/interrupt.html">interrupt</a>
     * <pre>
     *     void sqlite3_interrupt(sqlite3*);
     * </pre>
     */
    private static MethodHandle _interrupt() {
        var addr = resolveSymbol("sqlite3_interrupt");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS   // sqlite3*
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <pre>
     *  int sqlite3_open_v2(
     *      const char *filename,   \/* Database filename (UTF-8) *\/
     *      sqlite3 **ppDb,         \/* OUT: SQLite db handle *\/
     *      int flags,              \/* Flags *\/
     *      const char *zVfs        \/* Name of VFS module to use *\/
     *  );
     * </pre>
     */
    private static MethodHandle _openV2() {
        var addr = resolveSymbol("sqlite3_open_v2");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // result int
                ValueLayout.ADDRESS,    // const char *filename
                ValueLayout.ADDRESS,    // sqlite3 **ppDb
                ValueLayout.JAVA_INT,   // int flags
                ValueLayout.ADDRESS     // const char *zVfs
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <pre>
     *     int sqlite3_close_v2(sqlite3*);
     * </pre>
     */
    private static MethodHandle _closeV2() {
        var addr = resolveSymbol("sqlite3_close_v2");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS   // sqlite3*
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <pre>
     *  int sqlite3_exec(
     *   sqlite3*,                                  \/* An open database *\/
     *   const char *sql,                           \/* SQL to be evaluated *\/
     *   int (*callback)(void*,int,char**,char**),  \/* Callback function *\/
     *   void *,                                    \/* 1st argument to callback *\/
     *   char **errmsg                              \/* Error msg written here *\/
     * );
     * </pre>
     */
    private static MethodHandle _exec() {
        var addr = resolveSymbol("sqlite3_exec");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS,   // sqlite3*,
                ValueLayout.ADDRESS,   // const char *sql,
                ValueLayout.ADDRESS,   // void *
                ValueLayout.ADDRESS    // char **errmsg
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/prepare.html">prepare</a>
     *
     * <pre>
     *  int sqlite3_prepare_v2(
     *   sqlite3 *db,            \/* Database handle *\/
     *   const char *zSql,       \/* SQL statement, UTF-8 encoded *\/
     *   int nByte,              \/* Maximum length of zSql in bytes. *\/
     *   sqlite3_stmt **ppStmt,  \/* OUT: Statement handle *\/
     *   const char **pzTail     \/* OUT: Pointer to unused portion of zSql *\/
     * );
     * </pre>
     * <p>
     * Todo:
     * If the nByte argument is negative, then zSql is read up to the first zero terminator.
     * If nByte is positive, then it is the number of bytes read from zSql.
     * If nByte is zero, then no prepared statement is generated.
     * If the caller knows that the supplied string is nul-terminated,
     * then there is a small performance advantage to passing an nByte parameter that is the
     * number of bytes in the input string including the nul-terminator.
     */
    private static MethodHandle _prepareV2() {
        var addr = resolveSymbol("sqlite3_prepare_v2");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS,   // sqlite3 *db,
                ValueLayout.ADDRESS,   // const char *zSql,
                ValueLayout.JAVA_INT,  // int nByte,
                ValueLayout.ADDRESS,   // sqlite3_stmt **ppStmt,
                ValueLayout.ADDRESS    // const char **pzTail
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/finalize.html">finalize</a>
     *
     * <pre>
     *  int sqlite3_finalize(sqlite3_stmt *pStmt);
     * </pre>
     */
    private static MethodHandle _finalize() {
        var addr = resolveSymbol("sqlite3_finalize");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS    // sqlite3_stmt *pStmt
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/step.html">step</a>
     *
     * <pre>
     *  int sqlite3_finalize(sqlite3_stmt *pStmt);
     * </pre>
     */
    private static MethodHandle _step() {
        var addr = resolveSymbol("sqlite3_step");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS,   // sqlite3 *db,
                ValueLayout.ADDRESS    // sqlite3_stmt *pStmt
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/reset.html">reset</a>
     *
     * <pre>
     *     int sqlite3_reset(sqlite3_stmt *pStmt);
     * </pre>
     **/
    private static MethodHandle _reset() {
        var addr = resolveSymbol("sqlite3_reset");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS    // sqlite3_stmt *pStmt
        );
        return linker.downcallHandle(addr, descriptor);
    }


    private static MemorySegment resolveSymbol(String name) {
        return symbols.find(name)
                .orElseThrow(() -> new IllegalArgumentException("Unable to resolve symbol"));
    }
}
