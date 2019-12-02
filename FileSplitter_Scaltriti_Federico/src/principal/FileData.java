package principal;

public class FileData {
	private String name, path, action;
	private int dim;
	
	public FileData(String name, String path, String action, int dim) {
		super();
		this.name = name;
		this.path = path;
		this.action = action;
		this.dim = dim;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getDim() {
		return dim;
	}

	public void setDim(int dim) {
		this.dim = dim;
	}
		
}
