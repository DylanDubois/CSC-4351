package Semant;

import Absyn.ExpList;
import Absyn.SeqExp;
import Translate.Exp;
import Symbol.Table;
import Types.Type;
import java.util.Hashtable;

public class Semant
{
  Env env;
  public Semant(ErrorMsg.ErrorMsg err)
  {
    this(new Env(err));
  }
  
  Semant(Env e)
  {
    this.env = e;
  }
  
  public void transProg(Absyn.Exp exp)
  {
    transExp(exp);
  }
  
  private void error(int pos, String msg)
  {
    env.errorMsg.error(pos, msg);
  }
	
  private void transArgs(int epos, Types.RECORD formal, Absyn.ExpList args)
  {
    if (formal == null) {
      if (args != null){
	error(args.head.pos, "too many arguments");}
    }
    if (args == null) {
      error(epos, "missing argument for " + formal.fieldName);
    }
    ExpTy e = transExp(args.head);
    if (!e.ty.coerceTo(formal.fieldType))
      error(args.head.pos, "argument type mismatch");
    	//return new ExpList(e.exp, transArgs(epos, formal.tail, args.tail));
  }
  
  static final Types.VOID VOID = new VOID();
  static final Types.INT INT = new INT();
  static final Types.STRING STRING = new STRING();
  static final Types.NIL NIL = new NIL();
  
  private Exp checkInt(ExpTy et, int pos)
  {
    if (!INT.coerceTo(et.ty)) {
      error(pos, "integer required");
    }
    return et.exp;
  }
  
  private Exp checkComparable(ExpTy et, int pos)
  {
    Type a = et.ty.actual();
    if (!(a instanceof INT
        || a instanceof STRING 
        || a instanceof NIL
        || a instanceof RECORD
        || a instanceof ARRAY)) {
      error(pos, "integer, string, nil, record or array required");
    }
    return et.exp;
  }
  
  private Exp checkOrderable(ExpTy et, int pos)
  {
    Type a = et.ty.actual();
    if (!(a instanceof INT 
        || a instanceof STRING)) {
      error(pos, "Integer or String required.");
    }
    return et.exp;
  }
  
  
  //TransExp
  ExpTy transExp(Absyn.Exp e)
  {
    ExpTy result;

    if (e == null)
      return new ExpTy(null, VOID);
    else if (e instanceof OpExp)
      result = transExp((OpExp)e);
    else if (e instanceof LetExp)
      result = transExp((LetExp)e);
    else if (e instanceof VarExp)
      result = transExp((VarExp)e);
    else if (e instanceof NilExp)
      result = transExp((NilExp)e);
    else if (e instanceof IntExp)
      result = transExp((IntExp)e);
    else if (e instanceof StringExp)
      result = transExp((StringExp)e);
    else if (e instanceof CallExp)
      result = transExp((CallExp)e);
    else if (e instanceof RecordExp)
      result = transExp((RecordExp)e);
    else if (e instanceof SeqExp)
      result = transExp((SeqExp)e);
    else if (e instanceof AssignExp)
      result = transExp((AssignExp)e);
    else if (e instanceof IfExp)
      result = transExp((IfExp)e);
    else if (e instanceof WhileExp)
      result = transExp((WhileExp)e);
    else if (e instanceof ForExp)
      result = transExp((ForExp)e);
    else if (e instanceof BreakExp)
      result = transExp((BreakExp)e);
    else if (e instanceof ArrayExp)
      result = transExp((ArrayExp)e);
    else throw new Error("Semant.transExp");
    e.type = result.ty;
    return result;
  }
  
 ExpTy transExp(Absyn.OpExp e) {
    ExpTy left = transExp(e.left);
    ExpTy right = transExp(e.right);

    switch (e.oper) {
    case Absyn.OpExp.PLUS:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.MINUS:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.MUL:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.DIV:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.EQ:
      checkComparable(left, e.left.pos);
      checkComparable(right, e.right.pos);
      if(STRING.coerceTo(left.ty) && 
         STRING.coerceTo(right.ty))
    	  return new ExpTy(null, INT);
      else if (!left.ty.coerceTo(right.ty) && 
               !right.ty.coerceTo(left.ty))
    	  error(e.pos, "Operands not valid");
      return new ExpTy(null, INT);
    case Absyn.OpExp.NE:
    	  checkComparable(left, e.left.pos);
          checkComparable(right, e.right.pos);
          if(STRING.coerceTo(left.ty) && 
             STRING.coerceTo(right.ty))
        	  return new ExpTy(null, INT);
          else if (!left.ty.coerceTo(right.ty) && 
                   !right.ty.coerceTo(left.ty))
        	  error(e.pos, "Operands not valid");
          return new ExpTy(null, INT);
    case Absyn.OpExp.LT:
      checkOrderable(left, e.left.pos);
      checkOrderable(right, e.right.pos);
      if(STRING.coerceTo(left.ty) && 
         STRING.coerceTo(right.ty))
    	  return new ExpTy(null, INT);
      else if(!left.ty.coerceTo(right.ty) && 
              !right.ty.coerceTo(left.ty))
    	  error(e.pos, "Operands not valid");
      return new ExpTy(null, INT);
    case Absyn.OpExp.LE:
    	checkOrderable(left, e.left.pos);
        checkOrderable(right, e.right.pos);
        if(STRING.coerceTo(left.ty) && 
           STRING.coerceTo(right.ty))
      	  return new ExpTy(null, INT);
        else if(!left.ty.coerceTo(right.ty) && 
                !right.ty.coerceTo(left.ty))
      	  error(e.pos, "Operands not valid");
        return new ExpTy(null, INT);
    case Absyn.OpExp.GT:
    	checkOrderable(left, e.left.pos);
        checkOrderable(right, e.right.pos);
        if(STRING.coerceTo(left.ty) && 
           STRING.coerceTo(right.ty))
      	  return new ExpTy(null, INT);
        else if(!left.ty.coerceTo(right.ty) && 
                !right.ty.coerceTo(left.ty))
      	  error(e.pos, "Operands not valid");
        return new ExpTy(null, INT);
    case Absyn.OpExp.GE:
    	checkOrderable(left, e.left.pos);
        checkOrderable(right, e.right.pos);
        if(STRING.coerceTo(left.ty) && 
           STRING.coerceTo(right.ty))
      	  return new ExpTy(null, INT);
        else if(!left.ty.coerceTo(right.ty) && 
                !right.ty.coerceTo(left.ty))
      	  error(e.pos, "Operands not valid");
        return new ExpTy(null, INT); 
    default:
      throw new Error("Unknown operator");
    }
  }
  
  ExpTy transExp(Absyn.LetExp e) {
    env.venv.beginScope();
    env.tenv.beginScope();
    for (Absyn.DecList d = e.decs; d != null; d = d.tail) {
      transDec(d.head);
    }
    ExpTy body = transExp(e.body);
    env.venv.endScope();
    env.tenv.endScope();
    return new ExpTy(null, body.ty);
  }

  ExpTy transExp(Absyn.VarExp e) {
	return transVar(e.var);
  } 
  
  ExpTy transExp(Absyn.NilExp e) {
	return new ExpTy(null, NIL);
  }
 
  ExpTy transExp(Absyn.IntExp e) {
        return new ExpTy(null, INT);
  }

  ExpTy transExp(Absyn.StringExp e) {
	return new ExpTy(null, STRING);
  }
  
  ExpTy transExp(Absyn.CallExp e) {
	  Entry x = (Entry)env.venv.get(e.func);
	  if (x instanceof FunEntry) 
    {
		  FunEntry f = (FunEntry)x;
		  transArgs(e.pos, f.formals, e.args);
		  return new ExpTy(null, f.result);
	  }
	  error(e.pos, "Undeclared function" + e.func);
	  return new ExpTy(null, VOID);
  }	
  
  ExpTy transExp(Absyn.RecordExp e) {
   Types.NAME name = (Types.NAME)env.tenv.get(e.typ);
   if(name != null) {
	   Type actual = name.actual();
	   if( actual instanceof Types.RECORD)
     {
		   Types.RECORD r = (Types.RECORD)actual;
		   transFields(e.pos, r, e.fields);
		   return new ExpTy(null, name);
	   }
	   error(e.pos, "Record type required");
   }else
	   error(e.pos, "Undeclared type" + e.typ);
   return new ExpTy(null, VOID);
  }

  ExpTy transExp(Absyn.SeqExp e) {
	  Type type = VOID;
	  for (Absyn.ExpList exp = e.list; exp != null; exp = exp.tail)
    {
		  ExpTy et = transExp(exp.head);
		  type = et.ty;
	  }
	  return new ExpTy(null, type);
  }
  
  ExpTy transExp(Absyn.AssignExp e) {
	  ExpTy var = transVar(e.var);
	  ExpTy exp = transExp(e.exp);
	  if (exp.ty.coerceTo(var.ty))
	    error(e.pos, "Assignement type mismatch");
	  return new ExpTy(null, VOID);
  }

  ExpTy transExp(Absyn.IfExp e) {
	  ExpTy test = transExp(e.test);
	  checkInt(test, e.test.pos);
	  ExpTy thenclause = transExp(e.thenclause);
	  ExpTy elseclause = transExp(e.elseclause);
	  if(!thenclause.ty.coerceTo(elseclause.ty) && !elseclause.ty.coerceTo(thenclause.ty))
		  error (e.pos, "Result type mismatch");
	  return new ExpTy(null, elseclause.ty);
  }
  
  ExpTy transExp(Absyn.WhileExp e){
	  ExpTy t = transExp(e.test);
	  checkInt(t, e.test.pos);
	  Semant loop = new LoopSemant(env);
	  ExpTy body = loop.transExp(e.body);
	  if(!body.ty.coerceTo(VOID))
		  error(e.body.pos, "Result type mismatch");
	  return new ExpTy(null, VOID);
  }
  
  ExpTy transExp(Absyn.ForExp e){
	   ExpTy ex = transExp(e.var.init);
	   checkInt(ex, e.var.pos);
	   ExpTy hi = transExp(e.hi);
	   checkInt(hi, e.hi.pos);
	   e.var.entry = new LoopVarEntry(INT);
	   env.venv.put(e.var.name, e.var.entry);
	   Semant loop = new LoopSemant(env);
	   ExpTy body = loop.transExp(e.body);
	   env.venv.endScope();
	   if (!body.ty.coerceTo(VOID))
		   error(e.body.pos, "Result type mismatch");
	   return new ExpTy(null, VOID);
  }
 
  ExpTy transExp(Absyn.BreakExp e){
	  error(e.pos, "Break outside the loop");
	  return new ExpTy(null, VOID);
  }
  
  ExpTy transExp(Absyn.ArrayExp e){
	  Types.NAME name = (Types.NAME)env.tenv.get(e.typ);
	  ExpTy size = transExp(e.size);
	  ExpTy init = transExp(e.init);
	  checkInt(size, e.size.pos);
	  if (name != null) {
		  Type actual = name.actual();
		  if( actual instanceof Types.ARRAY){
			  Types.ARRAY array = (Types.ARRAY)actual;
			  if(!init.ty.coerceTo(array.element))
				  error(e.init.pos, "Element type mismatch");
			  return new ExpTy(null, name);
			  
		  } else
			  error(e.pos, "Array type is required");
	  }	else
		  error(e.pos, "Type is not declared");
	  return new ExpTy(null,  VOID);
  }

  
  //TransDec
  Exp transDec(Abysn.Dec d) {
    if (d instanceof Absyn.VarDec) {
      return transDec((Absyn.VarDec)d);
    }
    if (d instanceof Absyn.TypeDec) {
      return transDec((Absyn.TypeDec)d);
    }
    if (d instanceof Absyn.FunctionDec) {
      return transDec((Absyn.FunctionDec)d);
    }
    throw new Error("Semant.transDec");
  }
  
  Exp transDec(Absyn.VarDec d) {
    ExpTy i = transExp(d.i);
    Type type;
    if (d.typ == null)
    {
      if (i.ty.coerceTo(NIL)) {
        error(d.pos, "Record type required");
      }
      type = i.ty;
    } else {
      type = transTy(d.typ);
      if (!i.ty.coerceTo(type)) {
        error(d.pos, "Assignment type mismatch");
      }
    }
    d.entry = new VarEntry(type);
    env.venv.put(d.name, d.entry);
    return null;
  }
  
  Exp transDec(Absyn.TypeDec d) {
    Hashtable hash = new Hashtable();
    for (Absyn.TypeDec type = d; type != null; type = type.next)
    {
      if (hash.put(type.name, type.name) != null) {
        error(type.pos, "Type redeclared");
      }
      type.entry = new Types.NAME(type.name);
      env.tenv.put(type.name, type.entry);
    }
    for (Absyn.TypeDec type = d; type != null; type = type.next)
    {
      Types.NAME name = type.entry;
      name.bind(transTy(type.ty));
    }
    for (TypeDec type = d; type != null; type = type.next)
    {
      Types.NAME name = type.entry;
      if (name.isLoop()) {
        error(type.pos, "Illegal type cycle");
      }
    }
    return null;
  }
  
  Exp transDec(Absyn.FunctionDec d) {
    Hashtable hash = new Hashtable();
    for (Absyn.FunctionDec f = d; f != null; f = f.next)
    {
      if (hash.put(f.name, f.name) != null) {
        error(f.pos, "Function redeclared");
      }
      Types.RECORD fields = transTypeFields(new Hashtable(), f.params);
      Type type = transTy(f.result);
      f.entry = new FunEntry(fields, type);
      this.env.venv.put(f.name, f.entry);
    }
    
    for (Absyn.FunctionDec f = d; f != null; f = f.next)
    {
      env.venv.beginScope();
      putTypeFields(f.entry.formals);
      Semant fun = new Semant(env);
      ExpTy body = fun.transExp(f.body);
      if (!body.ty.coerceTo(f.entry.result)) {
        error(f.body.pos, "Result type mismatch");
      }
      env.venv.endScope();
    }
    return null;
  }
  
  private Types.RECORD transTypeFields(Hashtable hash, Absyn.FieldList f)
  {
    if (f == null) {
      return null;
    }
    Types.NAME name = (Types.NAME)env.tenv.get(f.typ);
    if (name == null) {
      error(f.pos, "Undeclared type: " + f.typ);
    }
    if (hash.put(f.name, f.name) != null) {
      error(f.pos, "Function redeclared" + f.name);
    }
    return new Types.RECORD(f.name, name, transTypeFields(hash, f.tail));
  }
  
  private void putTypeFields(Types.RECORD f)
  {
    if (f == null) {
      return;
    }
    env.venv.put(f.fieldName, new VarEntry(f.fieldType));
    putTypeFields(f.tail);
  }
  
  //TransTy
  Type transTy(Absyn.Ty t)
  {
    if (t instanceof NameTy) {
      return transTy((Absyn.NameTy)t);
    }
    if (t instanceof RecordTy) {
      return transTy((Absyn.RecordTy)t);
    }
    if (t instanceof ArrayTy) {
      return transTy((Absyn.ArrayTy)t);
    }
    throw new Error("Semant.transTy");
  }
  
  Type transTy(Absyn.NameTy t)
  {
    if (t == null) {
      return VOID;
    }
    Types.NAME name = (Types.NAME)env.tenv.get(t.name);
    if (name != null) {
      return name;
    }
    error(t.pos, "Undeclared type: " + t.name);
    return VOID;
  }
  
  Type transTy(Absyn.RecordTy t)
  {
    Types.RECORD type = transTypeFields(new Hashtable(), t.fields);
    if (type != null) {
      return type;
    }
    return VOID;
  }
  
  Type transTy(Absyn.ArrayTy t)
  {
    Types.NAME name = (Types.NAME)env.tenv.get(t.typ);
    if (name != null) {
      return new Types.ARRAY(name);
    }
    error(t.pos, "Undeclared type: " + t.typ);
    return VOID;
  }


//TransVar
ExpTy transVar(Absyn.SimpleVar e) {
    Entry x = (Entry)env.venv.get(e.name);
	if (x instanceof VarEntry) {
	    VarEntry v = (VarEntry)x;
	    return new ExpTy(null, v.ty);
	}
	else {
	   error(e.pos, "Undefined variable");
	   return new ExpTy(null, INT);
	}
}

ExpTy transVar(Absyn.Var v){
	  if (v instanceof Absyn.SimpleVar)
		  return transVar((Absyn.SimpleVar)v);
	  if (v instanceof Absyn.FieldVar)
		  return transVar((Absyn.FieldVar)v);
	  if (v instanceof Absyn.SubscriptVar)
		  return transVar((Absyn.SubscriptVar)v);
	  throw new Error("Semant.transVar");
  }

ExpTy transVar(Absyn.FieldVar v){
	  ExpTy var = transVar(v.var);
	  Type actual = var.ty.actual();
	  if (actual instanceof Types.RECORD){
		  int count = 0;
		  for(Types.RECORD field = (Types.RECORD)actual; field != null; field = field.tail){
			  if (field.fieldName == v.field)
				  return new ExpTy(null, field.fieldType);
		  ++count;
		  }
		  error(v.pos, "Undeclared field: " + v.field);
	  } else
		  error(v.var.pos, "Record required");
	  return new ExpTy(null, VOID);
  }
  
ExpTy transVar(Absyn.SubscriptVar v){
	  ExpTy var = transVar(v.var);
	  ExpTy index = transExp(v.index);
	  checkInt(index, v.index.pos);
	  Type actual = var.ty.actual();
	  if (actual instanceof Types.ARRAY) {
		  Types.ARRAY array = (Types.ARRAY)actual;
		  return new ExpTy(null, array.element);
	  }
	  error(v.var.pos, "Array required");
	  return new ExpTy(null, VOID);
  }

//TransTypeFields
private Types.RECORD transTypeFields(Hashtable hash, Absyn.FieldList f){
	  if (f == null)
		  return null;
	  Types.NAME name = (Types.NAME)env.tenv.get(f.typ);
	  if (name == null)
		  error(f.pos, "Undeclared type: " + f.typ);
	  if(hash.put(f.name, f.name) != null)
		  error(f.pos, "Function redeclared" + f.name);
	  return new Types.RECORD(f.name, name, transTypeFields(hash, f.tail));
  }
 
  private void transFields(int epos, Types.RECORD f, Absyn.FieldExpList exp)
  {
	    if (f == null) {
	      if (exp != null)
	        error(exp.pos, "Too many expressions");
	    }
	    if (exp == null) {
	      error(epos, "Missing expression for " + f.fieldName);
	    }
	    ExpTy e = transExp(exp.init);
	    if (exp.name != f.fieldName) {
	      error(exp.pos, "Field name mismatch");
	    }
	    if (!e.ty.coerceTo(f.fieldType)) {
	      error(exp.pos, "Field type mismatch");
	    }
   }
}


class LoopSemant extends Semant {
  LoopSemant(Env e)
  {
    super(e);
  }
}
