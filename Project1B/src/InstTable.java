import java.util.*;
import java.io.*;
import java.nio.file.Path;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {
		File file = new File("src\\"+fileName);
		String line;
		try {
			//�� �پ� �о� ���Ͽ� �����Ѵ�.
			BufferedReader br = new BufferedReader(new FileReader(file));
			while((line = br.readLine()) != null)
			{
				//'\t'�� �������� ��ūȭ �Ѵ�.
				instMap.put(line.split("\t")[0], new Instruction(line));
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	//get, set, search ���� �Լ��� ���� ����
	
	//opcode�� �ִ� ��ɾ��ִ��� Ȯ���Ѵ�.
	public boolean isOperation(String str) {
		if(instMap.get(str) != null)
			return true;
		return false;
	}
	
	//Format�� ������ش�.
	public int getForMat(String str) {
		return instMap.get(str).format;
	}
	
	//Opcode�� ������ش�.
	public int getOpcode(String str) {
		try {
			return instMap.get(str).opcode;
		}
		catch(Exception e) {
			return -1;
		}
	}
	
}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/* 
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;
	String instruction;
	int opcode;
	int numberOfOperand;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		String[] tok = line.split("\t");
		instruction = tok[0];
		numberOfOperand = Integer.parseInt(tok[1]);
		if("3/4".equals(tok[2]))
			format = 3;
		else
			format = Integer.parseInt(tok[2]);
		opcode = Integer.parseInt(tok[3], 16);
	}
	//�� �� �Լ� ���� ����
	@Override
	public String toString() {
		return "|"+instruction+"|"+numberOfOperand+"|"+format+'|'+opcode+"|"; 
	}
}
