public class Sym {
	private String type;
	private SymTable table;
	
	public Sym(String type) {
		this.type = type;
	}
	
	public Sym(String type, SymTable T){
		this.type = type;
		this.table = T;
	}
	
	public String getType() {
		return type;
	}

	public SymTable getTable(){
		return table;
	}

	public String toString() {
		return type;
	}
}