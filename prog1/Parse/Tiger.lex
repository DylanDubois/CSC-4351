package Parse;
import ErrorMsg.ErrorMsg;

%% 

%implements Lexer
%function nextToken
%type java_cup.runtime.Symbol
%char

%{
private void newline() {
  errorMsg.newline(yychar);
}

private void err(int pos, String s) {
  errorMsg.error(pos,s);
}

private void err(String s) {
  err(yychar,s);
}

private java_cup.runtime.Symbol tok(int kind) {
    return tok(kind, null);
}

private java_cup.runtime.Symbol tok(int kind, Object value) {
    return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

private ErrorMsg errorMsg;

Yylex(java.io.InputStream s, ErrorMsg e) {
  this(s);
  errorMsg=e;
}

%}

%eofval{
	{
	 return tok(sym.EOF, null);
        }
%eofval} 

private int commentCount = 0;
StringBuffer string = new StringBuffer();
%state STRING
%state COMMENT
%state STRING_IGNORE
letter=[a-zA-Z]
digit=[0-9]

%%
<YYINITIAL> " " {}
<YYINITIAL> \n|\r\n {newline();}
<YYINITIAL> "/*" {yybegin(COMMENT);}
<COMMENT> "/*" {commentCount += 1; yybegin(COMMENT);}
<COMMENT> "*/" {
	if (commentCount <= 0) 
		yybegin(COMMENT); 
	else 
		commentCount--;
}
<COMMENT> . {}
<YYINITIAL> "," {return tok(sym.COMMA, null);}
<YYINITIAL> ":" {return tok(sym.COLON, null);}
<YYINITIAL> ";" {return tok(sym.SEMICOLON, null);}
<YYINITIAL> "(" {return tok(sym.LPAREN, null);}
<YYINITIAL> ")" {return tok(sym.RPAREN, null);}
<YYINITIAL> "[" {return tok(sym.RBRACK, null);}
<YYINITIAL> "]" {return tok(sym.LBRACK, null);}
<YYINITIAL> "{" {return tok(sym.RBRACE, null);}
<YYINITIAL> "}" {return tok(sym.LBRACE, null);}
<YYINITIAL> "." {return tok(sym.DOT, null);}
<YYINITIAL> "+" {return tok(sym.PLUS, null);}
<YYINITIAL> "-" {return tok(sym.MINUS, null);}
<YYINITIAL> "*" {return tok(sym.TIMES, null);}
<YYINITIAL> "/" {return tok(sym.DIVIDE, null);}
<YYINITIAL> "=" {return tok(sym.EQ, null);}
<YYINITIAL> "<>" {return tok(sym.NEQ, null);}
<YYINITIAL> "<" {return tok(sym.LT, null);}
<YYINITIAL> "<=" {return tok(sym.LE, null);}
<YYINITIAL> ">" {return tok(sym.GT, null);}
<YYINITIAL> ">=" {return tok(sym.GE, null);}
<YYINITIAL> "&" {return tok(sym.AND, null);}
<YYINITIAL> "|" {return tok(sym.OR, null);}
<YYINITIAL> ":=" {return tok(sym.ASSIGN, null);}

<YYINITIAL> "while" {return tok(sym.WHILE, null);}
<YYINITIAL> "for" {return tok(sym.FOR, null);}
<YYINITIAL> "to" {return tok(sym.TO, null);}
<YYINITIAL> "break" {return tok(sym.BREAK, null);}
<YYINITIAL> "let" {return tok(sym.LET, null);}
<YYINITIAL> "in" {return tok(sym.IN, null);}
<YYINITIAL> "end" {return tok(sym.END, null);}
<YYINITIAL> "function" {return tok(sym.FUNCTION, null);}
<YYINITIAL> "var" {return tok(sym.VAR, null);}
<YYINITIAL> "type" {return tok(sym.TYPE, null);}
<YYINITIAL> "array" {return tok(sym.ARRAY, null);}
<YYINITIAL> "if" {return tok(sym.IF, null);}
<YYINITIAL> "then" {return tok(sym.THEN, null);} 
<YYINITIAL> "else" {return tok(sym.ELSE, null);}
<YYINITIAL> "do" {return tok(sym.DO, null);}
<YYINITIAL> "of" {return tok(sym.OF, null);}
<YYINITIAL> "nil" {return tok(sym.NIL, null);}

<STRING> \" { yybegin(YYINITIAL); return tok(sym.STRING, string.toString());}
<STRING> \n|\r\n { err("Error parsing string" + " \"" + string.toString() + "\""); yybegin(YYINITIAL); newline(); return tok(sym.STRING, string.toString());}
<STRING> [^\n\r\"\\]+ {string.append(yytext());}
<STRING> \\t {string.append('\t');}
<STRING> \\n {string.append('\n');}
<STRING> \\r {string.append('\r');}
<STRING> \\\" {string.append('\"');}
<STRING> \\ { yybegin(STRING_IGNORE);}

<STRING_IGNORE> n {string.append("\n"); yybegin(STRING); }
<STRING_IGNORE> "^"{letters} {string.append((char)(yytext().charAt(1)-'A'+1));yybegin(STRING);}
<STRING_IGNORE> \\{digit}{digit}{digit} {int a = Integer.parseInt(yytext()); if (a < 128) {string.append((char)a);} else {err("Not in the ASCII code range");} yybegin(STRING);}
<STRING_IGNORE> t {string.append("\t"); yybegin(STRING); }
<STRING_IGNORE> \" {string.append("\""); yybegin(STRING); }
<STRING_IGNORE> \\ {string.append("\\"); yybegin(STRING); }
<STRING_IGNORE> " "|\t|\f {}
<STRING_IGNORE> . {err("Unexpected character '" + yytext() + "' after '\\'."); }

<YYINITIAL> [digit]+ {return tok(sym.INT, new Integer(yytext()));}
<YYINITIAL> [letter](letter|digit|_)* {return tok(sym.ID, yytext());}

<STRING> . {string.append(yytext());}

. { err("Illegal character: " + yytext()); }
