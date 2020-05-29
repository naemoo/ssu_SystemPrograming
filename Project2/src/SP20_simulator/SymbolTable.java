package SP20_simulator;
import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	ArrayList<Integer> lengthList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.

	public SymbolTable() {
		symbolList = new ArrayList<>();
		addressList = new ArrayList<>();
		lengthList = new ArrayList<>();
	}
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param address : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location,int progLen) throws SYMTABDuplicateError{
		if(symbolList.contains(symbol))
			throw new SYMTABDuplicateError();
		symbolList.add(symbol);
		addressList.add(location);
		lengthList.add(progLen);
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		int idx = symbolList.indexOf(symbol);
		addressList.set(idx, newLocation);
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int idx = symbolList.indexOf(symbol);
		if(idx >= 0)
			return addressList.get(idx);
		return -1;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i <symbolList.size();i++) {
			sb.append(symbolList.get(i)+","+String.format("%X",addressList.get(i))+","+
					String.format("%X", lengthList.get(i)));
			sb.append("\n");
		}
		return sb.toString();
	}
}

class SYMTABDuplicateError extends Exception{
	public SYMTABDuplicateError() {
		super("���� ������ SYMTABLE�� �ֽ��ϴ�.");
	}
}

