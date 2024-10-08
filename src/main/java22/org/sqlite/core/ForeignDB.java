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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.sqlite.core.ForeignFunctionHandler.xFuncMethodType;
import static org.sqlite.core.ForeignSqlite3.SQLITE_TRANSIENT;

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
        return ref(sqlite3Handle);
    }

    private static MemorySegment ptr(Long address) {
        return MemorySegment.ofAddress(address);
    }

    public ForeignDB(String url, String fileName, SQLiteConfig config) throws SQLException {
        super(url, fileName, config);
    }

    private static MemorySegment ref(long ptr) {
        return MemorySegment.ofAddress(ptr);
    }

    private Object invokeExact(MethodHandle handle, Object... args) throws SQLException {
        try {
            return handle.invokeWithArguments(args);
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Convenience method to invoke a {@link MethodHandle} with the signature `int -> args ...`
     *
     * @param handle the {@link MethodHandle} to invoke
     * @param args   the arguments to the <code>handle</code>
     * @return the return value as int
     */
    private int invokeIntExact(MethodHandle handle, Object... args) throws SQLException {
        try {
            return (int) handle.invokeWithArguments(args);
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Convenience method to invoke a {@link MethodHandle} with the signature `int -> args ...` with
     * result code check.
     * <p/>
     * This implementation throws a {@link SQLException} if the result code of the method call
     * is not {@link Codes#SQLITE_OK}.
     *
     * @param handle the {@link MethodHandle} to invoke
     * @param args   the arguments to the <code>handle</code>
     * @return the return value as int
     */
    private int invokeIntExactChecked(MethodHandle handle, Object... args) throws SQLException {
        try {
            var resultCode = (int) handle.invokeWithArguments(args);
            if (resultCode != SQLITE_OK) {
                throwex(resultCode);
            }
            return resultCode;
        } catch (Throwable t) {
            throw new SQLException("Failed to invoke method " + handle, t);
        }
    }

    /**
     * Convenience method to invoke a {@link MethodHandle} with the signature `const char* -> args ...`.
     *
     * @param handle the {@link MethodHandle} to invoke
     * @param args   the arguments to the <code>handle</code>
     * @return the return value as String
     */
    private String invokeStringExact(MethodHandle handle, Object... args) throws SQLException {
        try {
            var result = (MemorySegment) handle.invokeWithArguments(args);
            return result
                    .reinterpret(Integer.MAX_VALUE)
                    .getString(0);
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    private void invokeVoidExact(MethodHandle handle, Object... args) throws SQLException {
        try {
            handle.invokeWithArguments(args);
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public void interrupt() throws SQLException {
        invokeVoidExact(ForeignSqlite3.interrupt, sqlite3Handle());
    }

    @Override
    public void busy_timeout(int ms) throws SQLException {
        invokeIntExactChecked(ForeignSqlite3.busyTimeout, sqlite3Handle(), ms);
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
        return invokeStringExact(ForeignSqlite3.errmsg, sqlite3Handle());
    }

    @Override
    public String libversion() throws SQLException {
        return invokeStringExact(ForeignSqlite3.libversion);
    }

    @Override
    public long changes() throws SQLException {
        return invokeIntExact(ForeignSqlite3.changes, sqlite3Handle());
    }

    @Override
    public long total_changes() throws SQLException {
        return invokeIntExact(ForeignSqlite3.totalChanges, sqlite3Handle());
    }

    @Override
    public int shared_cache(boolean enable) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.enableSharedCache, enable ? 1 : 0);
    }

    @Override
    public int enable_load_extension(boolean enable) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.enableLoadExtension, sqlite3Handle(), enable ? 1 : 0);
    }

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

        invokeIntExactChecked(ForeignSqlite3.closeV2, sqlite3Handle());
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
            invokeIntExactChecked(ForeignSqlite3.prepareV2,
                    sqlite3Handle(),
                    nativeSql,
                    (int) nativeSql.byteSize(),
                    nativeStatementHandle,
                    MemorySegment.NULL
            );
            return new SafeStmtPtr(this, nativeStatementHandle.get(ValueLayout.JAVA_LONG, 0));
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    public int step(long stmt) throws SQLException {
        try {
            var resultCode = invokeIntExact(ForeignSqlite3.step, ptr(stmt));
            if (!(resultCode == SQLITE_ROW || resultCode == SQLITE_DONE)) {
                throwex(resultCode);
            }
            return resultCode;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    int bind_parameter_count(long stmt) throws SQLException {
        return invokeIntExact(ForeignSqlite3.bindParameterCount, ptr(stmt));
    }

    @Override
    public int clear_bindings(long stmt) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.clearBindings, ptr(stmt));
    }

    @Override
    public int reset(long stmt) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.reset, ptr(stmt));
    }

    @Override
    protected int finalize(long stmt) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.finalize, ptr(stmt));
    }

    @Override
    public int column_count(long stmt) throws SQLException {
        return invokeIntExact(ForeignSqlite3.columnCount, ptr(stmt));
    }

    @Override
    public int column_type(long stmt, int col) throws SQLException {
        return invokeIntExact(ForeignSqlite3.columnType, ptr(stmt), col);
    }

    @Override
    public String column_decltype(long stmt, int col) throws SQLException {
        return invokeStringExact(ForeignSqlite3.columnDecltype, ptr(stmt), col);
    }

    @Override
    public String column_table_name(long stmt, int col) throws SQLException {
        return invokeStringExact(ForeignSqlite3.columnTableName, ptr(stmt), col);
    }

    @Override
    public String column_name(long stmt, int col) throws SQLException {
        return invokeStringExact(ForeignSqlite3.columnName, ptr(stmt), col);
    }

    @Override
    public String column_text(long stmt, int col) throws SQLException {
        try {
            /*
            The safest policy is to invoke these routines in one of the following ways:
              - sqlite3_column_text() followed by sqlite3_column_bytes()
              - sqlite3_column_blob() followed by sqlite3_column_bytes()
             */
            var result = (MemorySegment) invokeExact(ForeignSqlite3.columnText, ptr(stmt), col);

            if (result.equals(MemorySegment.NULL)) {
                return null;
            } else {
                return result
                        .reinterpret(getBytes(stmt, col) + 1) // include the \0
                        .getString(0);
            }

        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    @Override
    public byte[] column_blob(long stmt, int col) throws SQLException {
        try {
            /*
            The safest policy is to invoke these routines in one of the following ways:
              - sqlite3_column_text() followed by sqlite3_column_bytes()
              - sqlite3_column_blob() followed by sqlite3_column_bytes()
             */
            var result = (MemorySegment) invokeExact(ForeignSqlite3.columnBlob, ptr(stmt), col);

            if (result.equals(MemorySegment.NULL)) {
                return null;
            } else {
                return result
                        .reinterpret(getBytes(stmt, col))
                        .asSlice(0)
                        .toArray(ValueLayout.JAVA_BYTE);
            }

        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    private int getBytes(long stmt, int col) throws SQLException {
        return invokeIntExact(ForeignSqlite3.columnBytes, ptr(stmt), col);
    }

    @Override
    public double column_double(long stmt, int col) throws SQLException {
        return (double) invokeExact(ForeignSqlite3.columnDouble, ptr(stmt), col);
    }

    @Override
    public long column_long(long stmt, int col) throws SQLException {
        return (long) invokeExact(ForeignSqlite3.columnInt64, ptr(stmt), col);
    }

    @Override
    public int column_int(long stmt, int col) throws SQLException {
        return (int) invokeExact(ForeignSqlite3.columnInt, ptr(stmt), col);
    }

    @Override
    int bind_null(long stmt, int pos) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.bindNull, ptr(stmt), pos);
    }

    @Override
    int bind_int(long stmt, int pos, int v) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.bindInt, ptr(stmt), pos, v);
    }

    @Override
    int bind_long(long stmt, int pos, long v) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.bindLong, ptr(stmt), pos, v);
    }

    @Override
    int bind_double(long stmt, int pos, double v) throws SQLException {
        return invokeIntExactChecked(ForeignSqlite3.bindDouble, ptr(stmt), pos, v);
    }

    @Override
    int bind_text(long stmt, int pos, String v) throws SQLException {
        // Use try-with-resources to manage the lifetime of off-heap memory
        try (var arena = Arena.ofConfined()) {
            var nativeText = v != null ? arena.allocateFrom(v) : MemorySegment.NULL;
            var nativeTextSize = (int) nativeText.byteSize(); // TODO validate size

            return invokeIntExactChecked(ForeignSqlite3.bindText,
                    ptr(stmt),
                    pos,
                    nativeText,
                    nativeTextSize,
                    SQLITE_TRANSIENT
            );
        }
    }

    @Override
    int bind_blob(long stmt, int pos, byte[] v) throws SQLException {
        // Use try-with-resources to manage the lifetime of off-heap memory
        try (var arena = Arena.ofConfined()) {
            var nativeByteArray = arena.allocate(v.length);
            nativeByteArray.asByteBuffer().put(v);

            // int sqlite3_bind_blob(sqlite3_stmt*, int, const void*, int n, void(*)(void*));
            return invokeIntExactChecked(ForeignSqlite3.bindBlob,
                    ptr(stmt),
                    pos,
                    nativeByteArray,
                    (int) nativeByteArray.byteSize(),
                    SQLITE_TRANSIENT
            );
        }
    }

    @Override
    public void result_null(long context) throws SQLException {
        invokeVoidExact(ForeignSqlite3.resultNull, ptr(context));
    }

    @Override
    public void result_text(long context, String val) throws SQLException {
        try (var arena = Arena.ofConfined()) {
            var nativeText = val != null ? arena.allocateFrom(val) : MemorySegment.NULL;
            var nativeTextSize = (int) nativeText.byteSize(); // TODO check size against limit

            invokeVoidExact(ForeignSqlite3.resultText,
                    ptr(context),
                    nativeText,
                    nativeTextSize,
                    SQLITE_TRANSIENT
            );
        }
    }

    @Override
    public void result_blob(long context, byte[] val) throws SQLException {
        try (var arena = Arena.ofConfined()) {
            var nativeByteArray = arena.allocate(val.length);
            nativeByteArray.asByteBuffer().put(val);

            invokeVoidExact(ForeignSqlite3.resultBlob,
                    ptr(context),
                    nativeByteArray,
                    (int) nativeByteArray.byteSize(),
                    SQLITE_TRANSIENT
            );
        }
    }

    @Override
    public void result_double(long context, double val) throws SQLException {
        invokeVoidExact(ForeignSqlite3.resultDouble, ptr(context), val);
    }

    @Override
    public void result_long(long context, long val) throws SQLException {
        invokeVoidExact(ForeignSqlite3.resultInt64, ptr(context), val);
    }

    @Override
    public void result_int(long context, int val) throws SQLException {
        invokeVoidExact(ForeignSqlite3.resultInt, ptr(context), val);
    }

    @Override
    public void result_error(long context, String err) throws SQLException {
        try (var arena = Arena.ofConfined()) {
            var nativeText = err != null ? arena.allocateFrom(err) : MemorySegment.NULL;
            var nativeTextSize = (int) nativeText.byteSize();

            // void sqlite3_result_error(sqlite3_context*, const char*, int);
            invokeVoidExact(ForeignSqlite3.resultError, ptr(context), nativeText, nativeTextSize);
        }
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
        Asserts.state(name.getBytes(UTF_8).length <= 255,
                "The length of the name is limited to 255 bytes in a UTF-8 representation, exclusive of the zero-terminator");

        var isAggregateFunction = f instanceof Function.Aggregate;
        var isWindowingFunction = f instanceof Function.Window;

        // TODO: What is the life-time of this receiver?
        var xFuncHandler = new ForeignFunctionHandler(f);

        // Use try-with-resources to manage the lifetime of off-heap memory
        try (var arena = Arena.ofConfined()) {
            var nativeName = arena.allocateFrom(name);

            // TODO check exceptions
            var xFunc = MethodHandles.lookup()
                    .findVirtual(ForeignFunctionHandler.class, "xFunc", xFuncMethodType);
            var xFuncBinding = xFunc.bindTo(xFuncHandler);
            var xFuncDescriptor = ForeignSqlite3.CreateFunctionV2NativeCallbacks.xFuncDescriptor;
            var xFuncHandle = nativeLinker()
                    .upcallStub(xFuncBinding, xFuncDescriptor, Arena.global());

            return invokeIntExactChecked(ForeignSqlite3.createFunctionV2,
                    sqlite3Handle(),        // sqlite3 *db
                    nativeName,             // const char *zFunctionName
                    nArgs,                  // int nArg
                    flags,                  // int eTextRep (flags)
                    MemorySegment.NULL,     // void *pApp (user-data)
                    xFuncHandle,            // void (*xFunc)(sqlite3_context*,int,sqlite3_value**)
                    MemorySegment.NULL,     // (*xStep)(sqlite3_context*,int,sqlite3_value**)
                    MemorySegment.NULL,     // void (*xFinal)(sqlite3_context*)
                    MemorySegment.NULL      // void(*xDestroy)(void*)
            );

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new SQLException(e);
        }
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
        return invokeIntExact(ForeignSqlite3.limit, id, value);
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
