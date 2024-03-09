///////////////////////////////////////////////////////////////////////////////
//
// Title:            SymTable.java
// Files:            DuplicateSymNameException.java,
//                   EmptySymTableException.java
// Semester:         CS536 Spring 2024
//
// Author:           Leah Gunn
// Email:            lcgunn@wisc.edu
// CS Login:         lgunn
// Lecturer's Name:  Beck Hasti
// Lab Section:      Lec 001
//

import java.util.*;

public class SymTable {
    ArrayList<HashMap<String, Sym>> symTable = 
    new ArrayList<HashMap<String, Sym>>();

    /**
     * Constructor: initialize the SymTable's List field to contain a
     * single, empty HashMap
     */
    public SymTable() {
        symTable.add(new HashMap<String, Sym>());
    }

    /**
     * If this SymTable's list is empty, throw an EmptySymTableException.
     * If either name or sym (or both) is null, throw an IllegalArgumentException.
     * If the first HashMap in the list already contains the given name as a key,
     * throw a DuplicateSymNameException.
     * Otherwise, add the given name and sym to the first HashMap in the list.
     * 
     * @param name
     * @param sym
     * @throws DuplicateSymNameException
     * @throws EmptySymTableException
     */
    void addDecl(String name, Sym sym) 
    throws DuplicateSymNameException, EmptySymTableException {
        // Throw IllegalArgumentException if either parameter is null
        if(name == null || sym == null){
            throw new IllegalArgumentException();
        }

        // Throw EmptySymTableException if table is empty
        if (this.symTable.isEmpty()) {
            throw new EmptySymTableException();
        }

        // Throw DuplicateSymNameException if there is already a 
        // key of that name in the first hashmap
        if(lookupLocal(name) != null){
            throw new DuplicateSymNameException();
        }
        
        // Add name and symbol to first hashmap
        this.symTable.get(0).put(name, sym);
    }

    /**
     * Add a new, empty HashMap to the front of the list.
     */
    void addScope() {
        symTable.add(0, new HashMap<String, Sym>());
    }

    /**
     * If this SymTable's list is empty, throw an EmptySymTableException.
     * Otherwise, if the first HashMap in the list contains name as a key,
     * return the associated Sym; otherwise, return null.
     * 
     * @param name
     * @return
     * @throws EmptySymTableException
     */
    Sym lookupLocal(String name) throws EmptySymTableException {
        // Throw EmptySymTableException if table is empty
        if (this.symTable.isEmpty()) {
            throw new EmptySymTableException();
        }
        // Access front hashmap
        for (String key : symTable.get(0).keySet()) {
            if(key.equals(name)){   // capitals matter
                return symTable.get(0).get(key);
            }
        }
        return null;
    }

    /**
     * If this SymTable's list is empty, throw an EmptySymTableException.
     * Otherwise, if any HashMap in the list contains name as a key,
     * return the first associated Sym (i.e., the one from the HashMap that
     * is closest to the front of the list); otherwise, return null.
     * 
     * @param name
     * @return
     * @throws EmptySymTableException
     */
    Sym lookupGlobal(String name) throws EmptySymTableException {
        // Throw EmptySymTableException if table is empty
        if (this.symTable.isEmpty()) {
            throw new EmptySymTableException();
        }
        // Loop through list to each hashmap
        for(int i = 0; i < symTable.size(); i++){
            for (String key : symTable.get(i).keySet()) {
                if(key.equals(name)){   // capitals matter
                    return symTable.get(i).get(key);
                }
            }
        }
        return null;
    }

    /**
     * If this SymTable's list is empty, throw an EmptySymTableException.
     * Otherwise, remove the HashMap from the front of the list. To clarify,
     * throw an exception only if before attempting to remove, the list is
     * empty (i.e., there are no HashMaps to remove).
     * 
     * @throws EmptySymTableException
     */
    void removeScope() throws EmptySymTableException {
        // Throw EmptySymTableException if table is empty
        if (this.symTable.isEmpty()) {
            throw new EmptySymTableException();
        }
        // Remove HashMap from the front of the list
        symTable.remove(0);    //Uses the index for removal

    }

    /**
     * This method is for debugging. It prints each HashMap, M,
     * in the list
     */
    void print() {
        System.out.print("\n++++ SYMBOL TABLE\n");
        // Print each HashMap in list
        if (symTable.isEmpty()) {
            //Do nothing
        } else {
            for (int M = 0; M < symTable.size(); M++) {
                // Print toString() of each HashMap
                System.out.println(symTable.get(M).toString());
            }
        }
        System.out.print("\n++++ END TABLE\n");
    }
}
