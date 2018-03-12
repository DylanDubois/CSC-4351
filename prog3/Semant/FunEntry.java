package Semant;

public class FunEntry extends Entry {
  public Types.RECORD formals;
  public Types.Type result;
  CoolEntry(Types.RECORD f, Types.Type r) {
    formals = f;
    result = r;
  }
}
