///////////////////////////////////////////////////////////////////////////////
//
// Title:            P1.java
// Files:            Sym.java, DuplicateSymNameException.java,
//                   SymTable.java, EmptySymTableException.java
// Semester:         CS536 Spring 2024
//
// Author:           Leah Gunn
// Email:            lcgunn@wisc.edu
// CS Login:         lgunn
// Lecturer's Name:  Beck Hasti
// Lab Section:      Lec 001
//
public class P1 {
    public static void main(String[] args) {
        // Initializing a table and symbols
        SymTable table = new SymTable();
        Sym one = new Sym("int");

        // Test Sym
        System.out.println("Test Sym");
        String type_test = "int";
        if(one.getType().equals(type_test)){
            System.out.println("**** getType() Successful ****");
        }else{
            System.out.println("getType() method failed.");
        }
        System.out.println("Test toString():");
        System.out.println(one.toString());
        System.out.println("------------------------------");

        // Test SymTable
        System.out.println("Test SymTable");
        // Test 2: addScope()
        System.out.println("**** Manually Test addScope() ****");
        test_addScope(table);

        // Test 3: removeScope()
        if(test_removeScope()){
            System.out.println("**** removeScope() Successful ****");
        }else{
            return;
        }

        // Test 4: addDecl()
        if(test_addDecl(table, "1", one)){
            System.out.println("**** addDecl() Successful ****");
        }else{
            return;
        }

        // Test 5: lookupLocal()
        if(test_lookupLocal("1", one)){
            System.out.println("**** lookupLocal() Successful ****");
        }else{
            return;
        }

        // Test 6: lookupGlobal()
        if(test_lookupGlobal("1", one)){
            System.out.println("**** lookupGlobal() Successful ****");
        }else{
            return;
        }

        // Test 7: print()
        System.out.println("\nTest print():");
        table.print();
        try {
            table.addDecl("2.0", new Sym("double"));
        } catch (DuplicateSymNameException e) {
        } catch (EmptySymTableException e) {}

        table.print();
        table.addScope();
        table.print();

        try{
            while(true){
                table.removeScope();
            }
        }catch(EmptySymTableException e){
            //Do nothing
        }
        table.print();
    }

    /**
     * Helper method for testing addScope()
     * @param table
     */
    public static void test_addScope(SymTable table){
        table.print();
        table.addScope();
        table.print();
    }

    /**
     * Helper method for testing removeScope()
     * @return
     */
    public static boolean test_removeScope(){
        SymTable table = new SymTable();
        // Test 1: Removing a hashmap
        try{
            table.removeScope();
        }catch(EmptySymTableException e){
            System.out.println("removeScope() 1 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 2: Make sure EmptySymTableException gets thrown when the table is empty
        try{
            table.removeScope();
            System.out.println("removeScope() 2 Failed: EmptySymTableException" +
            " not thrown when it was supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            //Do nothing, the exception was supposed to be thrown
        }
        return true;
    }

    /**
     * Helper method for testing addDecl()
     * @param table
     * @param key
     * @param symbol
     * @return
     */
    public static boolean test_addDecl(SymTable table, String key, Sym symbol){
        // Test 1: IllegalArgumentException test name = null
        try{
            table.addDecl(null, symbol);
            System.out.println("addDecl() 1 Failed: IllegalArgumentException" +
            " not thrown when it was supposed to be.");
            return false;
        }catch(IllegalArgumentException e){
            // Do nothing
        }catch(DuplicateSymNameException e){
            System.out.println("addDecl() 1 Failed: DuplicateSymNameException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            System.err.println("addDecl() 1 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 1.2: IllegalArgumentException test symbol = null
        try{
            table.addDecl(key, null);
            System.out.println("addDecl() 1.2 Failed: IllegalArgumentException" +
            " not thrown when it was supposed to be.");
            return false;
        }catch(IllegalArgumentException e){
            // Do nothing
        }catch(DuplicateSymNameException e){
            System.out.println("addDecl() 1.2 Failed: DuplicateSymNameException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            System.out.println("addDecl() 1.2 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 2: Adding a symbol to the hashmap
        try{
            table.addDecl(key, symbol);
            if(table.lookupLocal(key) != symbol){
                System.out.println("addDecl() 2 Failed:" +
                " key not added to first hashmap.");
                return false;
            }
        }catch(IllegalArgumentException e){
            System.out.println("addDecl() 2 Failed: IllegalArgumentException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(DuplicateSymNameException e){
            System.out.println("addDecl() 2 Failed: DuplicateSymNameException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            System.out.println("Test 2 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 3: Adding the same key again
        try{
            table.addDecl(key, symbol);
        }catch(IllegalArgumentException e){
            System.out.println("addDecl() 3 Failed: IllegalArgumentException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(DuplicateSymNameException e){
            // Do nothing
        }catch(EmptySymTableException e){
            System.out.println("addDecl() 3 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 4: Adding a symbol to an empty table
        SymTable table2 = new SymTable();
        // Empty out a table
        try{
            table2.removeScope();
        }catch(EmptySymTableException e){
            // Do nothing
        }
        // Try adding a key to an empty table
        try{
            table2.addDecl(key, symbol);
        }catch(IllegalArgumentException e){
            System.out.println("addDecl() 4 Failed: IllegalArgumentException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(DuplicateSymNameException e){
            System.out.println("addDecl() 4 Failed: DuplicateSymNameException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            // Do nothing
        }

        // Success!
        return true;
    }

    /**
     * Helper method for testing lookupLocal()
     * @param key
     * @param symbol
     * @return
     */
    public static boolean test_lookupLocal(String key, Sym symbol){
        SymTable table = new SymTable();
        // Test 1: lookupLocal() should return null since key is not in hashmap
        try{
            if(table.lookupLocal(key) != null){
                System.out.println("lookupLocal() 1 Failed:" +
                " lookupLocal() should have returned null");
                return false;
            }
        }catch(EmptySymTableException e){
            System.out.println("lookupLocal() 1 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be");
            return false;
        }
        
        // Add key to table
        try{
            table.addDecl(key, symbol);
        }catch(IllegalArgumentException e){
            System.out.println("Failed: IllegalArgumentException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(DuplicateSymNameException e){
            System.out.println("Failed: DuplicateSymNameException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            System.out.println("Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        } 
        
        // Test 2: lookupLocal() should return key
        try{
            if(table.lookupLocal(key) == symbol){
                //Do nothing
            }else{
                System.out.println("lookupLocal() 2 Failed:" +
                " lookupLocal() should have returned symbol");
                return false;
            }
        }catch(EmptySymTableException e){
            System.out.println("lookupLocal() 2 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be");
            return false;
        }

        // add scope
        table.addScope();

        // Test 3: lookupLocal() should return null
        try{
            if(table.lookupLocal(key) == null){
                //Do nothing
            }else{
                System.out.println("lookupLocal() 3 Failed:" +
                " lookupLocal() should have returned null");
                return false;
            }
        }catch(EmptySymTableException e){
            System.out.println("lookupLocal() 3 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be");
            return false;
        }

        // remove scope
        try{
            table.removeScope();
            table.removeScope();
        }catch(EmptySymTableException e){
            System.out.println("Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 4: lookupLocal successfully throws exception when table is empty
        try{
            table.lookupLocal(key);
            System.out.println("lookupLocal() 4 Failed: EmptySymTableException" +
            " not thrown when it was supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            //Do nothing, the exception was supposed to be thrown
        }

        //Success!
        return true;
    }

    /**
     * Helper method to test lookupGlobal()
     * @param key
     * @param symbol
     * @return
     */
    public static boolean test_lookupGlobal(String key, Sym symbol){
        SymTable table = new SymTable();
        // Test 1: lookupGlobal() should return null since key is not in hashmap
        try{
            if(table.lookupGlobal(key) != null){
                System.out.println("lookupGlobal() 1 Failed:" +
                " lookupGlobal() should have returned null");
                return false;
            }
        }catch(EmptySymTableException e){
            System.out.println("lookupGlobal() 1 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be");
            return false;
        }
        
        // Add key to table
        try{
            table.addDecl(key, symbol);
        }catch(IllegalArgumentException e){
            System.out.println("lookupGlobal() Failed: IllegalArgumentException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(DuplicateSymNameException e){
            System.out.println("lookupGlobal() Failed: DuplicateSymNameException" +
            " thrown when it was not supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            System.out.println("lookupGlobal() Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        } 

        // Add another hashmap so its clear lookupGlobal searched outside the first hashmap
        table.addScope();
        
        // Test 2: lookupGlobal() should return key
        try{
            if(table.lookupGlobal(key) == symbol){
                //Do nothing
            }else{
                System.out.println("lookupGlobal() 2 Failed:" +
                " lookupGlobal() should have returned symbol");
                return false;
            }
        }catch(EmptySymTableException e){
            System.out.println("lookupGlobal() 2 Failed: EmptySymTableException" +
            " thrown when it was not supposed to be");
            return false;
        }

        // remove scope
        try{
            table.removeScope();
            table.removeScope();
        }catch(EmptySymTableException e){
            System.out.println("lookupGlobal() Failed: EmptySymTableException" +
            " thrown when it was not supposed to be.");
            return false;
        }

        // Test 3: lookupGlobal successfully throws exception when table is empty
        try{
            table.lookupGlobal(key);
            System.out.println("lookupGlobal() 3 Failed: EmptySymTableException" +
            " not thrown when it was supposed to be.");
            return false;
        }catch(EmptySymTableException e){
            //Do nothing, the exception was supposed to be thrown
        }

        // Success!
        return true;
    }
}
