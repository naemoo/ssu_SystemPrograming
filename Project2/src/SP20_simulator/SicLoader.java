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
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. 
 * <br><br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;
	String startObjAddr; //Object���� ���� �ּ� ����
	int startAddr; //���� �޸� �ּ�
	int programLen; // program Length�� ����
	String mainName; // ProgName�� ����
	
	public SicLoader(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
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
	
	// 'T','D' �߽����� �о� ESTAB�� �ϼ��Ѵ�.
	private void pass1(BufferedReader br) throws IOException{
		rMgr.symtabList = new SymbolTable();
		String sectName = null;
		String line;
		int c_len = 0;//control Section ����
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
	
	// Pass2 ���� -> �޸𸮿� �ε�
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
				//Packing ���� -> char 2byte�� �о� 1byte�� ����
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
				// Memory ����
				// ���� Data + REF �ּ�  -> ������ ���� int�� ����� �ٽ� char[]������ ��ȯ�Ѵ�.
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
