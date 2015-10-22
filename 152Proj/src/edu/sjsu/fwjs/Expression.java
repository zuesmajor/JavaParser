package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2; 
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    public Value evaluate(Environment env) {
    	Value value1 = e1.evaluate(env);
        Value value2 = e2.evaluate(env);
        if (!(value1 instanceof IntVal && value2 instanceof IntVal))
        	throw new RuntimeException("Expected ints, but got " + value1 + " and " + value2);
        int one = ((IntVal) value1).toInt();
        int two = ((IntVal) value2).toInt();
        
        switch(op){
        case ADD:
        	return new IntVal(one + two);
        case SUBTRACT:
        	return new IntVal(one - two);
	    case MULTIPLY:
	        return new IntVal(one * two);
	    case DIVIDE:
	        return new IntVal(one / two);
	    case MOD:
	        return new IntVal(one % two);
	    case GT:
	        return new BoolVal(one > two);
	    case GE:
	        return new BoolVal(one >= two);
	    case LT:
	        return new BoolVal(one < two);
	    case LE:
	        return new BoolVal(one <= two);
	    case EQ:
	        return new BoolVal(one == two);
	    }
        // YOUR CODE HERE
        throw new RuntimeException("Unrecognized operator: " + op); 
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
    public Value evaluate(Environment env) {
    	Value value = this.cond.evaluate(env);
    	if(!(value instanceof BoolVal)){
    		throw new RuntimeException("Expected boolean, but got " + value);
    	}
    	BoolVal boolValue = (BoolVal) value;
    	if (boolValue.toBoolean()) {
            return this.thn.evaluate(env);
        } 
    	else if (this.els != null){
            return this.els.evaluate(env);
        } 
    	else {
            return null;
        }
        // YOUR CODE HERE
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	Value value = this.cond.evaluate(env);
        if (!(value instanceof BoolVal))
        	throw new RuntimeException("Expected boolean, but got " + value);
        boolean bool = ((BoolVal) value).toBoolean();
        if(bool){
            this.body.evaluate(env);
            return this.evaluate(env);
        }
        else{
            return null;
        }
        // YOUR CODE HERE
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
    	e1.evaluate(env);
        return e2.evaluate(env);    
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
    	Value v = exp.evaluate(env);
        env.createVar(varName, v);
        return v;    
    }  // YOUR CODE HERE
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
    	Value v = e.evaluate(env);
        env.updateVar(varName, v);
        return v;
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
    	ClosureVal c = new ClosureVal(params, body, env);
        return c;
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
    	ClosureVal c = (ClosureVal) (f.evaluate(env));
        List<Value> values = new ArrayList<Value>();
        for (int i = 0; i < args.size(); i++) {
           values.add(args.get(i).evaluate(env));
        }
        return c.apply(values, env);
    }
}

