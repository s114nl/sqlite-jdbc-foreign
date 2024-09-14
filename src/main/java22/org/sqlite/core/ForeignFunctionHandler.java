package org.sqlite.core;

import org.sqlite.Function;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodType;
import java.sql.SQLException;

public class ForeignFunctionHandler {
    public static final MethodType xFuncMethodType = xFuncMethodType();
    private final Function f;

    public ForeignFunctionHandler(Function f) {
        this.f = f;
    }

    /**
     * <pre>
     *     void (*xFunc)(sqlite3_context*,int,sqlite3_value**),
     * </pre>
     *
     * @param context     the sqlite3_context pointer
     * @param nArg        the number of arguments
     * @param valueHandle the argument values
     */
    @SuppressWarnings("unused")
    public void xFunc(MemorySegment context, int nArg, MemorySegment valueHandle) throws SQLException {
        // TODO handle exception
        f.xFunc();
    }

    /**
     * <pre>
     *     void (*xFunc)(sqlite3_context*,int,sqlite3_value**),
     * </pre>
     */
    private static MethodType xFuncMethodType() {

        return MethodType.methodType(
                void.class,             // return void
                MemorySegment.class,    // sqlite3_context*
                int.class,              // int
                MemorySegment.class     // sqlite3_value**
        );
    }
}
