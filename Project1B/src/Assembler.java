import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셈블러의 메인 루틴
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
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
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
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		//SYMTAB과 LITTAB를 생성한 뒤  list에 넣어주기
		SymbolTable symTab = new SymbolTable();
		LiteralTable litTab = new LiteralTable();
		symtabList.add(symTab);
		literaltabList.add(litTab);
		
		//TokenTable 생성 뒤 SYMTAB와 LITTAB 링크해주기
		TokenTable tokenTab = new TokenTable(symTab, litTab, instTable);
		TokenList.add(tokenTab);
		
		//token idx
		int t_idx = 0;
		
		//한줄씩 읽어 토큰화 진행
		for(int i = 0 ; i <lineList.size();i++) {
			if(lineList.get(i) !=null&& lineList.get(i).charAt(0)=='.')
				continue;
			/*
			 * 새로운 Control Section 시작
			 */
			if(lineList.get(i).contains("CSECT")) {
				//새로운 Section Control 시작 시 변수 초기화
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		PrintStream out = null;
		//fileName이 Null일 경우 표준 출력
		if(fileName == null) 
			out= new PrintStream(System.out);
		else {
			//null이 아닐 경우 fileName에 wirte하기
			try {
				String path = System.getProperty("user.dir");
				File file = new File(path+"\\src\\"+fileName);
				out = new PrintStream(new FileOutputStream(file));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		//출력하기 : SymbolTable Class toString() Method Override
		for(SymbolTable symTab : symtabList) {
			out.println(symTab);
		}
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		PrintStream out = null;
		if(fileName == null) 
			out= new PrintStream(System.out);
		else {
			//null이 아닐 경우 fileName에 wirte하기
			try {
				String path = System.getProperty("user.dir");
				File file = new File(path+"\\src\\"+fileName);
				out = new PrintStream(new FileOutputStream(file));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		//출력하기 : LiteralTable Class toString() Method Override
		for(LiteralTable litTab : literaltabList) {
			out.print(litTab);
		}
	}

	/**
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
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
					//H Record일 때
					sb.append('H');
					//프로그램 이름 6자리
					sb.append(String.format("%-6s",t.label));
					//프로그램 시작 주소
					sb.append(String.format("%06X",t.location));
					//프로그램 길이
					int sum_len = tt.tokenList.stream().mapToInt(tok->tok.byteSize).sum();
					sb.append(String.format("%06X", sum_len));
					codeList.add(sb.toString());
					sb = new StringBuilder();
					break;
				case 'D':
					//D Record
					sb.append('D');
					//외부로 보내는 변수이름 + 위치
					for(int j = 0 ; j < TokenTable.MAX_OPERAND;j++) {
						if(t.operand[j] == null)
							break;
						//외부로 보내는 변수 이름
						sb.append(String.format("%-6s",t.operand[j]));
						//외부로 보내는 변수 위치
						sb.append(String.format("%06X",tt.symTab.search(t.operand[j])));
					}
					codeList.add(sb.toString());
					sb = new StringBuilder();
					break;
				case 'R':
					//R Record 일 때
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
					//T Record 출력
					sb.append('T');
					//시작 주소 출력
					sb.append(String.format("%06X",tt.getToken(i-1).location));
					//해당 라인의 길이의 합을 저장
					int sum = 0;
					//현재 토큰 T 중 어느 위치에 있는지 확인한다.
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
				//M Record 출력 EX) M 위치 + Reference일때 더할것
				if(i == tt.tokenList.size()-1) {
					for(Token tok:tt.tokenList) {
						for(int j = 0;j<tok.memoCnt;j++) {
							//4형식일 경우
							if(tok.byteSize == 4) {
								sb.append('M');
								sb.append(String.format("%06X05",(int)(tok.location-2.5)));
								//EXTREF 사용 시
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
								//Expression이 + 일때
								if(tok.operand[0].contains("+")) {
									st = tok.operand[0].split("+");
									sb.append(String.format("%06X06",tok.location-3));
									sb.append("+");
									sb.append(String.format("%-6s",st[j]));
								}
								//Expression이 - 일때
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
					//E 출력
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
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
