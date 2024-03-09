///////////////////////////////////////////////////////////////////////////////
//
// Title:            Sym.java
// Files:            N/A
// Semester:         CS536 Spring 2024
//
// Author:           Leah Gunn
// Email:            lcgunn@wisc.edu
// CS Login:         lgunn
// Lecturer's Name:  Beck Hasti
// Lab Section:      Lec 001
//
public class Sym{
    String type;

    /**
     * Constructor: Initializes Sym to have a type given by 
     * the parameter
     * @param type
     */
    public Sym(String type){
        this.type = type;
    }

    /**
     * Returns the Sym's type
     * @return
     */
    public String getType(){
        return this.type;
    }
    
    /**
     * Return this Sym's type. (This method will be changed later
     *  when more information is stored in a Sym.)
     * @return
     */
    public String toString(){
        return this.type;
    }
}
