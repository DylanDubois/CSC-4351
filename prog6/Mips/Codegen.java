package Mips;
import Temp.Temp;
import Temp.TempList;
import Temp.Label;
import Temp.LabelList;
import java.util.Hashtable;

public class Codegen {
  MipsFrame frame;
  public Codegen(MipsFrame f) {frame = f;}

  private Assem.InstrList ilist = null, last = null;

  private void emit(Assem.Instr inst) {
    if (last != null)
      last = last.tail = new Assem.InstrList(inst, null);
    else {
      if (ilist != null)
	throw new Error("Codegen.emit");
      last = ilist = new Assem.InstrList(inst, null);
    }
  }

  Assem.InstrList codegen(Tree.Stm s) {
    munchStm(s);
    Assem.InstrList l = ilist;
    ilist = last = null;
    return l;
  }

  static Assem.Instr OPER(String a, TempList d, TempList s, LabelList j) {
    return new Assem.OPER("\t" + a, d, s, j);
  }
  static Assem.Instr OPER(String a, TempList d, TempList s) {
    return new Assem.OPER("\t" + a, d, s);
  }
  static Assem.Instr MOVE(String a, Temp d, Temp s) {
    return new Assem.MOVE("\t" + a, d, s);
  }

  static TempList L(Temp h) {
    return new TempList(h, null);
  }
  static TempList L(Temp h, TempList t) {
    return new TempList(h, t);
  }

  void munchStm(Tree.Stm s) {
    if (s instanceof Tree.MOVE) 
      munchStm((Tree.MOVE)s);
    else if (s instanceof Tree.EXP)
      munchStm((Tree.EXP)s);
    else if (s instanceof Tree.JUMP)
      munchStm((Tree.JUMP)s);
    else if (s instanceof Tree.CJUMP)
      munchStm((Tree.CJUMP)s);
    else if (s instanceof Tree.LABEL)
      munchStm((Tree.LABEL)s);
    else
      throw new Error("Codegen.munchStm");
  }
	
//Tree.Move
  void munchStm(Tree.MOVE s) {
		//Exp src = munchExp(s.src);
    //Exp dst = munchExp(s.dst);
		Temp t1 = munchExp(s.src);
		Temp t2 = munchExp(s.dst);
		
		if (dst instanceof TEMP) {
		emit(MOVE("move `d0,`s0", t2, t1));
		}
		//else {}
  }

//Tree.EXP
  void munchStm(Tree.EXP s) {
    munchExp(s.exp);
  }

//Tree.JUMP	
  void munchStm(Tree.JUMP s) {
		LabelList list = s.list;
		if (e instanceof NAME) {
			emit(new assem.OPER("j " + list.head.toString(), null, null, list));
		}
  }

//Tree.CJUMP	
  private static String[] CJUMP = new String[10];
  static {
    CJUMP[Tree.CJUMP.EQ ] = "beq";
    CJUMP[Tree.CJUMP.NE ] = "bne";
    CJUMP[Tree.CJUMP.LT ] = "blt";
    CJUMP[Tree.CJUMP.GT ] = "bgt";
    CJUMP[Tree.CJUMP.LE ] = "ble";
    CJUMP[Tree.CJUMP.GE ] = "bge";
    CJUMP[Tree.CJUMP.ULT] = "bltu";
    CJUMP[Tree.CJUMP.ULE] = "bleu";
    CJUMP[Tree.CJUMP.UGT] = "bgtu";
    CJUMP[Tree.CJUMP.UGE] = "bgeu";
  }
  void munchStm(Tree.CJUMP s) {
		Temp l = munchExp(s.left);
		Temp r = munchExp(s.right);
		TempList tl = new TempList(l, new TempList(r, null));
		LabelList ll = new LabelList(s.iftrue, new LabelList(s.iffalse, null));
		String o = CJUMP[s.relop];
		emit(new Assem.OPER(o + "`s0, `s1, `j0", null, tl, ll));
  }

//Tree.LABEL
  void munchStm(Tree.LABEL l) {
		emit(new Assem.LABEL(l.toString() + ":", l));
  }

  Temp munchExp(Tree.Exp s) {
    if (s instanceof Tree.CONST)
      return munchExp((Tree.CONST)s);
    else if (s instanceof Tree.NAME)
      return munchExp((Tree.NAME)s);
    else if (s instanceof Tree.TEMP)
      return munchExp((Tree.TEMP)s);
    else if (s instanceof Tree.BINOP)
      return munchExp((Tree.BINOP)s);
    else if (s instanceof Tree.MEM)
      return munchExp((Tree.MEM)s);
    else if (s instanceof Tree.CALL)
      return munchExp((Tree.CALL)s);
    else
      throw new Error("Codegen.munchExp");
  }
	
//Tree.CONST
  Temp munchExp(Tree.CONST e) {
	  if (e.value == 0) {
			return frame.ZERO;
  	}
		if (e.value =! 0) {
			Temp t = new Temp();
			TempList tl = L(t);
			emit(OPER("li `d0," + e.value, tl, null));
    	return t;
		}
	}

	//Tree.Name
  Temp munchExp(Tree.NAME e) {
		Temp result = new Temp();
		emit(OPER("la `d0 " + e.label.toString(), L(result), null));
    return result;
  }

	//Tree.TEMP
  Temp munchExp(Tree.TEMP e) {
    if (e.temp == frame.FP) {
      Temp result = new Temp();
      emit(OPER("addu `d0 `s0 " + frame.name + "_framesize",
		L(result), L(frame.SP)));
      return result;
    } else {
    return e.temp;
		}
  }

	//Tree.BINOP
  private static String[] BINOP = new String[10];
  static {
    BINOP[Tree.BINOP.PLUS   ] = "add";
    BINOP[Tree.BINOP.MINUS  ] = "sub";
    BINOP[Tree.BINOP.MUL    ] = "mulo";
    BINOP[Tree.BINOP.DIV    ] = "div";
    BINOP[Tree.BINOP.AND    ] = "and";
    BINOP[Tree.BINOP.OR     ] = "or";
    BINOP[Tree.BINOP.LSHIFT ] = "sll";
    BINOP[Tree.BINOP.RSHIFT ] = "srl";
    BINOP[Tree.BINOP.ARSHIFT] = "sra";
    BINOP[Tree.BINOP.XOR    ] = "xor";
  }

  private static int shift(int i) {
    int shift = 0;
    if ((i >= 2) && ((i & (i - 1)) == 0)) {
      while (i > 1) {
	shift += 1;
	i >>= 1;
      }
    }
    return shift;
  }
//BINOP
  Temp munchExp(Tree.BINOP e) {
		Temp t = new Temp();
		Temp right = munchExp(e.right);
		Temp left = munchExp(e.left);
		
    if (e.right instanceof Tree.CONST) {
      return munchExp(e, e.left, (Tree.CONST)e.right);
		} else if (e.left instanceof Tree.CONST && e.right instanceof Tree.CONST) {
      return munchExp(e, (Tree.CONST)e.left, (Tree.CONST)e.right);
    } else if (e.left instanceof Tree.CONST) {
      return munchExp(e, (Tree.CONST)e.left, e.right);
		}
		String s = BINOP[e.binop];
    TempList tl = L(t);
    TempList tsl = L(left, L(right, null));
		emit(OPER(s + " `d0, `s0,`s1", tl, tsl));
    return t;
  }
	Temp munchExp(Tree.BINOP e, Tree.CONST right, Tree.Exp left) {
    Temp t = new Temp();
    TempList tl = L(t);
    String s = BINOP[e.binop];
    Temp l = munchExp(left);
    TempList tl2 = L(l);
    emit(new Assem.OPER(operation + " `d0, `s0," + right.value, tl, tl2));
    return t;
  }
	
//Tree.MEM*
  Temp munchExp(Tree.MEM e) {
		Temp result = new Temp();
  	if (e.exp instanceof Tree.CONST) {
			if (e.exp instanceof Tree.BINOP) {
      return munchExp(e, (Tree.CONST)e.exp);
    	}	
		} else {
    emit(OPER("lw `d0 (`s0)", L(result), L(munchExp(e.exp))));
    return result;
		}
  }
	Temp munchExp(Tree.MEM e, Tree.CONST a) {
    Temp result = new Temp();
    emit(OPER("lw `d0 " + a.value, L(result), null));
    return result;
  	}

//Tree.CALL
  Temp munchExp(Tree.CALL s) {
    if (s.func instanceof Tree.NAME) {
      emit(OPER("jal " + ((Tree.NAME)s.func).label.toString(), frame.calldefs, munchArgs(0, s.args)));
      return frame.V0;
    }
    emit(OPER("jal `d0 `s0", frame.calldefs, L(munchExp(s.func), munchArgs(0, s.args))));
    return frame.V0;
  }

  private TempList munchArgs(int i, Tree.ExpList args) {
    if (args == null)
      return null;
    Temp src = munchExp(args.head);
    if (i > frame.maxArgs)
      frame.maxArgs = i;
    switch (i) {
    case 0:
      emit(MOVE("move `d0 `s0", frame.A0, src));
      break;
    case 1:
      emit(MOVE("move `d0 `s0", frame.A1, src));
      break;
    case 2:
      emit(MOVE("move `d0 `s0", frame.A2, src));
      break;
    case 3:
      emit(MOVE("move `d0 `s0", frame.A3, src));
      break;
    default:
      emit(OPER("sw `s0 " + (i-1)*frame.wordSize() + "(`s1)",
		null, L(src, L(frame.SP))));
      break;
    }
    return L(src, munchArgs(i+1, args.tail));
  }	
}
