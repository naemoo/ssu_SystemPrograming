package SP20_simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;




/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * ����̽��� ���� ����� ��ġ���� �ǹ� ������ ���⼭�� ���Ϸ� ����̽��� ��ü�Ѵ�.<br>
	 * ��, 'F1'�̶�� ����̽��� 'F1'�̶�� �̸��� ������ �ǹ��Ѵ�. <br>
	 * deviceManager�� ����̽��� �̸��� �Է¹޾��� �� �ش� �̸��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	HashMap<String,Resource> deviceManager = new HashMap<>();
	char[] memory = new char[65536]; // String���� �����ؼ� ����Ͽ��� ������.
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList;
	// �̿ܿ��� �ʿ��� ���� �����ؼ� ����� ��.
	int memIdx = 0; //�޸� ��� ���� ��ġ
	int targetAddress = 0; //TargetAddress
	String usingDevice = new String();
	
	boolean end =false;

	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){
		//register �� �޸� �ʱ�ȭ �ʱ�ȭ
		Arrays.fill(register, 0);
		deviceManager = new HashMap<>();
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 */
	public void closeDevice() {
		for(String k:deviceManager.keySet()) 
			deviceManager.get(k).close();
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		// �ش� Device�� ����� Stream ���� �� CC = 0 ����
		if(!deviceManager.containsKey(devName)) {
			try {
				deviceManager.put(devName, new Resource(devName));
				usingDevice = devName;
			}
			catch(IOException ie) {
				// ������� ���ϴ� ��Ȳ�̶�� 0  ����
				register[9] = 0;
			}
			register[9] = 1;
			usingDevice = devName;
		}
		// CC�� 0�� �ƴ� �� ����
		else {
			register[9] = 1;
			usingDevice = devName;
		}
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������ �׷��� ������ null
	 */
	public char[] readDevice(String devName, int num){
		Resource res;
		char[] ans = null;
		if(deviceManager.containsKey(devName)) {
			res = deviceManager.get(devName);
			ans = new char[num];
			for(int i = 0 ; i < num;i++) {
				ans[i] = res.read();
			}
		}
		return ans;
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
	 */
	public void writeDevice(String devName, char[] data, int num){
		if(deviceManager.containsKey(devName)) {
			Resource res = deviceManager.get(devName);
			for(int i = 0 ; i < num;i++)
				res.write(data[i]);
		}
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public char[] getMemory(int location, int num){
		return Arrays.copyOfRange(memory, location, location+num);
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, char[] data, int num){
		for(int i = 0 ; i < num;i++) {
			memory[i+locate] = data[i];
		}
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		if(regNum == 6)
			return (int)register_F;
		return register[regNum];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		if(regNum == 6)
			register_F = value;
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data , num : Byte ��
	 * @return numũ���� char�迭
	 */
	public char[] intToChar(int data,int num){
		char[] charArr = new char[num];
		for(int i = 0 ; i < num;i++) {
			charArr[num-1-i] = (char)(data & 0xFF);
			data = data>>8;
		}
		return charArr;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data 
	 * @return
	 */
	public int byteToInt(char[] data){
		int ans = 0;
		for(char c:data)
			ans = (ans<<8) + c;
		return ans;
	}
	/**
	 * char[]������ String
	 * @param data 
	 * @return
	 */
	public String byteToString(char[] data){
		int ans = 0;
		for(char c:data)
			ans = (ans<<8) + c;
		return String.format("%0"+data.length*2+"X", ans);
	}
}

class Resource{
	Reader in;
	Writer out;
	public Resource(String  devName) throws IOException{
		String path = System.getProperty("user.dir");
		File src = new File(path+ "/" +devName+".txt");
		out = new FileWriter(src,true);
		in = new FileReader(src);
	}
	public void close() {
		try {
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	public char read() {
		try {
			char ans = (char)in.read();
			if(ans == (char)-1)
				return 0;
			return ans;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return (char)0;
	}
	public void write(char data) {
		try {
			out.write(data);
			out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}



