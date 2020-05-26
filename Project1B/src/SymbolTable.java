import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	public SymbolTable() {
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
	}
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location) throws SYMTABDuplicateError{
		if(symbolList.contains(symbol))
			throw new SYMTABDuplicateError();
		symbolList.add(symbol);
		locationList.add(location);
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		int idx = symbolList.indexOf(symbol);
		locationList.set(idx, newLocation);
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int idx = symbolList.indexOf(symbol);
		if(idx > 0)
			return locationList.get(idx);
		return -1;
	}
	//SYMYAB Class toStirng�޼ҵ� �������̵�
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ;i<symbolList.size();i++) {
			sb.append(symbolList.get(i)+"\t"+
					Integer.toHexString(locationList.get(i)).toUpperCase()+"\n");
		}
		return sb.toString();
	}
	
}

class SYMTABDuplicateError extends Exception{
	public SYMTABDuplicateError() {
		super("���� ������ SYMTABLE�� �ֽ��ϴ�.");
	}
}


