package SP20_simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;




/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	/**
	 * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다.<br>
	 * 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의 파일을 의미한다. <br>
	 * deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	HashMap<String,Resource> deviceManager = new HashMap<>();
	char[] memory = new char[65536]; // String으로 수정해서 사용하여도 무방함.
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList;
	// 이외에도 필요한 변수 선언해서 사용할 것.
	int memIdx = 0; //메모리 사용 가능 위치
	int targetAddress = 0; //TargetAddress
	String usingDevice = new String();
	
	boolean end =false;

	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		//register 및 메모리 초기화 초기화
		Arrays.fill(register, 0);
		deviceManager = new HashMap<>();
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 */
	public void closeDevice() {
		for(String k:deviceManager.keySet()) 
			deviceManager.get(k).close();
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 */
	public void testDevice(String devName) {
		// 해당 Device에 입출력 Stream 연결 및 CC = 0 대입
		if(!deviceManager.containsKey(devName)) {
			try {
				deviceManager.put(devName, new Resource(devName));
				usingDevice = devName;
			}
			catch(IOException ie) {
				// 사용하지 못하는 상황이라면 0  대입
				register[9] = 0;
			}
			register[9] = 1;
			usingDevice = devName;
		}
		// CC에 0이 아닌 수 저장
		else {
			register[9] = 1;
			usingDevice = devName;
		}
	}

	/**
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터 그렇지 않으면 null
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
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
	 */
	public void writeDevice(String devName, char[] data, int num){
		if(deviceManager.containsKey(devName)) {
			Resource res = deviceManager.get(devName);
			for(int i = 0 ; i < num;i++)
				res.write(data[i]);
		}
	}
	
	/**
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public char[] getMemory(int location, int num){
		return Arrays.copyOfRange(memory, location, location+num);
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, char[] data, int num){
		for(int i = 0 ; i < num;i++) {
			memory[i+locate] = data[i];
		}
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		if(regNum == 6)
			return (int)register_F;
		return register[regNum];
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		if(regNum == 6)
			register_F = value;
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
	 * @param data , num : Byte 수
	 * @return num크기의 char배열
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
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
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
	 * char[]형에서 String
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



