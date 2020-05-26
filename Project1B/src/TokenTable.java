import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	//main 프로그램일 경우 true
	boolean isMain;
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/** 해당 Control Section에 EXTREF 저장 **/
	List<String> refList = new LinkedList<>();
	
	/**
	 * 초기화하면서 symTable과 literalTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab,LiteralTable literalTab ,InstTable instTab) {
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		this.tokenList = new ArrayList<>(); 
	}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		Token t = new Token(line);
		int locctr = 0;
		if(tokenList.size()!=0) 
			locctr = getToken(tokenList.size()-1).location;
		tokenList.add(t);
		
		if(t.operator.equals("CSECT")||t.operator.equals("START"))
			t.record = 'H';
		
		/*
		 * SYMTAB 채우기 
		 */
		if(t.label!= null){
			try {
				symTab.putSymbol(t.label, locctr);
			}
			catch (SYMTABDuplicateError e) {
			}
		}
		
		/*
		 * LITTAB 채우기
		 */
		if(t.operand != null&&t.operand[0].charAt(0)=='=') {
			String operand = t.operand[0].substring(1);
			try {//위치 최종 아님 LTORG에서 수정
				literalTab.putLiteral(operand, locctr);
			}
			catch (literalDuplicationError e) {
			}
		}
		
		/*
		 * LOCCTR 증가 시키기 
		 */
		//1,2,3형식 명령어 
		if(instTab.isOperation(t.operator)) {
			String operator = t.operator;
			locctr += instTab.getForMat(operator);
			t.byteSize = instTab.getForMat(operator);
			t.record ='T';
			//RSUB가 아닐 경우 
			if(!(operator.equals("RSUB"))) {
				t.setFlag(TokenTable.pFlag, 1);
				if(t.operand[0].contains("#")) {//#일경우 i =1
					t.setFlag(TokenTable.iFlag, 1);
					if(isNumber(t.operand[0].substring(1)))
						t.setFlag(TokenTable.pFlag, 0);
					t.operand[0] = t.operand[0].substring(1);
				}
				else if(t.operand[0].contains("@")) {//#일경우 n =1
					t.setFlag(TokenTable.nFlag, 1);
					if(isNumber(t.operand[0].substring(1)))
						t.setFlag(TokenTable.pFlag, 0);
					t.operand[0] = t.operand[0].substring(1);
				}
				//그렇지 않은 경우 n =1, i = 1 
				else {
					t.setFlag(TokenTable.iFlag, 1);
					t.setFlag(TokenTable.nFlag, 1);
				}
				//Xbit = 1 인 경우
				if(t.operand[1] != null&&t.operand[1].equals("X"))
					t.setFlag(TokenTable.xFlag, 1);
			}//RSUB일 경우 n =1,i =1
			else {
				t.setFlag(TokenTable.iFlag, 1);
				t.setFlag(TokenTable.nFlag, 1);
			}
		}
		//LTORG일 경우 
		else if(t.operator.equals("LTORG")||t.operator.equals("END")) {
			//literal 변경 시키기 + locctr 증가 시키기
			//테이블을 탐색한다.
			t.record ='T';
			Iterator<String> itr = literalTab.literalList.iterator();
			while(itr.hasNext()) {
				String element = itr.next();
				if(element.charAt(0)=='C') {
					int idx = element.lastIndexOf("'");
					int len = idx-2;
					String new_element = element.substring(2,idx);
					literalTab.modifyLiteral(element, locctr);
					literalTab.modifyLiteral(element, new_element);
					locctr += len;
					t.byteSize = len;
				}
				else if(element.charAt(0)=='X') {
					int idx = element.lastIndexOf("'");
					int len = idx-2;
					String new_element = element.substring(2,idx);
					literalTab.modifyLiteral(element, locctr);
					literalTab.modifyLiteral(element, new_element);
					locctr += len/2;
					t.byteSize = len/2;
				}
			}
		}
		//4형식일 경우
		else if(t.operator.charAt(0) == '+') {
			String operator = t.operator.substring(1);
			locctr += 4;
			t.record ='T';
			//4형식일 경우 n = 1, i =1, e =1
			t.setFlag(TokenTable.eFlag, 1);
			t.setFlag(TokenTable.nFlag, 1);
			t.setFlag(TokenTable.iFlag, 1);
			t.byteSize = 4;
			//XReg를 사용하는 경우
			if(t.operand[1]!=null && t.operand[1].equals("X")){
				t.setFlag(TokenTable.xFlag, 1);
			}
		}
		//RESW 시
		else if(t.operator.equals("RESW")) {
			locctr += Integer.parseInt(t.operand[0])*3;
			t.byteSize = Integer.parseInt(t.operand[0])*3;
		}//RESB 시
		else if(t.operator.equals("RESB")) {
			locctr += Integer.parseInt(t.operand[0]);
			t.byteSize = Integer.parseInt(t.operand[0]);
		}//WORD 시
		else if(t.operator.equals("WORD")) {
			locctr += 3;
			t.byteSize = 3;
			t.record ='T';
		}//BYTE 시
		else if(t.operator.equals("BYTE")) {
			String operand = t.operand[0];
			t.record ='T';
			int len = operand.lastIndexOf("'") - 2;
			if(operand.charAt(0)=='C') {
				locctr += len;
				t.byteSize = len;
			}
			else if(operand.charAt(0)=='X') {
				locctr += len/2;
				t.byteSize = len/2;
			}
		}//EXTDEF 시
		else if(t.operator.equals("EXTDEF")) {
			t.record = 'D';
		}//EXTREF 시
		else if(t.operator.equals("EXTREF")) {
			t.record = 'R';
			for(String str:t.operand) {
				if(str!=null) {
					refList.add(str);
				}
			}
		}
		else if(t.operator.equals("START")) {
			isMain = true;
		}
		
		//다음 Locctr을 저장
		t.location = locctr;
		
		/*
		 * EQU일 경우
		 */
		if(t.operator.equals("EQU")) {
			String operand = t.operand[0];
			//*가 아닐경우
			if(!operand.equals("*")) {
				//연산이 있다면 +-로 나눈다.
				StringTokenizer st = new StringTokenizer(operand,"+-");
				String op = st.nextToken();
				int address = symTab.search(op);
				while(st.hasMoreTokens()) {
					op = st.nextToken();
					int idx = operand.indexOf(op);
					if(operand.charAt(idx-1)=='+')
						address += symTab.search(op);
					else if(operand.charAt(idx-1)=='-')
						address -= symTab.search(op);
					operand = operand.substring(idx);
				}
				symTab.modifySymbol(t.label, address);
				t.location = address;
			}
		}
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		Token t = getToken(index);
		int i = 0;
		
		/*
		 * ByteSize가 유동적인 명령어 처리
		 */
		if(instTab.getOpcode(t.operator)<0) {
			if(t.operator.equals("BYTE")) {
				//EX) X'F1' C'EOF'
				if(t.operand[0].charAt(0)=='X') {
					i = Integer.parseInt(t.operand[0].substring(2,4),16);
				}
				else if(t.operand[0].charAt(0)=='C') {
					String op = t.operand[0].substring(2);
					for(int k = 0;k<t.byteSize;k++) 
						i = (i<<8) + op.charAt(k);
				}
				t.objectCode = String.format("%0"+t.byteSize*2+"X",i);
				return;
			}
			else if(t.operator.equals("WORD")) {
				//연산이 있을 경우
				if(t.operand[0].contains("+")||t.operand[0].contains("-")) {
					StringTokenizer st = new StringTokenizer(t.operand[0],"+-");
					//첫 피연산자는 무조건 양수
					//operand가 Symbol,Literal,Reference인지 확인
					String op = st.nextToken();
					if(symTab.search(op) >=0 ) {
						i = symTab.search(op);
					}
					else if(literalTab.search(op)>=0) {
						i = literalTab.search(op);
					}
					else if(refList.indexOf(op) >=0) {
						i = 0;
						t.memoCnt++;
					}
					while(st.hasMoreTokens()) {
						op = st.nextToken();
						int idx = t.operand[0].indexOf(op);
						char arthimetic = t.operand[0].charAt(idx-1);
						if(arthimetic == '+') {
							if(symTab.search(op) >=0 ) {
								i += symTab.search(op);
							}
							else if(literalTab.search(op)>=0) {
								i += literalTab.search(op);
							}
							else if(refList.indexOf(op) >=0) {
								i += 0;
								t.memoCnt++;
							}
						}
						else if(arthimetic == '-') {
							if(symTab.search(op) >=0 ) {
								i -= symTab.search(op);
							}
							else if(literalTab.search(op)>=0) {
								i -= literalTab.search(op);
							}
							else if(refList.indexOf(op) >=0) {
								i -= 0;
								t.memoCnt++;
							}
						}
					}
				}
				//EX) WORD 4096
				else if(isNumber(t.operand[0])){
					i = Integer.parseInt(t.operand[0]);
				}
				t.objectCode = String.format("%0"+t.byteSize*2+"X",i);
				return;
			}
			else if(t.operator.equals("LTORG")||t.operator.equals("END")) {
				for(String lit:literalTab.literalList) {
					//ByteSize와 literal 길이와 다를 경우 : X EX) F1
					if(t.byteSize != lit.length()) {
						i = Integer.parseInt(lit);
					}
					//그렇지 않으면 : C ex)EOF
					else {
						for(char c:lit.toCharArray())
							i = (i<<8)+c;
					}
				}
				t.objectCode = String.format("%0"+t.byteSize*2+"X",i);
				return;
			}
			else if(t.operator.equals("RESW")||t.operator.equals("RESB")||
					t.operator.equals("EQU"))
				return;
		}
		switch (t.byteSize) {
		case 0:
			return;
		//1형식일때
		case 1:
			i = instTab.getOpcode(t.operator); 
			break;
		//2형식 일때
		case 2:
			i = (instTab.getOpcode(t.operator)<<8)|(getRegister(t.operand[0])<<4)|
			(getRegister(t.operand[1]));
			break;
		//(opcode << 16) | (nix << 12) | (0xFFF&adr)
		//3형식일 때
		case 3:
			int adr = 0;
			//operand가 존재하지 않을 시 0으로 설정
			try {
				adr = symTab.search(t.operand[0]);
				if(symTab.search(t.operand[0])>=0)
					adr = symTab.search(t.operand[0]) - t.location;
				else if(t.operand[0].charAt(0)=='=') {
					String op = t.operand[0].substring(t.operand[0].indexOf("'")+1,
							t.operand[0].lastIndexOf("'"));
					adr = literalTab.search(op)-t.location;
				}
				//operand가 숫자라면 #인지 @인지 판별
				else if(isNumber(t.operand[0])) {
					adr = Integer.parseInt(t.operand[0]);
				}
			}
			catch(NullPointerException e) {
			}
			i = (instTab.getOpcode(t.operator)<<16|(t.nixbpe<<12)|(0xFFF&adr));
			break;
		//4형식일 때
		case 4:
			String op = t.operator.substring(1);
			adr = 0;
			if(symTab.search(t.operand[0])>=0)
				adr = symTab.search(t.operand[0]);
			else if(t.operand[0].charAt(0)=='=') 
				adr = literalTab.search(t.operand[0]);
			else if(refList.indexOf(t.operand[0])>=0)
				adr = 0;
			i = (instTab.getOpcode(op)<<24|(t.nixbpe<<20)|(adr));
			t.memoCnt++;
			break;
		}
		t.objectCode = String.format("%0"+t.byteSize*2+"X",i);
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
	/** 
	 * 2형식일 때 reg 넣으면 해당 reg번호 Mapping
	 * @param : Register 
	 * @return : Number of Register
	 */
	public char getRegister(String reg) {
		if(reg == null)
			return 0;
		else {
			switch(reg) {
			case "A":
				return 0;
			case "X":
				return 1;
			case "L":
				return 2;
			case "B":
				return 3;
			case "S":
				return 4;
			case "T":
				return 5;
			case "F":
				return 6;
			}
		}
		return 100;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int num = 0;
		for(Token t:tokenList) {
			sb.append(num +" "+ t.record +" "+
					Integer.toHexString(t.location).toUpperCase()+" : "
					+t.objectCode+"\n");
			num++;
		}
		return sb.toString();
	}
	
	/** 
	 * 주어진 String이 숫자인지 판별
	 * @param : String 
	 * @return : 숫자면 true 숫자가 아닌 문자라면 false
	 */
	public boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	//object Program 생성 시 사용되는 변수들
	char record;
	int memoCnt;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
		parsing(line);
		nixbpe = 0;
		record = 'E';
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		
		//토큰을 "\t"을 기준으로 나눈다.
		String[] tok = line.split("\t");
		if(!(tok[0].equals(""))) {
			label = tok[0];
		}
		if(!(tok[1].equals(""))) {
			operator = tok[1];
		}
		if(tok.length > 2 &&!(tok[2].equals(""))) {
			operand = new String[TokenTable.MAX_OPERAND];
			int num = 0;
			StringTokenizer st = new StringTokenizer(tok[2],",");
			while(st.hasMoreTokens()) {
				operand[num++] = st.nextToken();
			}
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if(value == 1) 
			nixbpe |= flag;
		else
			nixbpe -= flag;
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	@Override
	public String toString() {
		return label+","+operator;
	}
	
	
}
