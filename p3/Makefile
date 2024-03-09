###
# This Makefile can be used to make a parser for the base language
# (parser.class) and to make a program (P3.class) that tests the 
# parser and the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
FLAGS = -g  
CP = ./deps:.

P3.class: P3.java parser.class Yylex.class ASTnode.class
	$(JC) $(FLAGS) -cp $(CP) P3.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC) $(FLAGS) -cp $(CP) parser.java

parser.java: base.cup
	java -cp $(CP) java_cup.Main < base.cup

Yylex.class: base.jlex.java sym.class ErrMsg.class
	$(JC) $(FLAGS) -cp $(CP) base.jlex.java

ASTnode.class: ast.java
	$(JC) $(FLAGS) -cp $(CP) ast.java

base.jlex.java: base.jlex sym.class
	java -cp $(CP) JLex.Main base.jlex

sym.class: sym.java
	$(JC) $(FLAGS) -cp $(CP) sym.java

sym.java: base.cup
	java -cp $(CP) java_cup.Main < base.cup

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) -cp $(CP) ErrMsg.java

##test
test:
	java -cp $(CP) P3 test.base test.out

###
# clean
###
clean:
	rm -f *~ *.class parser.java base.jlex.java sym.java

## cleantest (delete test artifacts)
cleantest:
	rm -f *.out
