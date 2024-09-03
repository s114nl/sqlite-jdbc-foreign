package org.sqlite.core;

import org.sqlite.*;
import org.sqlite.core.assertion.Asserts;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.sql.SQLException;

import static java.lang.foreign.Linker.nativeLinker;

public class ForeignDB extends DB {

    /**
     * C Pointer to the native sqlite3 struct.
     */
    private long sqlite3Handle = 0L;

    /**
     * Reference to the registered {@link BusyHandler}
     */
    private BusyHandler busyHandler;

    private MemorySegment sqlite3Handle() {
        return MemorySegment.ofAddress(sqlite3Handle);
    }

    public ForeignDB(String url, String fileName, SQLiteConfig config) throws SQLException {
        super(url, fileName, config);
    }

    @Override
    public void interrupt() throws SQLException {
        try {
            ForeignSqlite3.interrupt.invokeExact(sqlite3Handle());
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public void busy_timeout(int ms) throws SQLException {
        try {
            var resultCode = (int) ForeignSqlite3.busyTimeout.invokeExact(sqlite3Handle(), ms);
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public void busy_handler(BusyHandler busyHandler) throws SQLException {
        try {
            var callback = MethodHandles.lookup()
                    .bind(this, "nativeBusyHandlerCallback", MethodType.methodType(
                            int.class,           // return
                            MemorySegment.class, // context
                            int.class            // invocationCount
                    ));
            var callbackDescriptor = ForeignSqlite3.NativeBusyHandlerCallback.descriptor;
            var callbackHandle = nativeLinker()
                    .upcallStub(callback, callbackDescriptor, Arena.global());

            var resultCode = (int) ForeignSqlite3.busyHandler.invokeExact(
                    sqlite3Handle(),
                    callbackHandle,
                    MemorySegment.NULL
            );
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @SuppressWarnings("unused")
    int nativeBusyHandlerCallback(MemorySegment context, int invocationCount) {
        Asserts.notNull(busyHandler,
                "The busy-handle callback receiver cannot be null");
        return 0;
    }

    @Override
    String errmsg() throws SQLException {
        try {
            var result = (MemorySegment) ForeignSqlite3.errmsg.invokeExact(sqlite3Handle());
            return result
                    .reinterpret(Integer.MAX_VALUE)
                    .getString(0);
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public String libversion() throws SQLException {
        try {
            var version = (MemorySegment) ForeignSqlite3.libversion.invokeExact();
            return version
                    .reinterpret(Integer.MAX_VALUE)
                    .getString(0);
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public long changes() throws SQLException {
        try {
            return (int) ForeignSqlite3.changes.invokeExact(sqlite3Handle());
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public long total_changes() throws SQLException {
        try {
            return (int) ForeignSqlite3.totalChanges.invokeExact(sqlite3Handle());
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public int shared_cache(boolean enable) throws SQLException {
        return onoff(ForeignSqlite3.enableSharedCache, enable);
    }

    @Override
    public int enable_load_extension(boolean enable) throws SQLException {
        return onoff(ForeignSqlite3.enableLoadExtension, enable);
    }

    private int onoff(MethodHandle handle, boolean onoff) throws SQLException {
        try {
            var resultCode = (int) handle.invokeExact(
                    sqlite3Handle(), onoff ? 1 : 0
            );
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
            return resultCode;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
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
     *
     * @param filename  The database to open.
     * @param openFlags File opening configurations (<a
     *                  href="https://www.sqlite.org/c3ref/c_open_autoproxy.html">https://www.sqlite.org/c3ref/c_open_autoproxy.html</a>)
     * @throws SQLException
     */
    @Override
    protected void _open(String filename, int openFlags) throws SQLException {
        // opening a database is not re-entrant.
        Asserts.state(sqlite3Handle == 0L,
                "The database [{}] has already been opened.", filename);

        // Use try-with-resources to manage the lifetime of off-heap memory
        try (var arena = Arena.ofConfined()) {
            var nativeFilename = arena.allocateFrom(filename);
            var nativeDbHandle = arena.allocate(ValueLayout.ADDRESS);
            var resultCode = (int) ForeignSqlite3.openV2.invokeExact(
                    nativeFilename,
                    nativeDbHandle,
                    openFlags,
                    MemorySegment.NULL
            );
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }

            // store the pointer value to the sqlite3 db struct
            this.sqlite3Handle = nativeDbHandle.get(ValueLayout.JAVA_LONG, 0);

        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    protected void _close() throws SQLException {
        /*
        change_progress_handler(env, nativeDB, NULL, 0);
        change_busy_handler(env, nativeDB, NULL);
        clear_commit_listener(env, nativeDB, db);
        clear_update_listener(env, nativeDB);
         */

        try {
            var resultCode = (int) ForeignSqlite3.closeV2.invokeExact(sqlite3Handle());
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public int _exec(String sql) throws SQLException {
        // Use try-with-resources to manage the lifetime of off-heap memory
        try (var arena = Arena.ofConfined()) {
            var nativeSql = arena.allocateFrom(sql);
            return (int) ForeignSqlite3.exec.invokeExact(
                    sqlite3Handle(),
                    nativeSql,
                    MemorySegment.NULL,
                    MemorySegment.NULL,
                    MemorySegment.NULL
            );
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    protected SafeStmtPtr prepare(String sql) throws SQLException {
        // Use try-with-resources to manage the lifetime of off-heap memory
        try (var arena = Arena.ofConfined()) {
            var nativeSql = arena.allocateFrom(sql);
            var nativeStatementHandle = arena.allocate(ValueLayout.ADDRESS);
            var resultCode = (int) ForeignSqlite3.prepareV2.invokeExact(
                    sqlite3Handle(),
                    nativeSql,
                    (int) nativeSql.byteSize(),
                    nativeStatementHandle,
                    MemorySegment.NULL
            );
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }

            return new SafeStmtPtr(this, nativeStatementHandle.get(ValueLayout.JAVA_LONG, 0));
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    protected int finalize(long stmt) throws SQLException {
        try {
            var resultCode = (int) ForeignSqlite3.finalize.invokeExact(MemorySegment.ofAddress(stmt));
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
            return resultCode;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public int step(long stmt) throws SQLException {
        try {
            var resultCode = (int) ForeignSqlite3.step.invokeExact(MemorySegment.ofAddress(stmt));
            if (!(resultCode == SQLITE_ROW || resultCode == SQLITE_DONE)) {
                throwex(resultCode);
            }
            return resultCode;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public int reset(long stmt) throws SQLException {
        try {
            var resultCode = (int) ForeignSqlite3.reset.invokeExact(MemorySegment.ofAddress(stmt));
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
            return resultCode;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public int clear_bindings(long stmt) throws SQLException {
        try {
            var resultCode = (int) ForeignSqlite3.clearBindings.invokeExact(MemorySegment.ofAddress(stmt));
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    int bind_parameter_count(long stmt) throws SQLException {
        try {
            return (int) ForeignSqlite3.bindParameterCount.invokeExact(MemorySegment.ofAddress(stmt));
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public int column_count(long stmt) throws SQLException {
        return 0;
    }

    @Override
    public int column_type(long stmt, int col) throws SQLException {
        return 0;
    }

    @Override
    public String column_decltype(long stmt, int col) throws SQLException {
        return "";
    }

    @Override
    public String column_table_name(long stmt, int col) throws SQLException {
        return "";
    }

    @Override
    public String column_name(long stmt, int col) throws SQLException {
        return "";
    }

    @Override
    public String column_text(long stmt, int col) throws SQLException {
        return "";
    }

    @Override
    public byte[] column_blob(long stmt, int col) throws SQLException {
        return new byte[0];
    }

    @Override
    public double column_double(long stmt, int col) throws SQLException {
        return 0;
    }

    @Override
    public long column_long(long stmt, int col) throws SQLException {
        return 0;
    }

    @Override
    public int column_int(long stmt, int col) throws SQLException {
        return 0;
    }

    private int column(MethodHandle handle, long stmt, int col) throws SQLException {
        try {
            return (int) handle.invokeExact(MemorySegment.ofAddress(stmt));
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    int bind_null(long stmt, int pos) throws SQLException {
        return 0;
    }

    @Override
    int bind_int(long stmt, int pos, int v) throws SQLException {
        return 0;
    }

    @Override
    int bind_long(long stmt, int pos, long v) throws SQLException {
        return 0;
    }

    @Override
    int bind_double(long stmt, int pos, double v) throws SQLException {
        return 0;
    }

    @Override
    int bind_text(long stmt, int pos, String v) throws SQLException {
        return 0;
    }

    @Override
    int bind_blob(long stmt, int pos, byte[] v) throws SQLException {
        return 0;
    }

    @Override
    public void result_null(long context) throws SQLException {

    }

    @Override
    public void result_text(long context, String val) throws SQLException {

    }

    @Override
    public void result_blob(long context, byte[] val) throws SQLException {

    }

    @Override
    public void result_double(long context, double val) throws SQLException {

    }

    @Override
    public void result_long(long context, long val) throws SQLException {

    }

    @Override
    public void result_int(long context, int val) throws SQLException {

    }

    @Override
    public void result_error(long context, String err) throws SQLException {

    }

    @Override
    public String value_text(Function f, int arg) throws SQLException {
        return "";
    }

    @Override
    public byte[] value_blob(Function f, int arg) throws SQLException {
        return new byte[0];
    }

    @Override
    public double value_double(Function f, int arg) throws SQLException {
        return 0;
    }

    @Override
    public long value_long(Function f, int arg) throws SQLException {
        return 0;
    }

    @Override
    public int value_int(Function f, int arg) throws SQLException {
        return 0;
    }

    @Override
    public int value_type(Function f, int arg) throws SQLException {
        return 0;
    }

    @Override
    public int create_function(String name, Function f, int nArgs, int flags) throws SQLException {
        return 0;
    }

    @Override
    public int destroy_function(String name) throws SQLException {
        return 0;
    }

    @Override
    public int create_collation(String name, Collation c) throws SQLException {
        return 0;
    }

    @Override
    public int destroy_collation(String name) throws SQLException {
        return 0;
    }

    @Override
    public int backup(String dbName, String destFileName, ProgressObserver observer) throws SQLException {
        return 0;
    }

    @Override
    public int backup(String dbName, String destFileName, ProgressObserver observer, int sleepTimeMillis, int nTimeouts, int pagesPerStep) throws SQLException {
        return 0;
    }

    @Override
    public int restore(String dbName, String sourceFileName, ProgressObserver observer) throws SQLException {
        return 0;
    }

    @Override
    public int restore(String dbName, String sourceFileName, ProgressObserver observer, int sleepTimeMillis, int nTimeouts, int pagesPerStep) throws SQLException {
        return 0;
    }

    @Override
    public int limit(int id, int value) throws SQLException {
        return 0;
    }

    @Override
    public void register_progress_handler(int vmCalls, ProgressHandler progressHandler) throws SQLException {

    }

    @Override
    public void clear_progress_handler() throws SQLException {

    }

    @Override
    boolean[][] column_metadata(long stmt) throws SQLException {
        return new boolean[0][];
    }

    @Override
    void set_commit_listener(boolean enabled) {

    }

    @Override
    void set_update_listener(boolean enabled) {

    }

    @Override
    public byte[] serialize(String schema) throws SQLException {
        return new byte[0];
    }

    @Override
    public void deserialize(String schema, byte[] buff) throws SQLException {

    }
}
