package Translate;
import Symbol.Symbol;
import Tree.BINOP;
import Tree.CJUMP;
import Temp.Temp;
import Temp.Label;

public class Translate {
  public Frame.Frame frame;
  public Translate(Frame.Frame f) {
    frame = f;
  }
  private Frag frags;
  public void procEntryExit(Level level, Exp body) {
    Frame.Frame myframe = level.frame;
    Tree.Exp bodyExp = body.unEx();
    Tree.Stm bodyStm;
    if (bodyExp != null)
      bodyStm = MOVE(TEMP(myframe.RV()), bodyExp);
    else
      bodyStm = body.unNx();
    ProcFrag frag = new ProcFrag(myframe.procEntryExit1(bodyStm), myframe);
    frag.next = frags;
    frags = frag;
  }
  public Frag getResult() {
    return frags;
  }

  private static Tree.Exp CONST(int value) {
    return new Tree.CONST(value);
  }
  private static Tree.Exp NAME(Label label) {
    return new Tree.NAME(label);
  }
  private static Tree.Exp TEMP(Temp temp) {
    return new Tree.TEMP(temp);
  }
  private static Tree.Exp BINOP(int binop, Tree.Exp left, Tree.Exp right) {
    return new Tree.BINOP(binop, left, right);
  }
  private static Tree.Exp MEM(Tree.Exp exp) {
    return new Tree.MEM(exp);
  }
  private static Tree.Exp CALL(Tree.Exp func, Tree.ExpList args) {
    return new Tree.CALL(func, args);
  }
  private static Tree.Exp ESEQ(Tree.Stm stm, Tree.Exp exp) {
    if (stm == null)
      return exp;
    return new Tree.ESEQ(stm, exp);
  }

  private static Tree.Stm MOVE(Tree.Exp dst, Tree.Exp src) {
    return new Tree.MOVE(dst, src);
  }
  private static Tree.Stm EXP(Tree.Exp exp) {
    return new Tree.EXP(exp);
  }
  private static Tree.Stm JUMP(Label target) {
    return new Tree.JUMP(target);
  }
  private static
  Tree.Stm CJUMP(int relop, Tree.Exp l, Tree.Exp r, Label t, Label f) {
    return new Tree.CJUMP(relop, l, r, t, f);
  }
  private static Tree.Stm SEQ(Tree.Stm left, Tree.Stm right) {
    if (left == null)
      return right;
    if (right == null)
      return left;
    return new Tree.SEQ(left, right);
  }
  private static Tree.Stm LABEL(Label label) {
    return new Tree.LABEL(label);
  }

  private static Tree.ExpList ExpList(Tree.Exp head, Tree.ExpList tail) {
    return new Tree.ExpList(head, tail);
  }
  private static Tree.ExpList ExpList(Tree.Exp head) {
    return ExpList(head, null);
  }
  private static Tree.ExpList ExpList(ExpList exp) {
    if (exp == null)
      return null;
    return ExpList(exp.head.unEx(), ExpList(exp.tail));
  }

  public Exp Error() {
    return new Ex(CONST(0));
  }

  public Exp SimpleVar(Access access, Level level) {
    Tree.Exp framepointer = TEMP(level.frame.FP());
    for (Level lev = level; lev != access.home; lev = lev.parent) {
        framepointer = lev.frame.formals.head.exp(framepointer);
    }
    return new Ex(access.acc.exp(framepointer));
  }

  public Exp FieldVar(Exp record, int index) {
    index = index * frame.wordSize();
    Temp t = new Temp();
    Tree.Stm aStm = MOVE(TEMP(t), record.unEx());
    Tree.Exp aExp = MEM(BINOP(Tree.BINOP.PLUS, TEMP(t), CONST(index)));
    return new Ex(ESEQ(aStm, aExp));
  }

  public Exp SubscriptVar(Exp array, Exp index) {
    Tree.Exp size = BINOP(Tree.BINOP.MUL, index.unEx(),  CONST(frame.wordSize()));
    Temp t = new Temp();
    Tree.Stm aStm = MOVE(TEMP(t), array.unEx());
    Tree.Exp aExp = MEM(BINOP(Tree.BINOP.PLUS, TEMP(t), size));
    return new Ex(ESEQ(aStm, aExp));
  }

  public Exp NilExp() {
    return new Ex(CONST(0));
  }

  public Exp IntExp(int value) {
    return new Ex(CONST(value));
  }

  private java.util.Hashtable strings = new java.util.Hashtable();
  public Exp StringExp(String lit) {
    String u = lit.intern();
    Label lab = (Label)strings.get(u);
    if (lab == null) {
      lab = new Label();
      strings.put(u, lab);
      DataFrag frag = new DataFrag(frame.string(lab, u));
      frag.next = frags;
      frags = frag;
    }
    return new Ex(NAME(lab));
  }

  private Tree.Exp CallExp(Symbol f, ExpList args, Level from) {
    return frame.externalCall(f.toString(), ExpList(args));
  }
  private Tree.Exp CallExp(Level f, ExpList args, Level from) {
    Tree.Exp framepointer = TEMP(from.frame.FP());
    if (f.parent != from) {
      for (Level l = from; l != f.parent; l = l.parent) {
        framepointer = l.frame.formals.head.exp(framepointer);
      }
    }
    return CALL(NAME(f.frame.name), ExpList(framepointer, ExpList(args)));
  }

  public Exp FunExp(Symbol f, ExpList args, Level from) {
    return new Ex(CallExp(f, args, from));
  }
  public Exp FunExp(Level f, ExpList args, Level from) {
    return new Ex(CallExp(f, args, from));
  }
  public Exp ProcExp(Symbol f, ExpList args, Level from) {
    return new Nx(EXP(CallExp(f, args, from)));
  }
  public Exp ProcExp(Level f, ExpList args, Level from) {
    return new Nx(EXP(CallExp(f, args, from)));
  }

  public Exp OpExp(int op, Exp left, Exp right) {
    return new Ex(BINOP(op, left.unEx(), right.unEx()));
  }

  public Exp StrOpExp(int op, Exp left, Exp right) {
    return new RelCx(op, left.unEx(), right.unEx());
  }

  public Exp RecordExp(ExpList init) {
    int size = 0;
    for (ExpList e = init; e != null; e = e.tail) {
      size++;
    }
    Temp t = new Temp();
    return new Ex(
      ESEQ(SEQ(MOVE(TEMP(t), this.frame.externalCall("allocRecord", 
      ExpList(CONST(size)))), 
      startRecord(t, 0, init, this.frame.wordSize())), 
      TEMP(t)));
  }
  
  private Tree.Stm startRecord(Temp t, int i, ExpList init, int size)
  {
    if (init == null) {
      return null;
    }
    return SEQ(MOVE(MEM(BINOP(0, TEMP(t), CONST(i))), init.head.unEx()), startRecord(t, i + size, init.tail, size));
  }

  public Exp SeqExp(ExpList e) {
    if (e == null) {
      return new Nx(null);
    }
    Tree.Stm stm = null;
    for (; e.tail != null; e = e.tail) {
      stm = SEQ(stm, e.head.unNx());
    }
    Tree.Exp exp = e.head.unEx();
    if (exp == null) {
      return new Nx(SEQ(stm, e.head.unNx()));
    }
    return new Ex(ESEQ(stm, exp));
  }

  public Exp AssignExp(Exp lhs, Exp rhs) {
    return new Nx(MOVE(lhs.unEx(), rhs.unEx()));
  }

  public Exp IfExp(Exp cc, Exp aa, Exp bb) {
    return new IfThenElseExp(cc, aa, bb);
  }

  public Exp WhileExp(Exp test, Exp body, Label done) {
    Label t1 = new Label();
    Label t2 = new Label();
    Tree.Stm Stm1 = SEQ(LABEL(t1), test.unCx(t2, done));
    Tree.Stm Stm2 = SEQ(LABEL(t2), body.unNx());
    Tree.Stm l = SEQ(Stm1, SEQ(Stm2, JUMP(t1)));
    return new Nx(SEQ(l, LABEL(done)));
  }

  public Exp ForExp(Access i, Exp lo, Exp hi, Exp body, Label done) {
    Label t = new Label();
    Label inc = new Label();
    Temp c = new Temp();
    Temp home = i.home.frame.FP();
    return new Nx(SEQ(SEQ(SEQ(SEQ(MOVE(i.acc.exp(TEMP(home)), lo.unEx()), 
           MOVE(TEMP(c), hi.unEx())), CJUMP(4, i.acc.exp(TEMP(home)), 
           TEMP(c), t, done)), SEQ(SEQ(SEQ(LABEL(t), body.unNx()), 
           CJUMP(2, i.acc.exp(TEMP(home)), TEMP(c), inc, done)), 
           SEQ(SEQ(LABEL(inc), MOVE(i.acc.exp(TEMP(home)), 
           BINOP(0, i.acc.exp(TEMP(home)), CONST(1)))), JUMP(t)))), LABEL(done)));
  }

  public Exp BreakExp(Label done) {
    return new Nx(JUMP(done));
  }

  public Exp LetExp(ExpList lets, Exp body) {
    Tree.Stm stm = null;
    for (ExpList el = lets; el != null; el = el.tail) {
      stm = SEQ(stm, el.head.unNx());
    }
    Tree.Exp exp = body.unEx();
    if (exp == null) {
      return new Nx(SEQ(stm, body.unNx()));
    }
    return new Ex(ESEQ(stm, result));
  }

  public Exp ArrayExp(Exp size, Exp init) {
    return new Ex(this.frame.externalCall("initArray", ExpList(size.unEx(), ExpList(init.unEx()))));
  }

  public Exp VarDec(Access a, Exp init) {
    return new Nx(MOVE(a.acc.exp(TEMP(a.home.frame.FP())), init.unEx()));
  }

  public Exp TypeDec() {
    return new Nx(null);
  }

  public Exp FunctionDec() {
    return new Nx(null);
  }
}
