/*
 * 화일명 : my_assembler_00000000.c 
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

/*
 *
 * 프로그램의 헤더를 정의한다. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>//isdigit을 사용하기위해 추가한 헤더

// 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20160283.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일 
 * 반환 : 성공 = 0, 실패 = < 0 
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다. 
 *		   또한 중간파일을 생성하지 않는다. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n");
		return -1;
	}
	//make_symtab_output("symtab_20160283.txt");
	//make_literaltab_output("literaltab_20160283.txt");
	make_symtab_output(NULL);
	make_literaltab_output(NULL);
	
	if (assem_pass2() < 0)
	{
		printf("assem_pass2: 패스2 과정에서 실패하였습니다.  \n");
		return -1;
	}

	//make_objectcode_output("output_20160283.txt");
	make_objectcode_output(NULL);

	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다. 
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기 
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다. 
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
	int result;

	if ((result = init_inst_file("inst.data")) < 0)
		return -1;
	if ((result = init_input_file("input.txt")) < 0)
		return -1;
	return result;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 
 *        생성하는 함수이다. 
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *	
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	FILE* file;
	int errno;
	char temp[100] = { '\0', };
	char* tok;
	
	//inst.data파일을 읽기모드로 가져온다.
	if ((file = fopen(inst_file, "r")) == NULL)
		return -1;

	// instruction Table을 만든다.

	for (int i = 0; i < MAX_INST; i++) {
		//inst.data 파일을 한 줄씩 읽어 temp에 저장한다.
		fgets(temp, sizeof(temp), file);
		if (feof(file))
			break;
		//inst_table에 저장 할 수있게 동적 할당해준다.
		inst_table[i] = (inst*)malloc(sizeof(inst));

		//temp 토큰화
		tok = strtok(temp, "\t ");

		//첫번째 토큰 - mnemonic(table mnemonic 변수에 동적 할당 후 값 할당)
		inst_table[i]->mnemonic = (char*)malloc(strlen(tok) + 1);
		strcpy(inst_table[i]->mnemonic, tok);

		//두번째 토큰 - operand(table operand 변수에 동적 할당 후 값 할당)
		tok = strtok(NULL, "\t ");
		inst_table[i]->operand = (char*)malloc(strlen(tok));
		strcpy(inst_table[i]->operand, tok);

		//세번째 토큰 - format(table format 변수에 동적 할당 후 값 할당)
		tok = strtok(NULL, "\t ");
		inst_table[i]->format = (char*)malloc(strlen(tok));
		strcpy(inst_table[i]->format, tok);

		//네번째 토큰 - opcode(table opcode 변수에 동적 할당 후 값 할당)
		tok = strtok(NULL, "\t ");
		//개행문자 제거
		tok[strlen(tok) - 1] = '\0';
		inst_table[i]->opcode = (char*)malloc(strlen(tok));
		strcpy(inst_table[i]->opcode, tok);

		inst_index++;
	}

	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다. 
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0  
 * 주의 : 라인단위로 저장한다.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE* file;
	int errno;
	char temp[100];
	//input.txt를 읽기 모드로 가져온다.
	if ((file = fopen(input_file, "r")) == NULL)
		return -1;

	for (int i = 0; i < MAX_LINES; i++) {

		//input.txt를 한줄 씩 읽는다.
		fgets(temp, sizeof(temp), file);
		if (feof(file)) {
			input_data[i] = (char*)malloc(strlen(temp));
			strcpy(input_data[i], temp);
			line_num++;
			break;
		}

		//input_data 동적 할당 input_data 배열에는 어셈블리어를 저장한다.
		input_data[i] = (char*)malloc(strlen(temp));
		strcpy(input_data[i], temp);
		line_num++;
	}
	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다. 
 *        패스 1로 부터 호출된다. 
 * 매계 : 파싱을 원하는 문자열  
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str)
{
	char* tok;
	char* opptr;
	int errno;
	//토큰 테이블 동적할딩
	token_table[token_line] = (token*)malloc(sizeof(token));
	token_table[token_line]->label = NULL;
	token_table[token_line]->operator= NULL;
 	token_table[token_line]->operand[0] = NULL;
	token_table[token_line]->operand[1] = NULL;
	token_table[token_line]->operand[2] = NULL;
	token_table[token_line]->comment = NULL;

	//주석일 경우 
	if (str[0] == '.') {
		return errno;
	}

	//Label 저장 
	if (str[0] == '\t') {
		//Label이 없다면 NULL을 저장한다.
		token_table[token_line]->label = NULL;
		tok = strtok(str, "\t\n");
	}
	else {
		//Label이 있다면 토큰을 구하여 저장한다.
		tok = strtok(str, "\t\n");
		token_table[token_line]->label = (char*)malloc(strlen(tok) + 1);
		strcpy(token_table[token_line]->label, tok);
		tok = strtok(NULL, "\t\n");
	}


	//Operator 저장
	//Operator가 있다면 저장
	if (tok == NULL) {
		return errno;
	}
	token_table[token_line]->operator = (char*)malloc(strlen(tok) + 1);
	strcpy(token_table[token_line]->operator, tok);

	//RSUB는 operand가 없기때문에 따로 처리하였다.
	if (strcmp(tok, "RSUB") == 0) {
		tok = strtok(NULL, "\t");
		if (tok != NULL) {
			token_table[token_line]->comment = (char*)malloc(strlen(tok) + 1);
			strcpy(token_table[token_line]->comment, tok);
		}
		token_line++;
		return errno;
	}

	//Operand는 나중에 하기위해 현제 위치를 opptr에 저장한다.
	tok = strtok(NULL, "\t");
	opptr = tok;

	//Comment 저장
	tok = strtok(NULL, "\t\n");
	//Comment가 있다면 저장
	if (tok != NULL) {
		token_table[token_line]->comment = (char*)malloc(strlen(tok) + 1);
		strcpy(token_table[token_line]->comment, tok);
	}
	//operand가 최대 3개의 인자가 있을 수 있다. ex)EXTREF BUFFER,LENGTH,BUFFEND - Delimeter : ','
	char* subtok = strtok(opptr, ",\n");
	//Operand 인자 토큰화
	for (int i = 0; i < MAX_OPERAND; i++) {
		token_table[token_line]->operand[i] = NULL;
	}

	for (int i = 0; i < MAX_OPERAND; i++) {
		if (subtok == NULL)
			break;
		token_table[token_line]->operand[i] = (char*)malloc(strlen(subtok) + 1);
		strcpy(token_table[token_line]->operand[i], subtok);
		subtok = strtok(NULL, ",\n");
	}
	token_line++;
	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다. 
 * 매계 : 토큰 단위로 구분된 문자열 
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0 
 * 주의 : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{
	//4형식이 있을 경우 + 를 빼내기 위해 tmp에 저장
	char* pstr;
	if ('A' <= str[0] && str[0] <= 'Z')
		pstr = str;
	else
		pstr = str + 1;
	for (int i = 0; i < inst_index; i++) {
		if (strcmp(pstr, inst_table[i]->mnemonic) == 0)
			return i;
	}
	//inst_table에 없으면 -1
	return -1;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	int errno;
	for (int i = 0; i < line_num; i++) {
		if (token_parsing(input_data[i]) < 0) {
			errno = -1;
			break;
		}
	}
	return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 5번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 5번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
// void make_opcode_output(char *file_name)
// {
// 	/* add your code here */

// }
/* ----------------------------------------------------------------------------------
 * 설명 : 입력 Label이 SYMTAB에 있는지 확인하는 함수이다.
 * 매계 : LABLE
 * 반환 : 정상종료 = Symbol 테이블에 매칭하는 주소값, 에러 < 0
* 주의 : 없음
* -----------------------------------------------------------------------------------
*/
int search_symbol_table(char* label) {

	for (int i = 0; i < sym_line; i++) {
		if (strcmp(sym_table[i].symbol, label) == 0)
			return sym_table[i].addr;
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 Label이 SYMTAB에 있는지 확인하는 함수이다.
 * 매계 : LABLE, 몇 번째 Sector인지
 * 반환 : 정상종료 = Symbol 테이블에 매칭하는 주소값, 에러 < 0
* 주의 : 없음
* -----------------------------------------------------------------------------------
*/
int search_symbol_table_sect(char* label, int sect) {
	int cnt = 0;
	for (int i = 0; i < sym_line; i++) {
		if (cnt == sect) {
			if (strcmp(sym_table[i].symbol, label) == 0)
				return sym_table[i].addr;
		}
		if (sym_table[i].symbol[0] == '\0') {
			cnt++;
		}
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 리터럴이 리터럴 테이블에 있는지 확인하는 함수이다.
 * 매계 : literal
 * 반환 : 정상종료 = Symbol 테이블에 매칭하는 주소값 , 에러 < 0
 * 주의 : 없음
* -----------------------------------------------------------------------------------
*/
int search_literal_table(char* literal) {
	
	for (int i = 0; i < lit_line; i++) {
		if (strcmp(literal_table[i].literal, literal) == 0)
			return literal_table[i].addr;
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
* 설명 : token_table을 하나씩 읽어 SYMTAB만들면서 LITTAB도 같이 만든다.
* 매계 : 없음
* 반환 : 없음
* 주의 : 없음
*
* -----------------------------------------------------------------------------------
*/
void make_symtab() {
	//Control Section Start Index
	int cotidx = 0;
	//LTORG이후 address
	int litidx = 0;
	//SYMTAB만들기
	for (int i = 0; i <token_line; i++) {

		//locctr을 저장하기 위한 pass2에서 진행할 table을 미리생성
		object_table[obj_line] = (object*)malloc(sizeof(object));
		object_table[obj_line]->record = 'E';//EMPTY
		object_table[obj_line]->instruction = -1;
		object_table[obj_line]->locctr = -1;// -1로 초기화
		object_table[obj_line]->m_record[0] = NULL;
		object_table[obj_line]->m_record[1] = NULL;
		object_table[obj_line]->m_record[2] = NULL;
		object_table[obj_line]->format = 0;
		object_table[obj_line]->use_ref = 0;
		object_table[obj_line]->type = 0;
		/*
		* LABLE이 존재하는 경우, SYMTAB 만들기
		*/
		if (token_table[i]->label != NULL) {
			
			//새로운 Control Section 나왔을 시 LOCCTR = 0으로 해준다.
			if (strcmp(token_table[i]->operator,"CSECT") == 0) {
				//LOCCTR을 0000으로 초기화
				locctr = 0;
				//새로운 Control Section 시작
				cotidx = i;
				//Control Section을 구분하기 위해 NULL삽입
				memset(sym_table[sym_line].symbol, '\0', sizeof(sym_table[sym_line].symbol));
				sym_line++;
			}

			//Operator가 EQU일 경우
			if (strcmp(token_table[i]->operator,"EQU") == 0) {
				//*일경우 현재 LOCCTR를 SYMTAB에 넣는다.
				if (strcmp(token_table[i]->operand[0], "*")==0) {
					strcpy(sym_table[sym_line].symbol, token_table[i]->label);
					sym_table[sym_line].addr = locctr;
					sym_line++;
				}
				//연산이 있을 경우
				else {
					//ex) BUFFEND - BUFFER : 사칙연산 기준으로 토큰화 각 주소를 연산에 넣는다.
					//첫번째 피연산자 인덱스 찾기
					char tmp[20];
					strcpy(tmp, token_table[i]->operand[0]);
					char* tok = strtok(tmp, "+-");
					int idx1 = search_symbol_table(tok);
					//두번째 피연산자 인덱스 찾기
					tok = strtok(NULL, "+-");
					int idx2 = search_symbol_table(tok);
					//SYMTAB에 추가
					strcpy(sym_table[sym_line].symbol, token_table[i]->label);
					sym_table[sym_line].addr = idx1 - idx2;
					sym_line++;
				}
			}
			else {

				strcpy(sym_table[sym_line].symbol, token_table[i]->label);
				sym_table[sym_line].addr = locctr;
				sym_line++;
			}
		}
		//END일경우
		if (strcmp(token_table[i]->operator, "END") == 0) {
			char* plit = NULL;
			char* cont;
			for (int j = litidx; j <= i; j++) {
				char* opr = token_table[j]->operand[0];
				if (opr != NULL && opr[0] == '=') {
					plit = token_table[j]->operand[0];
					char tmp[20];
					strcpy(tmp, plit);
					cont = strtok(tmp, "'");
					cont = strtok(NULL, "'");
					//1. search lit_tbl -> 있으면 추가 x
					int adr = search_literal_table(cont);
					//2. 없으면 추가하기
					if (adr < 0) {
						strcpy(literal_table[lit_line].literal, cont);
						literal_table[lit_line].addr = locctr;	
						lit_line++;
						//추가
						if (plit != NULL && plit[1] == 'C') {
							int len = strlen(cont);
							locctr += len;
							object_table[obj_line]->type = 'C';
						}
						else if (plit != NULL && plit[1] == 'X') {
							int len = strlen(cont);
							locctr += len / 2;
							object_table[obj_line]->type = 'X';
						}
					}
				}
			}
		}
		//START일 경우
		else if (strcmp(token_table[i]->operator, "START") == 0) {
			object_table[obj_line]->record = 'H';
		}
		/*
		* LOCTTR 증가 시키기
		*/
		//inst_table에 해당 operator를 저장
		char* opt = token_table[i]->operator;
		//inst_table에 해당 operator 인덱스 찾아오기
        int idx = search_opcode(opt);
		//directives가 아닌 operator일 경우
		if (idx > 0) {
			int format = atoi(inst_table[idx]->format);
			//형식이 1,2형식인 경우
			if (format == 1 || format == 2) {
				locctr += format;
			}
			//format이 3,4형식인 경우
			else {
				//operator가 4형식인 경우
				if (opt[0] == '+') {
					locctr += 4;
				}
				//operator가 3형식인 경우
				else {
					locctr += 3;
				}
			}
		}
		/*
		* BYTE,WORD,REWB,RESW인 경우 LOCCTR을 증가시킨다.
		* EQU를 만났을 경우
		*/
		else {
			//BYTE일 경우 
			if (strcmp(opt, "BYTE") == 0) {
				//"'"을 기준으로 operand 토큰화
				char tmp[20];
				strcpy(tmp, token_table[i]->operand[0]);
				char * tok = strtok(tmp, "'");
				int len = 0;
				//Char일 경우 : 뒤에 나오는 문자열 만큼 더해준다.
				if (strcmp(tok, "C") == 0) {
					tok = strtok(NULL, "'");
					int len = strlen(tok);
					locctr += len;
				}
				//16진수 일경우 : (뒤에 나오는 문자열 / 2) 만큼 더해준다.
				else if (strcmp(tok, "X") == 0) {
					tok = strtok(NULL, "'");
					len = strlen(tok);
					locctr += len / 2;
				}
			}
			//WORD일 경우 
			else if (strcmp(opt, "WORD") == 0) {
				locctr += 3;
			}
			//RESB일 경우
			else if (strcmp(opt, "RESB") == 0) {
				int num = atoi(token_table[i]->operand[0]);
				locctr += 1 * num;
			}
			//RESW일 경우
			else if (strcmp(opt, "RESW") == 0) {
				int num = atoi(token_table[i]->operand[0]);
				locctr += 3 * num;
			}

			//LTONG 만났을 시 literal table 탐색 
			else if (strcmp(token_table[i]->operator,"LTORG") == 0) {
				char* plit = NULL;
				char* cont = NULL;
				int cur_locctr = locctr;
				//현재 control section에서 리터럴 인 것 찾아 plit에 저장
				for (int j = cotidx; j <= i; j++) {
					char* tmp = token_table[j]->operand[0];
					if(tmp != NULL && tmp[0] == '=') {
						plit = token_table[j]->operand[0];
						break;
					}
				}
				//literal에 따라 LOCCTR 증가 시키기 : C일때, B일때
				if (plit != NULL && plit[1] == 'C') {
					char tmp[10];
					strcpy(tmp, plit);
					cont = strtok(tmp, "'");
					cont = strtok(NULL, "'");
					int len = strlen(cont);
					locctr += len;
					object_table[obj_line]->type = 'C';
				}
				else if (plit != NULL && plit[1] == 'X') {
					char tmp[10];
					strcpy(tmp, plit);
					cont = strtok(tmp, "'");
					cont = strtok(NULL, "'");
					int len = strlen(cont);
					locctr += len / 2;
					object_table[obj_line]->type = 'X';
				}
				//1. search lit_tbl -> 있으면 추가 x
				int adr = search_literal_table(cont);
				//2. 없으면 추가하기
				if (adr < 0 && plit!=NULL) {
					strcpy(literal_table[lit_line].literal, cont);
					literal_table[lit_line].addr = cur_locctr;
					lit_line++;
				}
				//
				litidx = i+1;
			}
		}
		object_table[obj_line]->locctr = locctr;
		object_table[obj_line]->format = 0;
		obj_line++;
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	FILE* out;
	
	make_symtab();
	//file_name이 NULL일경우 표준출력으로 화면에 출력한다.
	if (file_name == NULL) {
		out = stdout;
	}
	//file_name이 NULL이 아닐 경우 파일에 출력한다.
	else
		out = fopen(file_name, "w");

	for (int i = 0; i < sym_line; i++) {
		if (sym_table[i].symbol[0] != '\0') {
			fprintf(out, "%s\t\t%X\n", sym_table[i].symbol, sym_table[i].addr);
		}
		else
			fputs("\n", out);
	}
	fputs("\n", stdout);
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 LITERAL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char* file_name)
{
	FILE* out;
	if (file_name == NULL)
		out = stdout;
	else
		out = fopen(file_name, "w");

	for (int i = 0; i < lit_line; i++) {
		if (sym_table[i].symbol != '\0') {
			fprintf(out, "%s\t%X\n", literal_table[i].literal, literal_table[i].addr);
		}
	}
}
/* ----------------------------------------------------------------------------------
* 설명 : TokenTable의 해당 라인을 넣으면 nixbpe를 setting해준다.
* 매계 : TokenTable의 라인
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
int set_nixbpe(token* tok) {
	tok->nixbpe = 0;
	char* op = tok->operator;
	char* Xbit = tok->operand[1];
	char* operand = tok->operand[0];
	int idx = -1;
	if ((idx = search_opcode(op))> 0) {
		//RSUB일 경우 바로 종료 : nixbpe =0
		if (strcmp(op, "RSUB") == 0) {
			tok->nixbpe = 48;
			return idx;
		}

		//n = 0, i =1 
		if (operand[0] == '#') {
			tok->nixbpe += 16;
			operand = operand + 1;
		}

		// n= 1, i = 0
		else if (operand[0] == '@') {
			tok->nixbpe += 32;
			operand = operand + 1;
		}//n =1, i = 1
		else {
			tok->nixbpe += 48;
		}
		// e = 1
		if (op[0] == '+') {
			tok->nixbpe += 1;
			tok->nixbpe -= 2;//p = 0
		}
		// x = 1
		if (Xbit !=NULL && strcmp(Xbit, "X") == 0)
			tok->nixbpe += 8;

		//p = 1, b= 0 기본
		tok->nixbpe += 2;

		//operand가 label이아닌 숫자인 경우
		if (isdigit(operand[0]) != 0) {
			//p=0,b=0
			tok->nixbpe -= 2;
		}
		return idx;
	}

	return idx;
}
/* ----------------------------------------------------------------------------------
* 설명 : 해당 token line을 instruction으로 변환해준다.
* 매계 : token_table의 idx
* 반환 : 없음
* 주의 :
* -----------------------------------------------------------------------------------
*/
void make_instruction(int i) {
	int idx;
	//opcode를 얻기위해 opertor 인덱스 번호를 얻어오고 + nixbpe를 설정한다.(operator일 경우)
	idx = set_nixbpe(token_table[i]);
	//operator일 경우
	if (idx > 0) {
		inst* pinst = inst_table[idx];
		char* format = pinst->format;
		//opcode 를 정수로 변환ㅇ
		char* stop;
		int opcode = (int)strtol(pinst->opcode, &stop, 16);
		/*
		 * 형식에 따라 instruction format 달리하기
		*/
		//1. 1형식 : opcode -> 1byte(char)
		if (strcmp(format, "1") == 0) {
			object_table[i]->record = 'T';
			object_table[i]->instruction = opcode;
			object_table[i]->format = 1;
		}//2. 2형식 : opcode<<8 | r1<<4 | r2<<4  -> 2byte(char*2)
 		else if (strcmp(format, "2") == 0) {
			char r[2];
			memset(r, 0, sizeof(r));
			//r1,r2 인자를 문자열로 저장한다.
			for (int j = 0; j < atoi(pinst->operand); j++) {
				r[j] = token_table[i]->operand[j][0];
			}
			//r1,r2인 문자열을 해당 Register 번호에 맞게 수정한다.
			for (int j = 0; j < 2; j++) {
				if (r[j] != 0) {
					switch (r[j])
					{
					case 'A':
						r[j] = 0;
						break;
					case 'X':
						r[j] = 1;
						break;
					case 'L':
						r[j] = 2;
						break;
					case 'B':
						r[j] = 3;
						break;
					case 'S':
						r[j] = 4;
						break;
					case 'T':
						r[j] = 5;
						break;
					case 'F':
						r[j] = 6;
						break;
					}
				}
			}
			object_table[i]->record = 'T';
			object_table[i]->instruction = opcode << 8 | r[0] << 4 | r[1];
			object_table[i]->format = 2;
		}

		else if (strcmp(format, "3/4") == 0) {
			//4. 4형식 : opcode << 24 | nixbpe<<20 | addr -> 4byte
			char* operator = token_table[i]->operator;
			char* operand = token_table[i]->operand[0];
			if (operator[0] == '+') {
				int nix = token_table[i]->nixbpe;
				int adr=0;
				//#,@ 문자 제거 하여 탐색
				if (operand[0] == '#' || operand[0] == '@') {
					operand = operand + 1;
				}
				//ref_table안에 있는 값인지 확인한다.
				int idx = search_ref_table(operand);
				if (idx >= 0) {
					adr = 0;
					object_table[i]->use_ref = 1;
					object_table[i]->m_record[0] = operand;
					object_table[i]->opt[0] = '+';
				}
				//sym_table안에 있는 값인지 확인한다.
				else if (search_symbol_table_sect(operand,sect_cnt) > 0) {
					adr = search_symbol_table_sect(operand, sect_cnt);
				}
				object_table[i]->record = 'T';
				
				object_table[i]->instruction = (opcode << 24) | (nix<<20) | (adr);
				object_table[i]->format = 4;
			}
			//3. 3형식 : opcode<<16 | nixbpe<<12 | addr(symtab -locctr) -> 3byte
			else {
				int nix = token_table[i]->nixbpe;
				int adr = 0;
				int symtab = 0;
				int pc = 0;
				if (object_table[i]->locctr != -1) {
					pc = object_table[i]->locctr;
				}
				if (operand!=NULL) {
					//#,@ 문자 제거 하여 탐색
					if (operand[0] == '#' || operand[0] == '@') {
						operand = operand + 1;
					}

					//operand가 숫자일 경우 -> p = 0 , b = 0 
					if ((nix & 2) == 0) {
						adr = atoi(operand);
					}//literal일 경우 -> p =1, b = 0
					else if (operand[0] == '=') {
						char tmp[10];
						strcpy(tmp, operand);
						char* tok = strtok(tmp, "'");
						tok = strtok(NULL, "'");
						symtab = search_literal_table(tok);
						adr = symtab - pc;
					}
					//opernad가 memory일 경우 -> p = 1, b = 0
					else {
						symtab = search_symbol_table_sect(operand, sect_cnt);
						adr = symtab - pc;
					}
				}
				object_table[i]->record = 'T';
				//adr 음수 일경우 하위 12비트 제외 전부 0
				object_table[i]->instruction = (opcode << 16) | (nix << 12) | (0xFFF&adr);
				object_table[i]->format = 3;
			}
		}
	}//direcrives - instruction 없다면 
	else {
		char* op = token_table[i]->operator;
		char* operand = token_table[i]->operand[0];
		if (strcmp(op, "EXTDEF") == 0) {
			object_table[i]->record = 'D';
		}
		else if (strcmp(op, "EXTREF") == 0) {
			for (int j = 0; j < MAX_OPERAND; j++) {
				ref_list[j] = token_table[i]->operand[j];
				object_table[i]->record = 'R';
			}
		}
		//CSECT일 때
		else if (strcmp(op, "CSECT") == 0) {
			object_table[i]->record = 'H';
			sect_cnt++;
		}
		//BYTE,WORD일 경우 instruction 생성
		else if (strcmp(op, "WORD") == 0 || strcmp(op, "BYTE") == 0) {
			char* opr = operand;
			//tmp : 토큰위한 임시저장 변수, stop : strtol쓰기위한 변수
			char tmp[20];
			strcpy(tmp, opr);
			char* stop;
			
			//BYTE일 경우
			if (opr[0] == 'C' || opr[0] == 'X') {
				char* tok = strtok(tmp, "'");
				tok = strtok(NULL, "'");
				//'C'일 경우
				if (opr[0] == 'C')
				{
					int len = strlen(tok);
					for (int j = 0; j < len; j++) {
						object_table[i]->instruction = (object_table[i]->instruction << 8) | tmp[j];
						object_table[i]->format += 1;
						object_table[i]->record = 'T';
					}
				}
				//'X'일경우
				else {
					object_table[i]->instruction = (int)strtol(tok, &stop, 16);
					object_table[i]->format += 1;
					object_table[i]->record = 'T';
				}
			}
			//WORD일 경우
			else {
				//Operand에 +,-있을 때
				if (strchr(opr, '+') != NULL || strchr(opr, '-') != NULL) {
					//+일 경우
					if (strchr(opr, '+') != NULL) {
						char* tok = strtok(tmp, "+");
						char* op1 = tok;//첫번째 피연산자
						tok = strtok(NULL, "+");
						char* op2 = tok;//두번째 피연산자
						//EXTREF인지 확인한다.
						int idx1 = search_ref_table(op1);
						object_table[i]->m_record[0] = op1;
						int idx2 = search_ref_table(op2);
						object_table[i]->m_record[1] = op2;
						int TA1 = 0;
						int TA2 = 0;

						//첫번째 피연산자가 EXTREF 아닐경우 SYMTAB을 찾아본다.
						if (idx1 < 0) {
							int sym_adr = search_symbol_table(op1);
							TA1 = sym_adr;
						}//EXTREF 사용시
						else {
							object_table[i]->opt[0] = '+';
							object_table[i]->use_ref = 1;
						}
						//두번째 피연산자EXTREF 아닐경우 STMTAB을 찾아본다.
						if (idx2 < 0) {
							int sym_adr = search_symbol_table(op2);
							TA2 = sym_adr;
						}//EXTREF 사용시
						else {
							object_table[i]->opt[1] = '+';
							object_table[i]->use_ref = 1;
						}
						object_table[i]->record = 'T';
						object_table[i]->instruction = TA1 + TA2;
						object_table[i]->format = 3;
					}
					// -일 경우
					else if (strchr(opr, '-') != NULL) {
						//EXTREF인지 확인한다.
						char* tok = strtok(tmp, "-");
						char* op1 = tok;//첫번째 피연산자
						tok = strtok(NULL, "-");
						char* op2 = tok;//두번째 피연산자
						//EXTREF인지 확인한다.
						int idx1 = search_ref_table(op1);
						object_table[i]->m_record[0] = (char*)malloc(sizeof(strlen(op1)));
						strcpy(object_table[i]->m_record[0], op1);
						int idx2 = search_ref_table(op2);
						object_table[i]->m_record[1] = (char*)malloc(sizeof(strlen(op2)));
						strcpy(object_table[i]->m_record[1], op2);
						int TA1 = 0;
						int TA2 = 0;

						//첫번째 피연산자가 EXTREF 아닐경우 SYMTAB을 찾아본다.
						if (idx1 < 0) {
							int sym_adr = search_symbol_table(op1);
							TA1 = sym_adr;
						}//EXTREF 사용시
						else {
							object_table[i]->opt[0] = '+';
							object_table[i]->use_ref = 1;
						}
						//두번째 피연산자EXTREF 아닐경우 STMTAB을 찾아본다.
						if (idx2 < 0) {
							int sym_adr = search_symbol_table(op2);
							TA2 = sym_adr;
						}//EXTREF 사용 시
						else {
							object_table[i]->opt[1] = '-';
							object_table[i]->use_ref = 1;
						}
						object_table[i]->record = 'T';
						object_table[i]->instruction = TA1 - TA2;
						object_table[i]->format = 3;
					}
				}
				//+,- 없을 경우
				else{
					object_table[i]->instruction = (int)strtol(opr, &stop, 16);
				}
			}
			
		}
		//literal일 경우도 instruction 생성 
		else if (strcmp(op, "LTORG") == 0 || strcmp(op, "END") == 0) {
			object_table[i]->instruction = 0;
			//liteal Table 순차적으로
			char * ptr = literal_table[literal_cnt].literal;
			char tmp[10];
			strcpy(tmp, ptr);
			int len = strlen(tmp);
			char* stop;//16진수 구하기 위한 변수
			//문자라면
			if (object_table[i]->type == 'C') {
				object_table[i]->record = 'T';
				for (int j = 0; j < len; j++) {
					object_table[i]->instruction = (object_table[i]->instruction << 8) | tmp[j];
					object_table[i]->format += 1;
				}
			}
			//숫자라면
			else {
				object_table[i]->record = 'T';
				object_table[i]->instruction = (int)strtol(tmp, &stop, 16);;
				object_table[i]->format += 1;
			}
			literal_cnt++;
		}
	}
}
/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 ref_table에 있는지 확인하는 함수이다.
 * 매계 : ref(operand가 들어온다)
 * 반환 : 정상종료 = 테이블에 매칭하는 인덱스 , 에러 < 0
 * 주의 : 없음
* -----------------------------------------------------------------------------------
*/
int search_ref_table(char* ref) {
	for (int i = 0; i < MAX_OPERAND; i++) {
		if (ref_list[i] == NULL)
			break;
		if (strcmp(ref_list[i], ref) == 0)
			return i;
	}
	return -1;
}


/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
	int errno;
	int sect_cnt = 0;//몇 번째 control sector인지 저장하는 변수
	for (int i = 0; i < token_line; i++) {
		make_instruction(i);
	}

	return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : Sector Control의 길이를 구해주는 함수이다.
* 매계 : 몇 번째 Sector Control 인지(0,1,2)
* 반환 : 길이를 반환
* 주의 : 
*
* -----------------------------------------------------------------------------------
*/
int get_address_len(int sect) {
	int s_cnt = 0;//현재 몇번째 Sector인지
	int start_ad = 0;
	int end_ad = 0;

	for (int i = 0; i < obj_line; i++) {
		if (s_cnt == sect) {
			//시작 주소
			if (object_table[i]->record == 'H') {
				start_ad = object_table[i]->locctr;
			}
			//끝나는 주소(다음 것이 H일 때)
			if (i < obj_line-1 && object_table[i + 1]->record == 'H') {
				end_ad = object_table[i]->locctr;
				break;
			}
			//마지막 Section Control일 경우
			else if (i == obj_line - 1) {
				end_ad = object_table[i]->locctr;
			}
		}
		else {
			if (i < obj_line && object_table[i + 1]->record == 'H') 
				s_cnt++;
		}
	}
	return end_ad - start_ad;
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	
	char record;//record ex)H D R T M E등
	int loc;//locctr
	int inst;//objcet code
	int format;//명령어가 몇 형식인지
	int end = object_table[0]->locctr;//프로그램 시작 주소
	int start_sect = 0;//Section control 시작 줄
	
	//전역변수 초기화 : 처음줄부터 탐색 진행
	sect_cnt = 0;
	literal_cnt = 0;


	//출력 위치
	FILE* out;
	
	//file_name이 비었을 경우 표준 출력
	if (file_name == NULL) {
		out = stdout;
	}//"output_20160283.txt에 출력
	else
		out = fopen(file_name,"w");
	
	for (int i = 0; i < obj_line; i++) {
		record = object_table[i]->record;
		loc = object_table[i]->locctr;
		inst = object_table[i]->instruction;
		format = object_table[i]->format;

		//record가 H인 경우
		if (record == 'H') {
			//첫번재 H가 아니라면 개행문자 넣어준다.
			if (i != 0) {
				fputs("\n\n", out);
			}
			//프로그램 이름
			fprintf(out, "%c%-6s",'H',token_table[i]->label);

			//시작 주소
			fprintf(out, "%06X", object_table[i]->locctr);

			//프로그램 길이
			int len = get_address_len(sect_cnt++);
			fprintf(out, "%06X", len);

			start_sect = i;
		}
		//record가 D인 경우
		else if (record == 'D') {
			//D : 외부로 보내는 변수이름 + 위치
			fputs("\n", out);
			fputs("D", out);
			for (int j = 0; j < MAX_OPERAND; j++) {
				if (token_table[i]->operand[j] != NULL) {
					int sym_loc = search_symbol_table(token_table[i]->operand[j]);
					fprintf(out, "%s%06X", token_table[i]->operand[j],sym_loc);
				}
			}
		}
		//record가 R인 경우
		else if (record == 'R') {
			//R : EXTREF 주소이름
			fputs("\n", out);
			fputs("R", out);
			for (int j = 0; j < MAX_OPERAND; j++) {
				if (token_table[i]->operand[j] != NULL) {
					fprintf(out, "%-6s", token_table[i]->operand[j]);
				}
			}
		}
		else if (record == 'T') {
			fputs("\n", out);
			fputs("T", out);
			int num = 0;//i변수를 되돌리기 위한 변수
			int sum = 0;//각 라인이 길이
			//길이 구하기 : 
			while ((i < obj_line) && (object_table[i]->record == 'T')) {
				
				//literal 일때
				if (format == 0) {
					sum += object_table[i]->format;
				}
				else
					sum += object_table[i]->format;

				if (sum > 30) {
					sum -= object_table[i]->format;//초과된것 원상태로 되돌리기
					break;
				}
				num++;
				i++;
			}
			//i위치 초기화
			i -= num;
			//Object Code 시작 주소 및 길이 출력
			fprintf(out, "%06X%02X", object_table[i - 1]->locctr, sum);

			//ObjectCode 출력
			for(int j = 0 ;j<num;j++) {
				format = object_table[i]->format;
				
				if (format == 1) {
					fprintf(out, "%02X", object_table[i]->instruction);
				}
				if (format == 2) {
					fprintf(out, "%04X", object_table[i]->instruction);
				}

				//3형식
				if (format == 3) {
					fprintf(out, "%06X",object_table[i]->instruction);
				}
				//4형식
				if (format == 4) {
					fprintf(out, "%08X", object_table[i]->instruction);
				}
				i++;
			}
			i--;
		}
		
		//M Record 와 E Record 
		if ((i != obj_line - 1 && object_table[i + 1]->record == 'H')||i== obj_line-1) {
			//M Record 출력
			for (int j = start_sect; j <= i; j++) {
				//REF사용한 줄 or 순수한 4형식인 줄
				if (object_table[j]->use_ref == 1 || object_table[j]->format == 4) {
					int startidx = 0;
					int target_bit = 0;
					//4형식일 때
					if (object_table[j]->format == 4) {
						startidx = (int)(object_table[j]->locctr - 2.5);
						target_bit = 5;
					}
					//3형식일때
					else {
						startidx = object_table[j]->locctr - 3;
						target_bit = 6;
					}
					char* ref;
					char opt;
					for (int k = 0; k < MAX_OPERAND; k++) {
						if (object_table[j]->m_record[k] == NULL) {
							//ref없는 순수한 4형식
							if (k == 0) {
								fprintf(out, "\nM%06X%02d", startidx, target_bit);
							}
							break;
						}
						ref = object_table[j]->m_record[k];
						opt = object_table[j]->opt[k];
						if (ref != NULL) {
							fprintf(out, "\nM%06X%02d%c%s", startidx, target_bit, opt, ref);
						}
						else {
							fprintf(out, "\nM%06X%02d%c%-6s", startidx, target_bit, ref);
						}
					}
				}
			}
			//E RECORD 
			if (end == 0) {
				fprintf(out, "\nE%06X", end);
				end = -1;
			}
			else {
				fprintf(out, "\nE");
			}
		}
	}
}

