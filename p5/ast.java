import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a base program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and identifiers contain line and character 
// number information; for string literals and identifiers, they also 
// contain a string; for integer literals, they also contain an integer 
// value.
//
// Here are all the different kinds of AST nodes and what kinds of 
// children they have.  All of these kinds of AST nodes are subclasses
// of "ASTnode".  Indentation indicates further subclassing:
//
//     Subclass              Children
//     --------              --------
//     ProgramNode           DeclListNode
//     DeclListNode          linked list of DeclNode
//     DeclNode:
//       VarDeclNode         TypeNode, IdNode, int
//       FctnDeclNode        TypeNode, IdNode, FormalsListNode, FctnBodyNode
//       FormalDeclNode      TypeNode, IdNode
//       TupleDeclNode       IdNode, DeclListNode
//
//     StmtListNode          linked list of StmtNode
//     ExpListNode           linked list of ExpNode
//     FormalsListNode       linked list of FormalDeclNode
//     FctnBodyNode          DeclListNode, StmtListNode
//
//     TypeNode:
//       LogicalNode         --- none ---
//       IntegerNode         --- none ---
//       VoidNode            --- none ---
//       TupleNode           IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       TupleAccessNode     ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         LessEqNode
//         GreaterNode
//         GreaterEqNode
//         AndNode
//         OrNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, 
// or internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        LogicalNode,  IntegerNode,  VoidNode,    IdNode,  
//        TrueNode,     FalseNode,    IntLitNode,  StrLitNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, StmtListNode, ExpListNode, FormalsListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,     FctnDeclNode,  FormalDeclNode,
//        TupleDeclNode,   FctnBodyNode,    TupleNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,    WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  TupleAccessNode, AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,  NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,       TimesNode,     DivideNode,
//        EqualsNode,      NotEqualsNode,   LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,   AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, DeclListNode, StmtListNode, ExpListNode,
// FormalsListNode, FctnBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void typeChecker() {
        myDeclList.typeChecker();
    }

    /***
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, tuple defintions, and functions in the program.
     ***/
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void typeChecker() {
        for (DeclNode node : myDecls) {
            node.typeChecker();
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     ***/
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing tuple names in variable decls), process all of the
     * decls in the list.
     ***/
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode) node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void typeChecker(FctnSym symFctn) {
        for (StmtNode node : myStmts) {
            if (node instanceof ReturnStmtNode) {
                ((ReturnStmtNode) node).typeChecker(symFctn);
            } else {
                node.typeChecker();
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     ***/
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public int len() {
        int length = 0;
        for (ExpNode node : myExps) {
            length++;
        }
        return length;
    }

    public void typeChecker(List<Type> params) {
        for (int i = 0; i < len(); i++) {
            Type nodeType = null;
            if (myExps.get(i) instanceof IdNode) {
                nodeType = ((IdNode) myExps.get(i)).sym().getType();
            } else if (myExps.get(i) instanceof IntLitNode) {
                nodeType = new IntegerType();
            } else if (myExps.get(i) instanceof StrLitNode) {
                nodeType = new StringType();
            } else if (myExps.get(i) instanceof TupleAccessNode) {
                nodeType = ((TupleAccessNode) myExps.get(i)).sym().getType();
            }
            if (!nodeType.equals(params.get(i))) {
                ErrMsg.fatal(myExps.get(i).lineNum(), myExps.get(i).charNum(),
                        "Actual type does not match formal type");
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     ***/
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     * process the formal decl
     * if there was no error, add type of formal decl to list
     ***/
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }

    /***
     * Return the number of formals in this list.
     ***/
    public int length() {
        return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FctnBodyNode extends ASTnode {
    public FctnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void typeChecker(FctnSym symFctn) {
        myDeclList.typeChecker();
        myStmtList.typeChecker(symFctn);
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     ***/
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

// **********************************************************************
// **** DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /***
     * Note: a formal decl needs to return a sym
     ***/
    abstract public Sym nameAnalysis(SymTable symTab);

    abstract public void typeChecker();
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /***
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a tuple type,
     * lookup type name (globally)
     * if type name doesn't exist, then error
     * if no errors so far,
     * if name has already been declared in this scope, then error
     * else add name to local symbol table
     *
     * symTab is local symbol table (say, for tuple field decls)
     * globalTab is global symbol table (for tuple type names)
     * symTab and globalTab can be the same
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }

    public Sym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Sym sym = null;
        IdNode tupleId = null;

        if (myType instanceof VoidNode) { // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Non-function declared void");
            badDecl = true;
        }

        else if (myType instanceof TupleNode) {
            tupleId = ((TupleNode) myType).idNode();
            try {
                sym = globalTab.lookupGlobal(tupleId.name());

                // if the name for the tuple type is not found,
                // or is not a tuple type
                if (sym == null || !(sym instanceof TupleDefSym)) {
                    ErrMsg.fatal(tupleId.lineNum(), tupleId.charNum(),
                            "Invalid name of tuple type");
                    badDecl = true;
                } else {
                    tupleId.link(sym);
                }
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply-declared identifier");
                badDecl = true;
            }
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in VarDeclNode.nameAnalysis");
            System.exit(-1);
        }

        if (!badDecl) { // insert into symbol table
            try {
                if (myType instanceof TupleNode) {
                    sym = new TupleSym(tupleId);
                } else {
                    sym = new Sym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymNameException ex) {
                System.err.println("Unexpected DuplicateSymNameException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(".");
    }

    // 3 children
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NON_TUPLE if this is not a tuple type

    public static int NON_TUPLE = -1;

    @Override
    public void typeChecker() {
        // do nothing
    }
}

class FctnDeclNode extends DeclNode {
    public FctnDeclNode(TypeNode type,
            IdNode id,
            FormalsListNode formalList,
            FctnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void typeChecker() {
        myBody.typeChecker(mySym);
    }

    public void link(FctnSym symFunc) {
        mySym = symFunc;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     * enter new scope
     * process the formals
     * if this function is not multiply declared,
     * update symbol table entry with types of formals
     * process the body of the function
     * exit scope
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FctnSym sym = null;
        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply-declared identifier");
            }

            else { // add function name to local symbol table
                try {
                    sym = new FctnSym(myType.type(), myFormalsList.length());
                    link(sym);
                    symTab.addDecl(name, sym);
                    myId.link(sym);
                } catch (DuplicateSymNameException ex) {
                    System.err.println("Unexpected DuplicateSymNameException " +
                            " in FctnDeclNode.nameAnalysis");
                    System.exit(-1);
                } catch (EmptySymTableException ex) {
                    System.err.println("Unexpected EmptySymTableException " +
                            " in FctnDeclNode.nameAnalysis");
                    System.exit(-1);
                }
            }
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in FctnDeclNode.nameAnalysis");
            System.exit(-1);
        }

        symTab.addScope(); // add a new scope for locals and params

        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }

        myBody.nameAnalysis(symTab); // process the function body

        try {
            symTab.removeScope(); // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in FctnDeclNode.nameAnalysis");
            System.exit(-1);
        }

        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("{");
        myFormalsList.unparse(p, 0);
        p.println("} [");
        myBody.unparse(p, indent + 4);
        p.println("]\n");
    }

    // 4 children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FctnBodyNode myBody;
    private FctnSym mySym;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     * then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;

        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Non-function declared void");
            badDecl = true;
        }

        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply-declared identifier");
                badDecl = true;
            }
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in FormalDeclNode.nameAnalysis");
            System.exit(-1);
        }

        if (!badDecl) { // insert into symbol table
            try {
                sym = new Sym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymNameException ex) {
                System.err.println("Unexpected DuplicateSymNameException " +
                        " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    // 2 children
    private TypeNode myType;
    private IdNode myId;

    @Override
    public void typeChecker() {
        // Do nothing
    }
}

class TupleDeclNode extends DeclNode {
    public TupleDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     * then multiply declared error (don't add to symbol table)
     * create a new symbol table for this tuple definition
     * process the decl list
     * if no errors
     * add a new entry to symbol table for this tuple
     ***/
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply-declared identifier");
                badDecl = true;
            }
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in TupleDeclNode.nameAnalysis");
            System.exit(-1);
        }

        SymTable tupleSymTab = new SymTable();

        // process the fields of the tuple
        myDeclList.nameAnalysis(tupleSymTab, symTab);

        if (!badDecl) {
            try { // add entry to symbol table
                TupleDefSym sym = new TupleDefSym(tupleSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymNameException ex) {
                System.err.println("Unexpected DuplicateSymNameException " +
                        " in TupleDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in TupleDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("tuple ");
        myId.unparse(p, 0);
        p.println(" {");
        myDeclList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}.\n");
    }

    // 2 children
    private IdNode myId;
    private DeclListNode myDeclList;

    @Override
    public void typeChecker() {
        // TODO Auto-generated method stub
    }
}

// **********************************************************************
// ***** TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class LogicalNode extends TypeNode {
    public LogicalNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new LogicalType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("logical");
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new IntegerType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new VoidType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class TupleNode extends TypeNode {
    public TupleNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }

    /***
     * type
     ***/
    public Type type() {
        return new TupleType(myId);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("tuple ");
        p.print(myId.name());
    }

    // 1 child
    private IdNode myId;
}

// **********************************************************************
// **** StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);

    abstract public void typeChecker();
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    public void typeChecker() {
        myAssign.typeChecker();
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void typeChecker() {
        if (myExp instanceof IdNode) {
            if (!(((IdNode) myExp).sym().getType().isIntegerType())) {
                ErrMsg.fatal(((IdNode) this.myExp).lineNum(), ((IdNode) this.myExp).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void typeChecker() {
        if (myExp instanceof IdNode) {
            if (!(((IdNode) myExp).sym().getType().isIntegerType())) {
                ErrMsg.fatal(((IdNode) this.myExp).lineNum(), ((IdNode) this.myExp).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    // 1 child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void typeChecker() {
        if (myExp instanceof EqualsNode) {
            ((EqualsNode) myExp).typeChecker();
        } else if (myExp instanceof NotEqualsNode) {
            ((NotEqualsNode) myExp).typeChecker();
        } else if (myExp instanceof GreaterNode) {
            ((GreaterNode) myExp).typeChecker();
        } else if (myExp instanceof GreaterEqNode) {
            ((GreaterEqNode) myExp).typeChecker();
        } else if (myExp instanceof LessNode) {
            ((LessNode) myExp).typeChecker();
        } else if (myExp instanceof LessEqNode) {
            ((LessEqNode) myExp).typeChecker();
        } else if (myExp instanceof AndNode) {
            ((AndNode) myExp).typeChecker();
        } else if (myExp instanceof OrNode) {
            ((OrNode) myExp).typeChecker();
        } else if (myExp instanceof NotNode) {
            ((NotNode) myExp).typeChecker();
        } else {
            ErrMsg.fatal((this.myExp).lineNum(), (this.myExp).charNum(), "Non-logical expression used in if condition");
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if ");
        myExp.unparse(p, 0);
        p.println(" [");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("]");
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
            StmtListNode slist1, DeclListNode dlist2,
            StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void typeChecker() {
        if (myExp instanceof EqualsNode) {
            ((EqualsNode) myExp).typeChecker();
        } else if (myExp instanceof NotEqualsNode) {
            ((NotEqualsNode) myExp).typeChecker();
        } else if (myExp instanceof GreaterNode) {
            ((GreaterNode) myExp).typeChecker();
        } else if (myExp instanceof GreaterEqNode) {
            ((GreaterEqNode) myExp).typeChecker();
        } else if (myExp instanceof LessNode) {
            ((LessNode) myExp).typeChecker();
        } else if (myExp instanceof LessEqNode) {
            ((LessEqNode) myExp).typeChecker();
        } else if (myExp instanceof AndNode) {
            ((AndNode) myExp).typeChecker();
        } else if (myExp instanceof OrNode) {
            ((OrNode) myExp).typeChecker();
        } else if (myExp instanceof NotNode) {
            ((NotNode) myExp).typeChecker();
        } else {
            ErrMsg.fatal((this.myExp).lineNum(), (this.myExp).charNum(), "Non-logical expression used in if condition");
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if ");
        myExp.unparse(p, 0);
        p.println(" [");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("]");
        doIndent(p, indent);
        p.println("else [");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("]");
    }

    // 5 children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void typeChecker() {
        if (myExp instanceof EqualsNode) {
            ((EqualsNode) myExp).typeChecker();
        } else if (myExp instanceof NotEqualsNode) {
            ((NotEqualsNode) myExp).typeChecker();
        } else if (myExp instanceof GreaterNode) {
            ((GreaterNode) myExp).typeChecker();
        } else if (myExp instanceof GreaterEqNode) {
            ((GreaterEqNode) myExp).typeChecker();
        } else if (myExp instanceof LessNode) {
            ((LessNode) myExp).typeChecker();
        } else if (myExp instanceof LessEqNode) {
            ((LessEqNode) myExp).typeChecker();
        } else if (myExp instanceof AndNode) {
            ((AndNode) myExp).typeChecker();
        } else if (myExp instanceof OrNode) {
            ((OrNode) myExp).typeChecker();
        } else if (myExp instanceof NotNode) {
            ((NotNode) myExp).typeChecker();
        } else {
            ErrMsg.fatal((this.myExp).lineNum(), (this.myExp).charNum(),
                    "Non-logical expression used in while condition");
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while ");
        myExp.unparse(p, 0);
        p.println(" [");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("]");
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /***
     * typeChecker
     * Checks exp node type
     * Returns error if:
     * - Node is a function
     * - Node is a tuple
     * - Node is a tuple variable
     * - Node is of void type
     ***/
    public void typeChecker() {
        if (myExp instanceof IdNode) {
            Sym mySym = ((IdNode) myExp).sym();
            if (mySym.getType().isFctnType()) {
                ErrMsg.fatal(((IdNode) myExp).lineNum(), ((IdNode) myExp).charNum(), "Read attempt of function name");
            }
            if (mySym.getType().isTupleType()) {
                ErrMsg.fatal(((IdNode) myExp).lineNum(), ((IdNode) myExp).charNum(), "Read attempt of tuple variable");
            }
            if (mySym.getType().isTupleDefType()) {
                ErrMsg.fatal(((IdNode) myExp).lineNum(), ((IdNode) myExp).charNum(), "Read attempt of tuple name");
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("read >> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    // 1 child (actually can only be an IdNode or a TupleAccessNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * typeChecker
     * Checks exp node type
     * Returns error if:
     * - Node is a function
     * - Node is a tuple
     * - Node is a tuple variable
     * - Node is of void type
     ***/
    public void typeChecker() {
        if (myExp instanceof IdNode) {
            Sym mySym = ((IdNode) myExp).sym();
            if (mySym.getType().isFctnType()) {
                ErrMsg.fatal(((IdNode) myExp).lineNum(), ((IdNode) myExp).charNum(), "Write attempt of function name");
            }
            if (mySym.getType().isTupleType()) {
                ErrMsg.fatal(((IdNode) myExp).lineNum(), ((IdNode) myExp).charNum(), "Write attempt of tuple variable");
            }
            if (mySym.getType().isTupleDefType()) {
                ErrMsg.fatal(((IdNode) myExp).lineNum(), ((IdNode) myExp).charNum(), "Write attempt of tuple name");
            }
        } else if (myExp instanceof CallExpNode) {
            ((CallExpNode) myExp).typeChecker(true);
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("write << ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    // 1 child
    private ExpNode myExp;

}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(".");
    }

    // 1 child
    private CallExpNode myCall;

    @Override
    public void typeChecker() {
        myCall.typeChecker();
    }
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     ***/
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(".");
    }

    // 1 child
    private ExpNode myExp; // possibly null

    @Override
    public void typeChecker() {
        // Shouldn't be used
    }

    public void typeChecker(FctnSym symFctn) {
        Type returnType = symFctn.getReturnType();
        if (returnType.isVoidType()) {
            if (myExp != null) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Return with value in void function");
            }
        } else {
            if (myExp == null) {
                ErrMsg.fatal(0, 0, "Return value missing");
            } else {
                Type myType = null;
                if (myExp instanceof IdNode) {
                    myType = ((IdNode) myExp).sym().getType();
                } else if ((myExp instanceof TrueNode) | (myExp instanceof FalseNode)) {
                    myType = new LogicalType();
                } else if (myExp instanceof IntLitNode) {
                    myType = new IntegerType();
                } else if (myExp instanceof StrLitNode) {
                    myType = new StringType();
                } else if (myExp instanceof TupleAccessNode) {
                    myType = ((TupleAccessNode) myExp).sym().getType();
                }
                if (!(myType.equals(returnType))) {
                    ErrMsg.fatal((myExp).lineNum(), (myExp).charNum(), "Return value wrong type");
                }
            }
        }
    }
}

// **********************************************************************
// **** ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /***
     * Default version for nodes with no names
     ***/
    public void nameAnalysis(SymTable symTab) {
    }

    /***
     * Return the line number for ExpNode
     ***/
    public int lineNum() {
        return 0;
    };

    /***
     * Return the char number for ExpNode
     ***/
    public int charNum() {
        return 0;
    };

    public void typeChecker(Type myType) {
    }
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("True");
    }

    private int myLineNum;
    private int myCharNum;

    @Override
    public int lineNum() {
        return myLineNum;
    }

    @Override
    public int charNum() {
        return myCharNum;
    }
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("False");
    }

    private int myLineNum;
    private int myCharNum;

    @Override
    public int lineNum() {
        return myLineNum;
    }

    @Override
    public int charNum() {
        return myCharNum;
    }
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /***
     * Link the given symbol to this ID.
     ***/
    public void link(Sym sym) {
        mySym = sym;
    }

    /***
     * Return the name of this ID.
     ***/
    public String name() {
        return myStrVal;
    }

    /***
     * Return the symbol associated with this ID.
     ***/
    public Sym sym() {
        return mySym;
    }

    /***
     * Return the line number for this ID.
     ***/
    public int lineNum() {
        return myLineNum;
    }

    /***
     * Return the char number for this ID.
     ***/
    public int charNum() {
        return myCharNum;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     ***/
    public void nameAnalysis(SymTable symTab) {
        try {
            Sym sym = symTab.lookupGlobal(myStrVal);
            if (sym == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
            } else {
                link(sym);
            }
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IdNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("<" + mySym + ">");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;

    @Override
    public int lineNum() {
        return myLineNum;
    }

    @Override
    public int charNum() {
        return myCharNum;
    }

}

class StrLitNode extends ExpNode {
    public StrLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;

    @Override
    public int lineNum() {
        return myLineNum;
    }

    @Override
    public int charNum() {
        return myCharNum;
    }
}

class TupleAccessNode extends ExpNode {
    public TupleAccessNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
    }

    /***
     * Return the symbol associated with this colon-access node.
     ***/
    public Sym sym() {
        return mySym;
    }

    /***
     * Return the line number for this colon-access node.
     * The line number is the one corresponding to the RHS of the colon-access.
     ***/
    public int lineNum() {
        return myId.lineNum();
    }

    /***
     * Return the char number for this colon-access node.
     * The char number is the one corresponding to the RHS of the colon-access.
     ***/
    public int charNum() {
        return myId.charNum();
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the colon-access
     * - process the RHS of the colon-access
     * - if the RHS is of a tuple type, set the sym for this node so that
     * a colon-access "higher up" in the AST can get access to the symbol
     * table for the appropriate tuple definition
     ***/
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable tupleSymTab = null; // to lookup RHS of colon-access
        Sym sym = null;

        myLoc.nameAnalysis(symTab); // do name analysis on LHS

        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode) myLoc;
            sym = id.sym();

            // check ID has been declared to be of a tuple type

            if (sym == null) { // ID was undeclared
                badAccess = true;
            } else if (sym instanceof TupleSym) {
                // get symbol table for tuple type
                Sym tempSym = ((TupleSym) sym).getTupleType().sym();
                tupleSymTab = ((TupleDefSym) tempSym).getSymTable();
            } else { // LHS is not a tuple type
                ErrMsg.fatal(id.lineNum(), id.charNum(),
                        "Colon-access of non-tuple type");
                badAccess = true;
            }
        }

        // if myLoc is really a colon-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a tuple type, or
        // a link to the Sym for the tuple type RHSid was declared to be
        else if (myLoc instanceof TupleAccessNode) {
            TupleAccessNode loc = (TupleAccessNode) myLoc;

            if (loc.badAccess) { // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this colon-access
            } else { // no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) { // no tuple in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(),
                            "Colon-access of non-tuple type");
                    badAccess = true;
                } else { // get the tuple's symbol table in which to lookup RHS
                    if (sym instanceof TupleDefSym) {
                        tupleSymTab = ((TupleDefSym) sym).getSymTable();
                    } else {
                        System.err.println("Unexpected Sym type in TupleAccessNode");
                        System.exit(-1);
                    }
                }
            }

        }

        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of colon-access");
            System.exit(-1);
        }

        // do name analysis on RHS of colon-access in the tuple's symbol table
        if (!badAccess) {
            try {
                sym = tupleSymTab.lookupGlobal(myId.name()); // lookup
                if (sym == null) { // not found - RHS is not a valid field name
                    ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                            "Invalid tuple field name");
                    badAccess = true;
                }

                else {
                    myId.link(sym); // link the symbol
                    // if RHS is itself as tuple type, link the symbol for its tuple
                    // type to this colon-access node (to allow chained colon-access)
                    if (sym instanceof TupleSym) {
                        mySym = ((TupleSym) sym).getTupleType().sym();
                    }
                }
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in TupleAccessNode.nameAnalysis");
                System.exit(-1);
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    // 4 children
    private ExpNode myLoc;
    private IdNode myId;
    private Sym mySym; // link to Sym for tuple type
    private boolean badAccess; // to prevent multiple, cascading errors
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void typeChecker() {
        if (myExp instanceof BinaryExpNode) {
            ((BinaryExpNode) myExp).typeChecker();
        } else {

            Type type1 = null;
            Type type2 = null;
            if (this.myExp instanceof IdNode) {
                type1 = ((IdNode) this.myExp).sym().getType();
            } else if ((this.myExp instanceof TrueNode) | (this.myExp instanceof FalseNode)) {
                type1 = new LogicalType();
            } else if ((this.myExp instanceof IntLitNode)) {
                type1 = new IntegerType();
            } else if ((this.myExp instanceof StrLitNode)) {
                type1 = new StringType();
            } else if (this.myExp instanceof TupleAccessNode) {
                type1 = ((TupleAccessNode) this.myExp).sym().getType();
            } else if (this.myExp instanceof CallExpNode) {
                type1 = ((FctnSym) ((CallExpNode) this.myExp).sym()).getReturnType();
            }

            if (this.myLhs instanceof IdNode) {
                type2 = ((IdNode) this.myLhs).sym().getType();
            } else if ((this.myLhs instanceof TrueNode) | (this.myLhs instanceof FalseNode)) {
                type2 = new LogicalType();
            } else if ((this.myLhs instanceof IntLitNode)) {
                type2 = new IntegerType();
            } else if ((this.myLhs instanceof StrLitNode)) {
                type2 = new StringType();
            } else if (this.myLhs instanceof TupleAccessNode) {
                type2 = ((TupleAccessNode) this.myLhs).sym().getType();
            } else if (this.myLhs instanceof CallExpNode) {
                type2 = ((FctnSym) ((CallExpNode) this.myLhs).sym()).getReturnType();
            }

            if (!type1.equals(type2)) {
                ErrMsg.fatal(this.myLhs.lineNum(), this.myLhs.charNum(), "Mismatched type");
            }else {
                if (type1.isFctnType()) {
                    ErrMsg.fatal(this.myLhs.lineNum(), this.myLhs.charNum(),
                            "Assignment to function name");
                }
                if (type1.isTupleType()) {
                    ErrMsg.fatal(this.myLhs.lineNum(), this.myLhs.charNum(),
                    "Assignment to tuple variable");
                }
                if (type1.isTupleDefType()) {
                    ErrMsg.fatal(this.myLhs.lineNum(), this.myLhs.charNum(),
                    "Assignment to tuple name");
                }
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's
     * two children
     ***/
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    // 2 children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public Sym sym() {
        return myId.sym();
    }

    public void typeChecker(boolean writeCheck) {
        Sym mySym = myId.sym();
        if (((FctnSym) mySym).getReturnType().isVoidType()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Write attempt of void");
        }
    }

    public void typeChecker() {
        Sym mySym = myId.sym();
        if (!(mySym.getType().isFctnType())) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Call attempt on non-function");
        } else {
            if (myExpList.len() != ((FctnSym) mySym).getNumParams()) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Function call with wrong # of args");
            } else {
                myExpList.typeChecker(((FctnSym) mySym).getParamTypes());
            }
        }
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's
     * two children
     ***/
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }

    // **** unparse ****
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's
     * two children
     ***/
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    abstract public void typeChecker();

    // 2 children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// **** Subclasses of UnaryExpNode
// **********************************************************************

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void typeChecker() {
        if (!(((IdNode) this.myExp).sym().getType().isLogicalType())) {
            ErrMsg.fatal(this.myExp.lineNum(), this.myExp.charNum(),
                    "Logical operator used with non-logical operand");
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(~");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// **** Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Arithmetic operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Arithmetic operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        Type type1 = null;
        Type type2 = null;
        if (this.myExp1 instanceof IdNode) {
            type1 = ((IdNode) this.myExp1).sym().getType();
        } else if ((this.myExp1 instanceof TrueNode) | (this.myExp1 instanceof FalseNode)) {
            type1 = new LogicalType();
        } else if ((this.myExp1 instanceof IntLitNode)) {
            type1 = new IntegerType();
        } else if ((this.myExp1 instanceof StrLitNode)) {
            type1 = new StringType();
        } else if (this.myExp1 instanceof TupleAccessNode) {
            type1 = ((TupleAccessNode) this.myExp1).sym().getType();
        } else if (this.myExp1 instanceof CallExpNode) {
            type1 = ((FctnSym) ((CallExpNode) this.myExp1).sym()).getReturnType();
        }

        if (this.myExp2 instanceof IdNode) {
            type2 = ((IdNode) this.myExp2).sym().getType();
        } else if ((this.myExp2 instanceof TrueNode) | (this.myExp2 instanceof FalseNode)) {
            type2 = new LogicalType();
        } else if ((this.myExp2 instanceof IntLitNode)) {
            type2 = new IntegerType();
        } else if ((this.myExp2 instanceof StrLitNode)) {
            type2 = new StringType();
        } else if (this.myExp2 instanceof TupleAccessNode) {
            type2 = ((TupleAccessNode) this.myExp2).sym().getType();
        } else if (this.myExp2 instanceof CallExpNode) {
            type2 = ((FctnSym) ((CallExpNode) this.myExp2).sym()).getReturnType();
        }

        if (!type1.equals(type2)) {
            ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(), "Mismatched type");
        } else {
            if (type1.isFctnType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(),
                        "Equality operator used with function names");
            }
            if (type1.isVoidType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(),
                        "Equality operator used with void function calls");
            }
            if (type1.isTupleType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(), "Equality operator used with tuple variables");
            }
            if (type1.isTupleDefType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(), "Equality operator used with tuple names");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        Type type1 = null;
        Type type2 = null;
        if (this.myExp1 instanceof IdNode) {
            type1 = ((IdNode) this.myExp1).sym().getType();
        } else if ((this.myExp1 instanceof TrueNode) | (this.myExp1 instanceof FalseNode)) {
            type1 = new LogicalType();
        } else if ((this.myExp1 instanceof IntLitNode)) {
            type1 = new IntegerType();
        } else if ((this.myExp1 instanceof StrLitNode)) {
            type1 = new StringType();
        } else if (this.myExp1 instanceof TupleAccessNode) {
            type1 = ((TupleAccessNode) this.myExp1).sym().getType();
        } else if (this.myExp1 instanceof CallExpNode) {
            type1 = ((FctnSym) ((CallExpNode) this.myExp1).sym()).getReturnType();
        }

        if (this.myExp2 instanceof IdNode) {
            type2 = ((IdNode) this.myExp2).sym().getType();
        } else if ((this.myExp2 instanceof TrueNode) | (this.myExp2 instanceof FalseNode)) {
            type2 = new LogicalType();
        } else if ((this.myExp2 instanceof IntLitNode)) {
            type2 = new IntegerType();
        } else if ((this.myExp2 instanceof StrLitNode)) {
            type2 = new StringType();
        } else if (this.myExp2 instanceof TupleAccessNode) {
            type2 = ((TupleAccessNode) this.myExp2).sym().getType();
        } else if (this.myExp2 instanceof CallExpNode) {
            type2 = ((FctnSym) ((CallExpNode) this.myExp2).sym()).getReturnType();
        }

        if (!type1.equals(type2)) {
            ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(), "Mismatched type");
        } else {
            if (type1.isFctnType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(),
                        "Equality operator used with function names");
            }
            if (type1.isVoidType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(),
                        "Equality operator used with void function calls");
            }
            if (type1.isTupleType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(), "Equality operator used with tuple variables");
            }
            if (type1.isTupleDefType()) {
                ErrMsg.fatal(this.myExp1.lineNum(), this.myExp1.charNum(), "Equality operator used with tuple names");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" ~= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof IntLitNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isIntegerType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp1 instanceof CallExpNode) {
                Type type1 = ((CallExpNode) this.myExp1).sym().getType();
                if (!(type1.isIntegerType())) {
                    ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
        if (!(this.myExp2 instanceof IntLitNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else if (this.myExp2 instanceof CallExpNode) {
                Type type2 = ((CallExpNode) this.myExp2).sym().getType();
                if (!(type2.isIntegerType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Relational operator used with non-integer operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Relational operator used with non-integer operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof TrueNode) | !(this.myExp1 instanceof FalseNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isLogicalType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Logical operator used with non-logical operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Logical operator used with non-logical operand");
            }
        }
        if (!(this.myExp2 instanceof TrueNode) | !(this.myExp2 instanceof FalseNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isLogicalType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Logical operator used with non-logical operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Logical operator used with non-logical operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" & ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void typeChecker() {
        if (!(this.myExp1 instanceof TrueNode) | !(this.myExp1 instanceof FalseNode)) {
            if (this.myExp1 instanceof IdNode) {
                Sym sym1 = ((IdNode) this.myExp1).sym();
                if (!(sym1.getType().isLogicalType())) {
                    ErrMsg.fatal(((IdNode) this.myExp1).lineNum(), ((IdNode) this.myExp1).charNum(),
                            "Logical operator used with non-logical operand");
                }
            } else {
                ErrMsg.fatal((this.myExp1).lineNum(), (this.myExp1).charNum(),
                        "Logical operator used with non-logical operand");
            }
        }
        if (!(this.myExp2 instanceof TrueNode) | !(this.myExp2 instanceof FalseNode)) {
            if (this.myExp2 instanceof IdNode) {
                Sym sym2 = ((IdNode) this.myExp2).sym();
                if (!(sym2.getType().isLogicalType())) {
                    ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                            "Logical operator used with non-logical operand");
                }
            } else {
                ErrMsg.fatal((this.myExp2).lineNum(), (this.myExp2).charNum(),
                        "Logical operator used with non-logical operand");
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" | ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
