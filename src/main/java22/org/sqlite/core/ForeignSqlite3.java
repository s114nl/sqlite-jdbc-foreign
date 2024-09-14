package org.sqlite.core;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class ForeignSqlite3 {

    private static final Linker linker;
    private static final SymbolLookup symbols;

    static {
        linker = Linker.nativeLinker();
        symbols = lookupSqlite3Library();
    }

    private static SymbolLookup lookupSqlite3Library() {
        var library = new File("/Users/epdittmer/Downloads/sqlite-amalgamation-3460100", "out");
        if (!library.exists()) {
            throw new IllegalArgumentException(library.getAbsolutePath());
        }
        return SymbolLookup.libraryLookup(library.getAbsolutePath(), Arena.global());
    }

    // consts
    static final MemorySegment SQLITE_TRANSIENT = MemorySegment.ofAddress(-1);

    // consts limits
    static final int SQLITE_LIMIT_LENGTH = 0;
    static final int SQLITE_LIMIT_SQL_LENGTH = 1;
    static final int SQLITE_LIMIT_COLUMN = 2;
    static final int SQLITE_LIMIT_EXPR_DEPTH = 3;
    static final int SQLITE_LIMIT_COMPOUND_SELECT = 4;
    static final int SQLITE_LIMIT_VDBE_OP = 5;
    static final int SQLITE_LIMIT_FUNCTION_ARG = 6;
    static final int SQLITE_LIMIT_ATTACHED = 7;
    static final int SQLITE_LIMIT_LIKE_PATTERN_LENGTH = 8;
    static final int SQLITE_LIMIT_VARIABLE_NUMBER = 9;
    static final int SQLITE_LIMIT_TRIGGER_DEPTH = 10;
    static final int SQLITE_LIMIT_WORKER_THREADS = 11;

    // functions
    static final MethodHandle bindBlob = _bindBlob();
    static final MethodHandle bindDouble = _bindDouble();
    static final MethodHandle bindInt = _bindInt();
    static final MethodHandle bindLong = _bindLong();
    static final MethodHandle bindNull = _bindNull();
    static final MethodHandle bindParameterCount = _bindParameterCount();
    static final MethodHandle bindText = _bindText();
    static final MethodHandle busyHandler = _busyHandler();
    static final MethodHandle busyTimeout = _busyTimeOut();
    static final MethodHandle changes = _changes();
    static final MethodHandle clearBindings = _clearBindings();
    static final MethodHandle closeV2 = _closeV2();
    static final MethodHandle columnBlob = _columnBlob();
    static final MethodHandle columnBytes = _columnBytes();
    static final MethodHandle columnCount = _columnCount();
    static final MethodHandle columnDecltype = _columnDecltype();
    static final MethodHandle columnDouble = _columnDouble();
    static final MethodHandle columnInt = _columnInt();
    static final MethodHandle columnInt64 = _columnInt64();
    static final MethodHandle columnName = _columName();
    static final MethodHandle columnTableName = _columnTableName();
    static final MethodHandle columnText = _columnText();
    static final MethodHandle columnType = _columnType();
    static final MethodHandle createFunctionV2 = _createFunctionV2();
    static final MethodHandle enableLoadExtension = _enableLoadExtension();
    static final MethodHandle enableSharedCache = _enableSharedCache();
    static final MethodHandle errmsg = _errmsg();
    static final MethodHandle exec = _exec();
    static final MethodHandle finalize = _finalize();
    static final MethodHandle interrupt = _interrupt();
    static final MethodHandle libversion = _libversion();
    static final MethodHandle openV2 = _openV2();
    static final MethodHandle prepareV2 = _prepareV2();
    static final MethodHandle reset = _reset();
    static final MethodHandle step = _step();
    static final MethodHandle totalChanges = _totalChanges();
    static final MethodHandle limit = _limit();
    static final MethodHandle resultNull = _resultNull();
    static final MethodHandle resultText = _resultText();
    static final MethodHandle resultBlob = _resultBlob();
    static final MethodHandle resultDouble = _resultDouble();
    static final MethodHandle resultInt64 = _resultInt64();
    static final MethodHandle resultInt = _resultInt();
    static final MethodHandle resultError = _resultError();

    private static MethodHandle _resultInt64() {
        return null;
    }

    private static MethodHandle _resultInt() {
        return null;
    }

    private static MethodHandle _resultDouble() {
        return null;
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/result_blob.html">result error</a>
     * <pre>
     *     void sqlite3_result_error(sqlite3_context*, const char*, int);
     * </pre>
     */
    private static MethodHandle _resultError() {
        var addr = resolveSymbol("sqlite3_result_error");
        var descriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,   // sqlite3_context *
                ValueLayout.ADDRESS,   // const char *
                ValueLayout.JAVA_INT   // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/result_blob.html">result blob</a>
     * <pre>
     *     void sqlite3_result_blob(sqlite3_context*, const void*, int, void(*)(void*));
     * </pre>
     */
    private static MethodHandle _resultBlob() {
        var addr = resolveSymbol("sqlite3_result_blob");
        var descriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,   // sqlite3_context *
                ValueLayout.ADDRESS,   // const void *
                ValueLayout.JAVA_INT,  // int
                ValueLayout.ADDRESS    // void(*)(void*)
        );
        return linker.downcallHandle(addr, descriptor);
    }


    /**
     * <a href="https://www.sqlite.org/c3ref/result_blob.html">result text</a>
     * <pre>
     *     void sqlite3_result_text(sqlite3_context*, const char*, int, void(*)(void*));
     * </pre>
     */
    private static MethodHandle _resultText() {
        var addr = resolveSymbol("sqlite3_result_text");
        var descriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,   // sqlite3_context *
                ValueLayout.ADDRESS,   // const char *
                ValueLayout.JAVA_INT,  // int
                ValueLayout.ADDRESS    // void(*)(void*)
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/result_blob.html">result NULL</a>
     * <pre>
     *     void sqlite3_result_null(sqlite3_context*);
     * </pre>
     */
    private static MethodHandle _resultNull() {
        var addr = resolveSymbol("sqlite3_result_null");
        var descriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS    // sqlite3_context *
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_blob.html">bind blob</a>
     *
     * <pre>
     *      int sqlite3_bind_blob(sqlite3_stmt*, int, const void*, int n, void(*)(void*));
     * </pre>
     */
    private static MethodHandle _bindBlob() {
        var addr = resolveSymbol("sqlite3_bind_blob");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return int
                ValueLayout.ADDRESS,    // sqlite3_stmt *
                ValueLayout.JAVA_INT,   // int
                ValueLayout.ADDRESS,    // const void*
                ValueLayout.JAVA_INT,   // int
                ValueLayout.ADDRESS     // void(*)(void*)
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_blob.html">bind text</a>
     *
     * <pre>
     *      int sqlite3_bind_text(sqlite3_stmt*,int,const char*,int,void(*)(void*));
     * </pre>
     */
    private static MethodHandle _bindText() {
        var addr = resolveSymbol("sqlite3_bind_text");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return int
                ValueLayout.ADDRESS,    // sqlite3_stmt *
                ValueLayout.JAVA_INT,   // int
                ValueLayout.ADDRESS,    // const char*
                ValueLayout.JAVA_INT,   // int
                ValueLayout.ADDRESS     // void(*)(void*) - SQLITE_TRANSIENT
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_blob.html">bind double</a>
     *
     * <pre>
     *      int sqlite3_bind_double(sqlite3_stmt*, int, double);
     * </pre>
     */
    private static MethodHandle _bindDouble() {
        var addr = resolveSymbol("sqlite3_bind_double");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return int
                ValueLayout.ADDRESS,    // sqlite3_stmt *
                ValueLayout.JAVA_INT,   // int
                ValueLayout.JAVA_DOUBLE // double
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_blob.html">bind int64</a>
     *
     * <pre>
     *      int sqlite3_bind_int64(sqlite3_stmt*, int, sqlite3_int64);
     * </pre>
     */
    private static MethodHandle _bindLong() {
        var addr = resolveSymbol("sqlite3_bind_int64");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return int
                ValueLayout.ADDRESS,   // sqlite3_stmt *
                ValueLayout.JAVA_INT,  // int
                ValueLayout.JAVA_LONG  // long
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_blob.html">bind int</a>
     *
     * <pre>
     *      int sqlite3_bind_int(sqlite3_stmt*, int, int);
     * </pre>
     */
    private static MethodHandle _bindInt() {
        var addr = resolveSymbol("sqlite3_bind_int");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return int
                ValueLayout.ADDRESS,   // sqlite3_stmt *
                ValueLayout.JAVA_INT,  // int
                ValueLayout.JAVA_INT   // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_blob.html">bind null</a>
     *
     * <pre>
     *     int sqlite3_bind_null(sqlite3_stmt*, int);
     * </pre>
     */
    private static MethodHandle _bindNull() {
        var addr = resolveSymbol("sqlite3_bind_null");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return int
                ValueLayout.ADDRESS,   // sqlite3_stmt *
                ValueLayout.JAVA_INT   // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/create_function.html">create function</a>
     *
     * <pre>
     * int sqlite3_create_function_v2(
     *   sqlite3 *db,
     *   const char *zFunctionName,
     *   int nArg,
     *   int eTextRep,
     *   void *pApp,
     *   void (*xFunc)(sqlite3_context*,int,sqlite3_value**),
     *   void (*xStep)(sqlite3_context*,int,sqlite3_value**),
     *   void (*xFinal)(sqlite3_context*),
     *   void(*xDestroy)(void*)
     * );
     * </pre>
     */
    private static MethodHandle _createFunctionV2() {
        var addr = resolveSymbol("sqlite3_create_function_v2");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return int
                ValueLayout.ADDRESS,   // sqlite3 *
                ValueLayout.ADDRESS,   // const char *zFunctionName
                ValueLayout.JAVA_INT,  // int
                ValueLayout.JAVA_INT,  // int,
                ValueLayout.ADDRESS,   // void *pApp (user-data)
                ValueLayout.ADDRESS,   // void (*xFunc)(sqlite3_context*,int,sqlite3_value**)
                ValueLayout.ADDRESS,   // void (*xStep)(sqlite3_context*,int,sqlite3_value**)
                ValueLayout.ADDRESS,   // void (*xFinal)(sqlite3_context*),
                ValueLayout.ADDRESS    // void(*xDestroy)(void*)
        );
        return linker.downcallHandle(addr, descriptor);
    }

    static class CreateFunctionV2NativeCallbacks {

        /**
         * {@link FunctionDescriptor} declaration for `void (*xFunc)(sqlite3_context*,int,sqlite3_value**)`
         */
        static final FunctionDescriptor xFuncDescriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS
        );
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column bytes</a>
     *
     * <pre>
     *     int sqlite3_column_bytes(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnBytes() {
        var addr = resolveSymbol("sqlite3_column_bytes");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return int
                ValueLayout.ADDRESS,   // sqlite3_stmt *
                ValueLayout.JAVA_INT   // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column int64</a>
     *
     * <pre>
     *     sqlite3_int64 sqlite3_column_int64(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnInt64() {
        var addr = resolveSymbol("sqlite3_column_int64");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_LONG,  // return double
                ValueLayout.ADDRESS,    // sqlite3_stmt *
                ValueLayout.JAVA_INT    // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column double</a>
     *
     * <pre>
     *     int sqlite3_column_int(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnInt() {
        var addr = resolveSymbol("sqlite3_column_int");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return double
                ValueLayout.ADDRESS,   // sqlite3_stmt *
                ValueLayout.JAVA_INT   // int
        );
        return linker.downcallHandle(addr, descriptor);
    }


    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column double</a>
     *
     * <pre>
     *     double sqlite3_column_double(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnDouble() {
        var addr = resolveSymbol("sqlite3_column_double");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_DOUBLE,  // return double
                ValueLayout.ADDRESS,      // sqlite3_stmt *
                ValueLayout.JAVA_INT      // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column text</a>
     *
     * <pre>
     *     const unsigned char *sqlite3_column_text(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnText() {
        var addr = resolveSymbol("sqlite3_column_text");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS,  // const unsigned char *
                ValueLayout.ADDRESS,  // sqlite3_stmt *
                ValueLayout.JAVA_INT  // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column blob</a>
     *
     * <pre>
     *     const void *sqlite3_column_blob(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnBlob() {
        var addr = resolveSymbol("sqlite3_column_blob");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS,  // result const void *
                ValueLayout.ADDRESS,  // sqlite3_stmt *
                ValueLayout.JAVA_INT  // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_decltype.html">column decltype</a>
     *
     * <pre>
     *     const char *sqlite3_column_decltype(sqlite3_stmt*,int);
     * </pre>
     */
    private static MethodHandle _columnDecltype() {
        var addr = resolveSymbol("sqlite3_column_decltype");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS,  // result const char *
                ValueLayout.ADDRESS,  // sqlite3_stmt *pStmt
                ValueLayout.JAVA_INT  // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_blob.html">column blob</a>
     * <pre>
     *     int sqlite3_column_type(sqlite3_stmt*, int iCol);
     * </pre>
     */
    private static MethodHandle _columnType() {
        var addr = resolveSymbol("sqlite3_column_type");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS,  // sqlite3_stmt *pStmt
                ValueLayout.JAVA_INT  // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_database_name.html">column database name</a>
     * <pre>
     *     const char *sqlite3_column_table_name(sqlite3_stmt*,int);
     * </pre>
     */
    private static MethodHandle _columnTableName() {
        var addr = resolveSymbol("sqlite3_column_table_name");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS, // result const char *
                ValueLayout.ADDRESS,  // sqlite3_stmt *pStmt
                ValueLayout.JAVA_INT  // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_name.html">column name</a>
     *
     * <pre>
     *     const char *sqlite3_column_name(sqlite3_stmt*, int N)
     * </pre>
     */
    private static MethodHandle _columName() {
        var addr = resolveSymbol("sqlite3_column_name");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS, // result const char *
                ValueLayout.ADDRESS,  // sqlite3_stmt *pStmt
                ValueLayout.JAVA_INT  // int
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/column_count.html">column count</a>
     * <pre>
     *     int sqlite3_column_count(sqlite3_stmt *pStmt);
     * </pre>
     */
    private static MethodHandle _columnCount() {
        var addr = resolveSymbol("sqlite3_column_count");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS   // sqlite3_stmt *pStmt
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/enable_load_extension.html">enable load extension</a>
     * <pre>
     *     int sqlite3_enable_load_extension(sqlite3 *db, int onoff);
     * </pre>
     */
    private static MethodHandle _enableLoadExtension() {
        var addr = resolveSymbol("sqlite3_enable_load_extension");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // result int
                ValueLayout.ADDRESS,    // sqlite3 *db
                ValueLayout.JAVA_INT    // int (1 = on, 0 = off)
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/enable_shared_cache.html">enable_shared_cache</a>
     * <pre>
     *  int sqlite3_enable_shared_cache(int);
     * </pre>
     */
    private static MethodHandle _enableSharedCache() {
        var addr = resolveSymbol("sqlite3_enable_shared_cache");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.JAVA_INT    // int (1 = on, 0 = off)
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://sqlite.org/c3ref/total_changes.html">total changes</a>
     *
     * <pre>
     *     int sqlite3_total_changes(sqlite3*);
     * </pre>
     */
    private static MethodHandle _totalChanges() {
        var addr = resolveSymbol("sqlite3_total_changes");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS    // sqlite3*
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://sqlite.org/c3ref/changes.html">changes</a>
     * <pre>
     *     int sqlite3_changes(sqlite3*);
     * </pre>
     */
    private static MethodHandle _changes() {
        var addr = resolveSymbol("sqlite3_changes");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // result int
                ValueLayout.ADDRESS    // sqlite3*
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/libversion.html">libversion</a>
     *
     * <pre>
     *     const char *sqlite3_libversion(void);
     * </pre>
     */
    private static MethodHandle _libversion() {
        var addr = resolveSymbol("sqlite3_libversion");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS // const char *
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/errcode.html">errcode</a>
     *
     * <pre>
     *     const char *sqlite3_errmsg(sqlite3*);
     * </pre>
     */
    private static MethodHandle _errmsg() {
        var addr = resolveSymbol("sqlite3_errmsg");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS, // const char *
                ValueLayout.ADDRESS  // sqlite3*
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/clear_bindings.html">clear_bindings</a>
     * <pre>
     *     int sqlite3_clear_bindings(sqlite3_stmt*);
     * </pre>
     */
    private static MethodHandle _clearBindings() {
        var addr = resolveSymbol("sqlite3_clear_bindings");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS   // sqlite3_stmt *pStmt
        );
        return linker.downcallHandle(addr, descriptor);
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/bind_parameter_count.html">bind_parameter_count</a>
     *
     * <pre>
     *     int sqlite3_bind_parameter_count(sqlite3_stmt*);
     * </pre>
     */
    private static MethodHandle _bindParameterCount() {
        var addr = resolveSymbol("sqlite3_bind_parameter_count");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // result int
                ValueLayout.ADDRESS   // sqlite3_stmt *pStmt
        );
        return linker.downcallHandle(addr, descriptor);
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
                ValueLayout.ADDRESS,  // sqlite3*
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
                ValueLayout.JAVA_INT
        );
    }

    /**
     * <a href="https://www.sqlite.org/c3ref/busy_timeout.html">busy timeout</a>
     * <pre>
     *     int sqlite3_busy_timeout(sqlite3*, int ms);
     * </pre>
     */
    private static MethodHandle _busyTimeOut() {
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
        var descriptor = FunctionDescriptor.ofVoid(
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
                ValueLayout.ADDRESS,   // int (*callback)(void*,int,char**,char**)
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

    /**
     * <a href="https://www.sqlite.org/draft/c3ref/limit.html">limit</a>
     *
     * <pre>
     *     int sqlite3_limit(sqlite3*, int id, int newVal);
     * </pre>
     **/
    private static MethodHandle _limit() {
        var addr = resolveSymbol("sqlite3_limit");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,               // result int
                ValueLayout.ADDRESS,    // sqlite3 *
                ValueLayout.JAVA_INT,               // int id
                ValueLayout.JAVA_INT                // int newVal
        );
        return linker.downcallHandle(addr, descriptor);
    }

    private static MemorySegment resolveSymbol(String name) {
        return symbols.find(name)
                .orElseThrow(() -> new IllegalArgumentException("Unable to resolve symbol"));
    }
}
