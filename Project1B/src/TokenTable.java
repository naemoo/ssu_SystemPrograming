import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	//main ���α׷��� ��� true
	boolean isMain;
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	/** �ش� Control Section�� EXTREF ���� **/
	List<String> refList = new LinkedList<>();
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� literalTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab,LiteralTable literalTab ,InstTable instTab) {
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		this.tokenList = new ArrayList<>(); 
	}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
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
		 * SYMTAB ä��� 
		 */
		if(t.label!= null){
			try {
				symTab.putSymbol(t.label, locctr);
			}
			catch (SYMTABDuplicateError e) {
			}
		}
		
		/*
		 * LITTAB ä���
		 */
		if(t.operand != null&&t.operand[0].charAt(0)=='=') {
			String operand = t.operand[0].substring(1);
			try {//��ġ ���� �ƴ� LTORG���� ����
				literalTab.putLiteral(operand, locctr);
			}
			catch (literalDuplicationError e) {
			}
		}
		
		/*
		 * LOCCTR ���� ��Ű�� 
		 */
		//1,2,3���� ��ɾ� 
		if(instTab.isOperation(t.operator)) {
			String operator = t.operator;
			locctr += instTab.getForMat(operator);
			t.byteSize = instTab.getForMat(operator);
			t.record ='T';
			//RSUB�� �ƴ� ��� 
			if(!(operator.equals("RSUB"))) {
				t.setFlag(TokenTable.pFlag, 1);
				if(t.operand[0].contains("#")) {//#�ϰ�� i =1
					t.setFlag(TokenTable.iFlag, 1);
					if(isNumber(t.operand[0].substring(1)))
						t.setFlag(TokenTable.pFlag, 0);
					t.operand[0] = t.operand[0].substring(1);
				}
				else if(t.operand[0].contains("@")) {//#�ϰ�� n =1
					t.setFlag(TokenTable.nFlag, 1);
					if(isNumber(t.operand[0].substring(1)))
						t.setFlag(TokenTable.pFlag, 0);
					t.operand[0] = t.operand[0].substring(1);
				}
				//�׷��� ���� ��� n =1, i = 1 
				else {
					t.setFlag(TokenTable.iFlag, 1);
					t.setFlag(TokenTable.nFlag, 1);
				}
				//Xbit = 1 �� ���
				if(t.operand[1] != null&&t.operand[1].equals("X"))
					t.setFlag(TokenTable.xFlag, 1);
			}//RSUB�� ��� n =1,i =1
			else {
				t.setFlag(TokenTable.iFlag, 1);
				t.setFlag(TokenTable.nFlag, 1);
			}
		}
		//LTORG�� ��� 
		else if(t.operator.equals("LTORG")||t.operator.equals("END")) {
			//literal ���� ��Ű�� + locctr ���� ��Ű��
			//���̺��� Ž���Ѵ�.
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
		//4������ ���
		else if(t.operator.charAt(0) == '+') {
			String operator = t.operator.substring(1);
			locctr += 4;
			t.record ='T';
			//4������ ��� n = 1, i =1, e =1
			t.setFlag(TokenTable.eFlag, 1);
			t.setFlag(TokenTable.nFlag, 1);
			t.setFlag(TokenTable.iFlag, 1);
			t.byteSize = 4;
			//XReg�� ����ϴ� ���
			if(t.operand[1]!=null && t.operand[1].equals("X")){
				t.setFlag(TokenTable.xFlag, 1);
			}
		}
		//RESW ��
		else if(t.operator.equals("RESW")) {
			locctr += Integer.parseInt(t.operand[0])*3;
			t.byteSize = Integer.parseInt(t.operand[0])*3;
		}//RESB ��
		else if(t.operator.equals("RESB")) {
			locctr += Integer.parseInt(t.operand[0]);
			t.byteSize = Integer.parseInt(t.operand[0]);
		}//WORD ��
		else if(t.operator.equals("WORD")) {
			locctr += 3;
			t.byteSize = 3;
			t.record ='T';
		}//BYTE ��
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
		}//EXTDEF ��
		else if(t.operator.equals("EXTDEF")) {
			t.record = 'D';
		}//EXTREF ��
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
		
		//���� Locctr�� ����
		t.location = locctr;
		
		/*
		 * EQU�� ���
		 */
		if(t.operator.equals("EQU")) {
			String operand = t.operand[0];
			//*�� �ƴҰ��
			if(!operand.equals("*")) {
				//������ �ִٸ� +-�� ������.
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
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		Token t = getToken(index);
		int i = 0;
		
		/*
		 * ByteSize�� �������� ��ɾ� ó��
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
				//������ ���� ���
				if(t.operand[0].contains("+")||t.operand[0].contains("-")) {
					StringTokenizer st = new StringTokenizer(t.operand[0],"+-");
					//ù �ǿ����ڴ� ������ ���
					//operand�� Symbol,Literal,Reference���� Ȯ��
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
					//ByteSize�� literal ���̿� �ٸ� ��� : X EX) F1
					if(t.byteSize != lit.length()) {
						i = Integer.parseInt(lit);
					}
					//�׷��� ������ : C ex)EOF
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
		//1�����϶�
		case 1:
			i = instTab.getOpcode(t.operator); 
			break;
		//2���� �϶�
		case 2:
			i = (instTab.getOpcode(t.operator)<<8)|(getRegister(t.operand[0])<<4)|
			(getRegister(t.operand[1]));
			break;
		//(opcode << 16) | (nix << 12) | (0xFFF&adr)
		//3������ ��
		case 3:
			int adr = 0;
			//operand�� �������� ���� �� 0���� ����
			try {
				adr = symTab.search(t.operand[0]);
				if(symTab.search(t.operand[0])>=0)
					adr = symTab.search(t.operand[0]) - t.location;
				else if(t.operand[0].charAt(0)=='=') {
					String op = t.operand[0].substring(t.operand[0].indexOf("'")+1,
							t.operand[0].lastIndexOf("'"));
					adr = literalTab.search(op)-t.location;
				}
				//operand�� ���ڶ�� #���� @���� �Ǻ�
				else if(isNumber(t.operand[0])) {
					adr = Integer.parseInt(t.operand[0]);
				}
			}
			catch(NullPointerException e) {
			}
			i = (instTab.getOpcode(t.operator)<<16|(t.nixbpe<<12)|(0xFFF&adr));
			break;
		//4������ ��
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
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
	/** 
	 * 2������ �� reg ������ �ش� reg��ȣ Mapping
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
	 * �־��� String�� �������� �Ǻ�
	 * @param : String 
	 * @return : ���ڸ� true ���ڰ� �ƴ� ���ڶ�� false
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
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	//object Program ���� �� ���Ǵ� ������
	char record;
	int memoCnt;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
		parsing(line);
		nixbpe = 0;
		record = 'E';
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		
		//��ū�� "\t"�� �������� ������.
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
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		if(value == 1) 
			nixbpe |= flag;
		else
			nixbpe -= flag;
	}
	
	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	@Override
	public String toString() {
		return label+","+operator;
	}
	
	
}
