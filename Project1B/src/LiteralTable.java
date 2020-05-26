import java.util.ArrayList;

/**
 * literal�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LiteralTable {
	ArrayList<String> literalList;
	ArrayList<Integer> locationList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	public LiteralTable() {
		literalList = new ArrayList<>();
		locationList = new ArrayList<>();
	}
	/**
	 * ���ο� Literal�� table�� �߰��Ѵ�.
	 * @param literal : ���� �߰��Ǵ� literal�� label
	 * @param location : �ش� literal�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� literal�� putLiteral�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifyLiteral()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putLiteral(String literal, int location) throws literalDuplicationError{
		if(literalList.contains(literal))
			throw new literalDuplicationError();
		else {
			literalList.add(literal);
			locationList.add(location);
		}
	}
	
	
	/**
	 * ������ �����ϴ� literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param literal : ������ ���ϴ� literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyLiteral(String literal, int newLocation) {
		int idx = literalList.indexOf(literal);
		locationList.set(idx, newLocation);
	}
	public void modifyLiteral(String literal1,String literal2) {
		int idx = literalList.indexOf(literal1);
		literalList.set(idx, literal2);
	}
	
	/**
	 * ���ڷ� ���޵� literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param literal : �˻��� ���ϴ� literal�� label
	 * @return literal�� ������ �ִ� �ּҰ�. �ش� literal�� ���� ��� -1 ����
	 */
	public int search(String literal) {
		int idx = literalList.indexOf(literal);
		if(idx >=0)
			return locationList.get(idx);
		return -1;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i<literalList.size();i++) {
			sb.append(literalList.get(i)+"\t"+
					Integer.toHexString(locationList.get(i)).toUpperCase()+"\n");
		}
		return sb.toString();
	}
}

class literalDuplicationError extends Exception{
	public literalDuplicationError() {
		super("�ش� Literal�� Literal Table�� �����մϴ�.");
	}
}


