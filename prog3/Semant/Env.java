package Semant;
import Symbol.Table;
import Symbol.Symbol;
import Types.Type;
import Types.RECORD;
import Types.NAME;

class Env {
  Table venv;			// value environment
  Table tenv;			// type environment
  ErrorMsg.ErrorMsg errorMsg;

  private static Symbol sym(String s) {
    return Symbol.symbol(s);
  }

  private static final Types.VOID VOID = Semant.VOID;
  private static RECORD RECORD(Symbol n, Type t, RECORD x) {
    return new RECORD(n, t, x);
  }
  private static RECORD RECORD(Symbol n, Type t) {
    return new RECORD(n, t, null);
  }

  private static FunEntry FunEntry(RECORD f, Type r) {
    return new FunEntry(f, r);
  }

  Env(ErrorMsg.ErrorMsg err) {
    errorMsg = err;
    venv = new Table();
    tenv = new Table();

    // initialize tenv and venv with predefined identifiers
    NAME INT = new NAME(sym("int"));
    INT.bind(Semant.INT);
    tenv.put(sym("int"), INT);

    NAME STRING = new NAME(sym("string"));
    STRING.bind(Semant.STRING);
    tenv.put(sym("string"), STRING);

    venv.put(sym("print"),     CoolEntry(RECORD(sym("s"), STRING), VOID));
    venv.put(sym("flush"),     CoolEntry(null, VOID));
    venv.put(sym("getchar"),   CoolEntry(null, STRING));
    venv.put(sym("ord"),       CoolEntry(RECORD(sym("s"), STRING), INT));
    venv.put(sym("chr"),       CoolEntry(RECORD(sym("i"), INT), STRING));
    venv.put(sym("size"),      CoolEntry(RECORD(sym("s"), STRING), INT));
    venv.put(sym("substring"), CoolEntry(RECORD(sym("s"), STRING,
					       RECORD(sym("first"), INT,
						      RECORD(sym("n"), INT))),
					STRING));
    venv.put(sym("concat"),    CoolEntry(RECORD(sym("s1"), STRING,
					       RECORD(sym("s2"), STRING)),
					STRING));
    venv.put(sym("not"),       CoolEntry(RECORD(sym("i"), INT), INT));
    venv.put(sym("exit"),      CoolEntry(RECORD(sym("i"), INT), VOID));
  }
}
