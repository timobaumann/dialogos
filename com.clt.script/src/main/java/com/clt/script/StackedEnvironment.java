package com.clt.script;

import java.io.IOException;
import java.io.Reader;

import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Variable;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class StackedEnvironment implements Environment {

    Environment primary;
    Environment fallback;

    public StackedEnvironment(Environment primary, Environment fallback) {

        this.primary = primary;
        this.fallback = fallback;
    }

    public Type getType(String typeName) {

        try {
            return this.primary.getType(typeName);
        } catch (TypeException exn) {
            return this.fallback.getType(typeName);
        }
    }

    public Variable createVariableReference(String id) {

        try {
            return this.primary.createVariableReference(id);
        } catch (TypeException exn) {
            return this.fallback.createVariableReference(id);
        }
    }

    public Expression createFunctionCall(String name, Expression[] arguments) {

        try {
            return this.primary.createFunctionCall(name, arguments);
        } catch (TypeException exn) {
            return this.fallback.createFunctionCall(name, arguments);
        }
    }

    public Reader include(String name)
            throws IOException {

        try {
            return this.primary.include(name);
        } catch (IOException exn) {
            return this.fallback.include(name);
        }
    }
}
