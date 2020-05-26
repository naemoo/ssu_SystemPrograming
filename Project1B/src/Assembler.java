import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ����*/
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����.   
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");	
		assembler.pass1();

		assembler.printSymbolTable(null);
		assembler.printLiteralTable(null);
		assembler.printSymbolTable("symtab_20160283.txt");
		assembler.printLiteralTable("literaltab_20160283.txt");
		assembler.pass2();
		assembler.printObjectCode("output_20160283.txt");
		assembler.printObjectCode(null);
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		File file = new File("src//"+inputFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine())!= null) {
				lineList.add(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		//SYMTAB�� LITTAB�� ������ ��  list�� �־��ֱ�
		SymbolTable symTab = new SymbolTable();
		LiteralTable litTab = new LiteralTable();
		symtabList.add(symTab);
		literaltabList.add(litTab);
		
		//TokenTable ���� �� SYMTAB�� LITTAB ��ũ���ֱ�
		TokenTable tokenTab = new TokenTable(symTab, litTab, instTable);
		TokenList.add(tokenTab);
		
		//token idx
		int t_idx = 0;
		
		//���پ� �о� ��ūȭ ����
		for(int i = 0 ; i <lineList.size();i++) {
			if(lineList.get(i) !=null&& lineList.get(i).charAt(0)=='.')
				continue;
			/*
			 * ���ο� Control Section ����
			 */
			if(lineList.get(i).contains("CSECT")) {
				//���ο� Section Control ���� �� ���� �ʱ�ȭ
				symTab = new SymbolTable();
				litTab = new LiteralTable();
				tokenTab = new TokenTable(symTab, litTab, instTable);
				symtabList.add(symTab);
				literaltabList.add(litTab);
				TokenList.add(tokenTab);
				t_idx = 0;
			}
			
			tokenTab.putToken(lineList.get(i));
//			Token t = tokenTab.getToken(t_idx);
			t_idx++;
		}
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		PrintStream out = null;
		//fileName�� Null�� ��� ǥ�� ���
		if(fileName == null) 
			out= new PrintStream(System.out);
		else {
			//null�� �ƴ� ��� fileName�� wirte�ϱ�
			try {
				String path = System.getProperty("user.dir");
				File file = new File(path+"\\src\\"+fileName);
				out = new PrintStream(new FileOutputStream(file));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		//����ϱ� : SymbolTable Class toString() Method Override
		for(SymbolTable symTab : symtabList) {
			out.println(symTab);
		}
	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		PrintStream out = null;
		if(fileName == null) 
			out= new PrintStream(System.out);
		else {
			//null�� �ƴ� ��� fileName�� wirte�ϱ�
			try {
				String path = System.getProperty("user.dir");
				File file = new File(path+"\\src\\"+fileName);
				out = new PrintStream(new FileOutputStream(file));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		//����ϱ� : LiteralTable Class toString() Method Override
		for(LiteralTable litTab : literaltabList) {
			out.print(litTab);
		}
	}

	/**
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		for(TokenTable tt:TokenList) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0 ; i <tt.tokenList.size();i++) {
				tt.makeObjectCode(i);
				Token t = tt.getToken(i);
				switch (t.record) {
				case 'H':
					sb = new StringBuilder();
					//H Record�� ��
					sb.append('H');
					//���α׷� �̸� 6�ڸ�
					sb.append(String.format("%-6s",t.label));
					//���α׷� ���� �ּ�
					sb.append(String.format("%06X",t.location));
					//���α׷� ����
					int sum_len = tt.tokenList.stream().mapToInt(tok->tok.byteSize).sum();
					sb.append(String.format("%06X", sum_len));
					codeList.add(sb.toString());
					sb = new StringBuilder();
					break;
				case 'D':
					//D Record
					sb.append('D');
					//�ܺη� ������ �����̸� + ��ġ
					for(int j = 0 ; j < TokenTable.MAX_OPERAND;j++) {
						if(t.operand[j] == null)
							break;
						//�ܺη� ������ ���� �̸�
						sb.append(String.format("%-6s",t.operand[j]));
						//�ܺη� ������ ���� ��ġ
						sb.append(String.format("%06X",tt.symTab.search(t.operand[j])));
					}
					codeList.add(sb.toString());
					sb = new StringBuilder();
					break;
				case 'R':
					//R Record �� ��
					sb.append('R');
					for(int j = 0 ; j < TokenTable.MAX_OPERAND;j++) {
						if(t.operand[j] == null)
							break;
						sb.append(String.format("%-6s", t.operand[j]));
					}
					codeList.add(sb.toString());
					sb = new StringBuilder();
					break;
				case 'T':
					//T Record ���
					sb.append('T');
					//���� �ּ� ���
					sb.append(String.format("%06X",tt.getToken(i-1).location));
					//�ش� ������ ������ ���� ����
					int sum = 0;
					//���� ��ū T �� ��� ��ġ�� �ִ��� Ȯ���Ѵ�.
					int idx_T = 0;
					for(int j = i;j<tt.tokenList.size();j++) {
						tt.makeObjectCode(j);
						if(tt.getToken(j).objectCode == null&&tt.getToken(j).record!='T') {
							idx_T = j;
							break;
						}
						sum += tt.getToken(j).byteSize;
						if(sum>30) {
							idx_T = j;
							sum -= tt.getToken(j).byteSize;
							break;
						}
						if(j==tt.tokenList.size()-1)
							idx_T = j+1;
					}
					sb.append(String.format("%02X", sum));
					//object code
					for(int j = i;j<idx_T;j++) {
						sb.append(tt.getToken(j).objectCode);
					}
					i = idx_T-1;
					codeList.add(sb.toString());
					sb = new StringBuilder();
					break;
				case 'E':
					break;
				}
				//M Record ��� EX) M ��ġ + Reference�϶� ���Ұ�
				if(i == tt.tokenList.size()-1) {
					for(Token tok:tt.tokenList) {
						for(int j = 0;j<tok.memoCnt;j++) {
							//4������ ���
							if(tok.byteSize == 4) {
								sb.append('M');
								sb.append(String.format("%06X05",(int)(tok.location-2.5)));
								//EXTREF ��� ��
								if(tt.refList.indexOf(tok.operand[0])>=0) {
									sb.append('+');
									sb.append(String.format("%-6s",tok.operand[0]));
								}
								codeList.add(sb.toString());
								sb = new StringBuilder();
							}
							if(tok.byteSize == 3) {
								String[] st = null;
								sb.append('M');
								//Expression�� + �϶�
								if(tok.operand[0].contains("+")) {
									st = tok.operand[0].split("+");
									sb.append(String.format("%06X06",tok.location-3));
									sb.append("+");
									sb.append(String.format("%-6s",st[j]));
								}
								//Expression�� - �϶�
								else if(tok.operand[0].contains("-")) {
									st = tok.operand[0].split("-");
									sb.append(String.format("%06X06",tok.location-3));
									if(j==0)
										sb.append("+");
									else
										sb.append("-");
									sb.append(String.format("%-6s",st[j]));
								}
								codeList.add(sb.toString());
								sb = new StringBuilder();
							}
						}
					}
					//E ���
					sb.append('E');
					if(tt.isMain)
						sb.append(String.format("%06X",0));
					sb.append("\n");
					codeList.add(sb.toString());
					sb = new StringBuilder();
				}
				
			}
		}
	}
	
	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		PrintStream out = null;
		if(fileName == null)
			out = new PrintStream(System.out);
		else {
			try {
				String path = System.getProperty("user.dir");
				File file = new File(path+"\\src\\"+fileName);
				out = new PrintStream(new FileOutputStream(file));
			}
			catch(IOException ie) {
				ie.printStackTrace();
			}
		}
		for(String str:codeList) {
			out.println(str);
		}
	}
	
}
