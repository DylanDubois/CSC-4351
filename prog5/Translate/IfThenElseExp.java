package Translate;
import Temp.Temp;
import Temp.Label;

class IfThenElseExp extends Exp {
  Exp cond, a, b;
  Label t = new Label();
  Label f = new Label();
  Label join = new Label();

  IfThenElseExp(Exp cc, Exp aa, Exp bb) {
    cond = cc; 
    a = aa; 
    b = bb;
  }

  Tree.Stm unCx(Label tt, Label ff) {
    // This is the naive implementation; you should extend it to eliminate
    // unnecessary JUMP nodes
    Tree.Stm aStm = a.unCx(tt, ff);
	if (aStm instanceof Tree.JUMP) {
        Tree.JUMP aJump = (Tree.JUMP)aStm;
        if (aJump.exp instanceof Tree.NAME) {
            Tree.NAME aName = (Tree.NAME)aJump.exp;
            aStm = null;
            t = aName.Tree.label;
        }
    }
    Tree.Stm bStm = b.unCx(tt, ff);
	if (bStm instanceof Tree.JUMP) {
        Tree.JUMP bJump = (Tree.JUMP)bStm;
        if (bJump.exp instanceof Tree.NAME) {
            Tree.NAME bName = (Tree.NAME)bJump.exp;
            bStm = null;
            f = bName.Tree.label;
        }
    }
    Tree.Stm condStm = cond.unCx(t, f);

    if (aStm == null && bStm == null)
      return condStm;
    if (aStm == null)
      return new Tree.SEQ(condStm, new Tree.SEQ(new Tree.LABEL(f), bStm));
    if (bStm == null)
      return new Tree.SEQ(condStm, new Tree.SEQ(new Tree.LABEL(t), aStm));
    return new Tree.SEQ(condStm,
			new Tree.SEQ(new Tree.SEQ(new Tree.LABEL(t), aStm),
				     new Tree.SEQ(new Tree.LABEL(f), bStm)));
  }

  Tree.Exp unEx() {
    Temp t = new Temp();
    Tree.Exp aExp = a.unEx();
    if (aExp == null)
        return null;
        
    Tree.Exp bExp = b.unEx();
    if (bExp == null)
        return null;
        
    else
      return new Tree.ESEQ(new Tree.SEQ(new Tree.SEQ(cond.unCx(t, f), 
				new Tree.SEQ(new Tree.SEQ(new Tree.LABEL(t), new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(t), aExp), 
				new Tree.JUMP(join))), new Tree.SEQ(new Tree.LABEL(f), new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(t), bExp), 
				new Tree.JUMP(join))))), new Tree.LABEL(join)), new Tree.TEMP(t)); 
  }

  Tree.Stm unNx() {
   	Tree.Stm aStm = a.unNx();
    if (aStm == null) {
        t = join;
	} else {
        aStm = new Tree.SEQ(new Tree.SEQ(new Tree.LABEL(t), aStm), new Tree.JUMP(join));
	}
    Tree.Stm bStm = b.unNx();
    if (bStm == null) {
        f = join;
	} else {
        bStm = new Tree.SEQ(new Tree.SEQ(new Tree.LABEL(f), bStm), new Tree.JUMP(join));
	}
    if ((aStm == null) && (bStm == null))
        return cond.unNx(); 
    Tree.Stm condStm = cond.unCx(t, f);
    if (aStm == null)
        return new Tree.SEQ(new Tree.SEQ(condStm, bStm), new Tree.LABEL(join));
    if (bStm == null)
        return new Tree.SEQ(new Tree.SEQ(condStm, aStm), new Tree.LABEL(join));
    else
        return new Tree.SEQ(new Tree.SEQ(condStm, new Tree.SEQ(aStm, bStm)), new Tree.LABEL(join));
  }
}
