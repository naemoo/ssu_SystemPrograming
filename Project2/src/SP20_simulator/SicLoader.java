package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import com.sun.jndi.url.rmi.rmiURLContext;
import com.sun.org.apache.bcel.internal.classfile.LineNumber;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	String startObjAddr; //Object에서 시작 주소 저장
	int startAddr; //시작 메모리 주소
	int programLen; // program Length를 저장
	String mainName; // ProgName을 저장
	
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		try {
			rMgr.memIdx = programLen;
			programLen = 0;
			BufferedReader br = new BufferedReader(new FileReader(objectCode));
			pass1(br);
			br = new BufferedReader(new FileReader(objectCode));
			pass2(br);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	};
	
	// 'T','D' 중심으로 읽어 ESTAB을 완성한다.
	private void pass1(BufferedReader br) throws IOException{
		rMgr.symtabList = new SymbolTable();
		String sectName = null;
		String line;
		int c_len = 0;//control Section 길이
		while((line= br.readLine())!= null) {
			if(line.charAt(0) == 'H') {
				String name = line.substring(1,7).trim();
				sectName = name;
				String locctr = line.substring(7,13).trim();
				String len = line.substring(13).trim();
				c_len = Integer.parseInt(len,16);
				try {
					rMgr.symtabList.putSymbol(name,rMgr.memIdx+Integer.parseInt(locctr,16)
						+programLen,Integer.parseInt(len,16));
				}
				catch (SYMTABDuplicateError e) {
				}
				programLen += Integer.parseInt(len,16); 
			}
			else if (line.charAt(0) == 'D') {
				for(int i = 1 ; i < line.length();i+=12) {
					String name = line.substring(i,i+6).trim();
					String locctr = line.substring(i+6,i+12).trim();
					try {
						rMgr.symtabList.putSymbol(name, Integer.parseInt(locctr,16) + 
							programLen	+ rMgr.memIdx - c_len, 0);
					}
					catch (SYMTABDuplicateError e) {
					} 
				}
			}
			else if (line.charAt(0) == 'E') {
				if(line.length()>1) {
					mainName = sectName;
					startObjAddr = line.substring(1,7).trim(); 
					startAddr = Integer.parseInt(startObjAddr,16) + rMgr.memIdx; 
				}
			}
		}
	}
	
	// Pass2 진행 -> 메모리에 로딩
	private void pass2(BufferedReader br) throws IOException{
		String line;
		int csaddr = 0;
		while((line= br.readLine())!= null) {
			if(line.charAt(0) =='H') {
				String name = line.substring(1,7).trim();
				csaddr = rMgr.symtabList.search(name);
			}
			else if(line.charAt(0) == 'T') {
				int lineAddr = Integer.parseInt(line.substring(1,7).trim(),16);
				int lineLen = Integer.parseInt(line.substring(7,9).trim(),16);
				char[] data = new char[lineLen];
				//Packing 진행 -> char 2byte를 읽어 1byte에 저장
				for(int i = 0 ; i< lineLen;i++) {
					int beginIdx = 9+i*2;
					String tmp = line.substring(beginIdx,beginIdx+2);
					data[i] = (char)Integer.parseInt(tmp,16);
				}
				rMgr.setMemory(lineAddr+csaddr,data, lineLen);
			}
			else if(line.charAt(0) == 'M') {
				int modAddr = Integer.parseInt(line.substring(1,7),16) + csaddr;
				int bitCnt = Integer.parseInt(line.substring(7,9));
				char op = line.charAt(9);
				String refName = line.substring(10,16).trim();
				// Memory 수정
				// 원래 Data + REF 주소  -> 연산을 위해 int로 만들고 다시 char[]형으로 전환한다.
				int orgData = rMgr.byteToInt(Arrays.copyOfRange(rMgr.memory, modAddr, modAddr+3));
				int modData = rMgr.symtabList.search(refName);
				if(op == '+') {
					modData += orgData;
				}
				else if(op == '-') {
					modData = orgData - modData;
				}
				char[] tmp = rMgr.intToChar(modData,3);
				for(int i = 0 ; i < 3;i++) {
					rMgr.memory[modAddr+i] = tmp[i];
				}
			}
		}
	}

}
