package Semant;

public class CoolEntry extends Entry {
  public Types.RECORD formals;
  public Types.Type result;
  CoolEntry(Types.RECORD f, Types.Type r) {
    formals = f;
    result = r;
  }
}
