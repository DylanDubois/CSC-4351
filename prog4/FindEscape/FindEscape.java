package FindEscape;

public class FindEscape {
  Symbol.Table escEnv = new Symbol.Table(); // escEnv maps Symbol to Escape
  Absyn.FunctionDec parentFun = null;

  public FindEscape(Absyn.Exp e) { traverseExp(0, e);  }

  //traverseVar
  void traverseVar(int depth, Absyn.Var v) {
    if(v instanceof Absyn.SimpleVar)
		  traverseVar (depth, (Absyn.SimpleVar)v);
	  else if(v instanceof Absyn.FieldVar)
		  traverseVar(depth, (Absyn.FieldVar)v);
	  else if(v instanceof Absyn.SubscriptVar)
		  traverseVar(depth, (Absyn.SubscriptVar)v);
	  else throw new Error("Error: traverseVar");
  }
  //FieldVar
  void traverseVar(int depth, Absyn.FieldVar v) {
     traverseVar(depth, v.var);
  }
  //SimpleVar
  void traverseVar(int depth, Absyn.SimpleVar v)
  {
	  Escape e = (Escape)escEnv.get(v.name);
	  if((e != null) && (depth > e.depth))
		  e.setEscape();
  }
  //SubscriptVar
  void traverseVar(int depth, Absyn.SubscriptVar v) {
    traverseVar(depth, v.var);
    traverseExp(depth, v.index);
  }
  
  //traverseExp
  void traverseExp(int depth, Absyn.Exp e) {
     if (e instanceof Absyn.ArrayExp)
      traverseExp(depth, (Absyn.ArrayExp)e);
    else if (e instanceof Absyn.AssignExp)
      traverseExp(depth, (Absyn.AssignExp)e);
    else if (e instanceof Absyn.CallExp)
      traverseExp(depth, (Absyn.CallExp)e);
    else if (e instanceof Absyn.ForExp)
      traverseExp(depth, (Absyn.ForExp)e);
    else if (e instanceof Absyn.IfExp)
      traverseExp(depth, (Absyn.IfExp)e);
    else if (e instanceof Absyn.LetExp)
      traverseExp(depth, (Absyn.LetExp)e);
    else if (e instanceof Absyn.OpExp)
      traverseExp(depth, (Absyn.OpExp)e);
    else if (e instanceof Absyn.RecordExp)
      traverseExp(depth, (Absyn.RecordExp)e);
    else if (e instanceof Absyn.SeqExp)
      traverseExp(depth, (Absyn.SeqExp)e);
    else if (e instanceof Absyn.VarExp)
      traverseExp(depth, (Absyn.VarExp)e);
    else if (e instanceof Absyn.WhileExp)
      traverseExp(depth, (Absyn.WhileExp)e);
  }
  //ArrayExp
  void traverseExp (int depth, Absyn.ArrayExp e){
	  traverseExp(depth, e.size);
	  traverseExp(depth, e.init);
  }
  //AssignExp
  void traverseExp(int depth, Absyn.AssignExp e){
	  traverseVar(depth, e.var);
	  traverseExp(depth, e.exp);
  }
  //CallExp
  void traverseExp(int depth, Absyn.CallExp e){
     if (parentFun != null) {
      parentFun.leaf = false;
    }
	  for (Absyn.ExpList arg=e.args; arg!= null; arg=arg.tail){
		  traverseExp(depth, arg.head);
    }
  }
  //ForExp
  void traverseExp(int depth, Absyn.ForExp e){
	  traverseExp(depth, e.var.init);
    traverseExp(depth, e.hi);
    escEnv.beginScope();
    escEnv.put(e.var.name, new VarEscape(depth, e.var));
    traverseExp(depth, e.body);
    escEnv.endScope();
  }
  //IfExp
  void traverseExp(int depth, Absyn.IfExp e){
	  traverseExp(depth, e.test);
	  traverseExp(depth, e.thenclause);
	  if (e.elseclause != null)
		  traverseExp(depth, e.elseclause);
  }
  //LetExp
  void traverseExp(int depth, Absyn.LetExp e){
	  escEnv.beginScope();
	  for(Absyn.DecList d = e.decs; d!= null; d=d.tail)
		  traverseDec(depth, d.head);
	  traverseExp(depth, e.body);
	  escEnv.endScope();
  }
  //OpExp
  void traverseExp(int depth, Absyn.OpExp e){
	  traverseExp(depth, e.left);
	  traverseExp(depth, e.right);
  }
  //RecordExp
  void traverseExp(int depth, Absyn.RecordExp e) { 
    for (Absyn.FieldExpList field = e.fields; field != null; field = field.tail) {
      traverseExp(depth, field.init);
    }
  }
  //SeqExp
  void traverseExp(int depth, Absyn.SeqExp e) {
    for (Absyn.ExpList expList = e.list; expList != null; expList = expList.tail){
      traverseExp(depth, expList.head);
    }
  }
  //VarExp
  void traverseExp(int depth, Absyn.VarExp e) {
    traverseVar(depth, e.var);
  }
  //WhileExp
  void traverseExp(int depth, Absyn.WhileExp e) {
    traverseExp(depth, e.test);
    traverseExp(depth, e.body);
  }
 
  //traverseDec
  void traverseDec(int depth, Absyn.Dec d) {
    if (d instanceof Absyn.VarDec)
		  traverseDec(depth, (Absyn.VarDec)d);
	  else if (d instanceof Absyn.FunctionDec)
		  traverseDec(depth, (Absyn.FunctionDec)d);
	  else if (!(d instanceof Absyn.TypeDec))
		  throw new Error("Error: traverseDec");
  }
  //VarDec
  void traverseDec(int depth, Absyn.VarDec d) {
    traverseExp(depth, d.init);
    escEnv.put(d.name, new VarEscape(depth, d));
  }
  //FunctionDec
  void traverseDec(int depth, Absyn.FunctionDec d) {
    Absyn.FunctionDec priorParentFun = parentFun;
    for (Absyn.FunctionDec fd = d; fd != null; fd = fd.next) {
      escEnv.beginScope();
      parentFun = fd;
      for (Absyn.FieldList param = fd.params; param != null; param = param.tail)
        escEnv.put(param.name, new FormalEscape(depth + 1, param));
      traverseExp(depth + 1, fd.body);
      escEnv.endScope();
    }
    parentFun = priorParentFun;
  }
}
