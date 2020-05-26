/*
 * ȭ�ϸ� : my_assembler_00000000.c 
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

/*
 *
 * ���α׷��� ����� �����Ѵ�. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>//isdigit�� ����ϱ����� �߰��� ���

// ���ϸ��� "00000000"�� �ڽ��� �й����� ������ ��.
#include "my_assembler_20160283.h"

/* ----------------------------------------------------------------------------------
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ���� 
 * ��ȯ : ���� = 0, ���� = < 0 
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�. 
 *		   ���� �߰������� �������� �ʴ´�. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}
	//make_symtab_output("symtab_20160283.txt");
	//make_literaltab_output("literaltab_20160283.txt");
	make_symtab_output(NULL);
	make_literaltab_output(NULL);
	
	if (assem_pass2() < 0)
	{
		printf("assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}

	//make_objectcode_output("output_20160283.txt");
	make_objectcode_output(NULL);

	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�. 
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ� 
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���. 
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
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)�� 
 *        �����ϴ� �Լ��̴�. 
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *	
 *	===============================================================================
 *		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
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
	
	//inst.data������ �б���� �����´�.
	if ((file = fopen(inst_file, "r")) == NULL)
		return -1;

	// instruction Table�� �����.

	for (int i = 0; i < MAX_INST; i++) {
		//inst.data ������ �� �پ� �о� temp�� �����Ѵ�.
		fgets(temp, sizeof(temp), file);
		if (feof(file))
			break;
		//inst_table�� ���� �� ���ְ� ���� �Ҵ����ش�.
		inst_table[i] = (inst*)malloc(sizeof(inst));

		//temp ��ūȭ
		tok = strtok(temp, "\t ");

		//ù��° ��ū - mnemonic(table mnemonic ������ ���� �Ҵ� �� �� �Ҵ�)
		inst_table[i]->mnemonic = (char*)malloc(strlen(tok) + 1);
		strcpy(inst_table[i]->mnemonic, tok);

		//�ι�° ��ū - operand(table operand ������ ���� �Ҵ� �� �� �Ҵ�)
		tok = strtok(NULL, "\t ");
		inst_table[i]->operand = (char*)malloc(strlen(tok));
		strcpy(inst_table[i]->operand, tok);

		//����° ��ū - format(table format ������ ���� �Ҵ� �� �� �Ҵ�)
		tok = strtok(NULL, "\t ");
		inst_table[i]->format = (char*)malloc(strlen(tok));
		strcpy(inst_table[i]->format, tok);

		//�׹�° ��ū - opcode(table opcode ������ ���� �Ҵ� �� �� �Ҵ�)
		tok = strtok(NULL, "\t ");
		//���๮�� ����
		tok[strlen(tok) - 1] = '\0';
		inst_table[i]->opcode = (char*)malloc(strlen(tok));
		strcpy(inst_table[i]->opcode, tok);

		inst_index++;
	}

	return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�. 
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0  
 * ���� : ���δ����� �����Ѵ�.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE* file;
	int errno;
	char temp[100];
	//input.txt�� �б� ���� �����´�.
	if ((file = fopen(input_file, "r")) == NULL)
		return -1;

	for (int i = 0; i < MAX_LINES; i++) {

		//input.txt�� ���� �� �д´�.
		fgets(temp, sizeof(temp), file);
		if (feof(file)) {
			input_data[i] = (char*)malloc(strlen(temp));
			strcpy(input_data[i], temp);
			line_num++;
			break;
		}

		//input_data ���� �Ҵ� input_data �迭���� ������ �����Ѵ�.
		input_data[i] = (char*)malloc(strlen(temp));
		strcpy(input_data[i], temp);
		line_num++;
	}
	return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�. 
 *        �н� 1�� ���� ȣ��ȴ�. 
 * �Ű� : �Ľ��� ���ϴ� ���ڿ�  
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str)
{
	char* tok;
	char* opptr;
	int errno;
	//��ū ���̺� �����ҵ�
	token_table[token_line] = (token*)malloc(sizeof(token));
	token_table[token_line]->label = NULL;
	token_table[token_line]->operator= NULL;
 	token_table[token_line]->operand[0] = NULL;
	token_table[token_line]->operand[1] = NULL;
	token_table[token_line]->operand[2] = NULL;
	token_table[token_line]->comment = NULL;

	//�ּ��� ��� 
	if (str[0] == '.') {
		return errno;
	}

	//Label ���� 
	if (str[0] == '\t') {
		//Label�� ���ٸ� NULL�� �����Ѵ�.
		token_table[token_line]->label = NULL;
		tok = strtok(str, "\t\n");
	}
	else {
		//Label�� �ִٸ� ��ū�� ���Ͽ� �����Ѵ�.
		tok = strtok(str, "\t\n");
		token_table[token_line]->label = (char*)malloc(strlen(tok) + 1);
		strcpy(token_table[token_line]->label, tok);
		tok = strtok(NULL, "\t\n");
	}


	//Operator ����
	//Operator�� �ִٸ� ����
	if (tok == NULL) {
		return errno;
	}
	token_table[token_line]->operator = (char*)malloc(strlen(tok) + 1);
	strcpy(token_table[token_line]->operator, tok);

	//RSUB�� operand�� ���⶧���� ���� ó���Ͽ���.
	if (strcmp(tok, "RSUB") == 0) {
		tok = strtok(NULL, "\t");
		if (tok != NULL) {
			token_table[token_line]->comment = (char*)malloc(strlen(tok) + 1);
			strcpy(token_table[token_line]->comment, tok);
		}
		token_line++;
		return errno;
	}

	//Operand�� ���߿� �ϱ����� ���� ��ġ�� opptr�� �����Ѵ�.
	tok = strtok(NULL, "\t");
	opptr = tok;

	//Comment ����
	tok = strtok(NULL, "\t\n");
	//Comment�� �ִٸ� ����
	if (tok != NULL) {
		token_table[token_line]->comment = (char*)malloc(strlen(tok) + 1);
		strcpy(token_table[token_line]->comment, tok);
	}
	//operand�� �ִ� 3���� ���ڰ� ���� �� �ִ�. ex)EXTREF BUFFER,LENGTH,BUFFEND - Delimeter : ','
	char* subtok = strtok(opptr, ",\n");
	//Operand ���� ��ūȭ
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
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�. 
 * �Ű� : ��ū ������ ���е� ���ڿ� 
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0 
 * ���� : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{
	//4������ ���� ��� + �� ������ ���� tmp�� ����
	char* pstr;
	if ('A' <= str[0] && str[0] <= 'Z')
		pstr = str;
	else
		pstr = str + 1;
	for (int i = 0; i < inst_index; i++) {
		if (strcmp(pstr, inst_table[i]->mnemonic) == 0)
			return i;
	}
	//inst_table�� ������ -1
	return -1;
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 5��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 5�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/
// void make_opcode_output(char *file_name)
// {
// 	/* add your code here */

// }
/* ----------------------------------------------------------------------------------
 * ���� : �Է� Label�� SYMTAB�� �ִ��� Ȯ���ϴ� �Լ��̴�.
 * �Ű� : LABLE
 * ��ȯ : �������� = Symbol ���̺� ��Ī�ϴ� �ּҰ�, ���� < 0
* ���� : ����
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
 * ���� : �Է� Label�� SYMTAB�� �ִ��� Ȯ���ϴ� �Լ��̴�.
 * �Ű� : LABLE, �� ��° Sector����
 * ��ȯ : �������� = Symbol ���̺� ��Ī�ϴ� �ּҰ�, ���� < 0
* ���� : ����
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
 * ���� : �Է� ���ͷ��� ���ͷ� ���̺� �ִ��� Ȯ���ϴ� �Լ��̴�.
 * �Ű� : literal
 * ��ȯ : �������� = Symbol ���̺� ��Ī�ϴ� �ּҰ� , ���� < 0
 * ���� : ����
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
* ���� : token_table�� �ϳ��� �о� SYMTAB����鼭 LITTAB�� ���� �����.
* �Ű� : ����
* ��ȯ : ����
* ���� : ����
*
* -----------------------------------------------------------------------------------
*/
void make_symtab() {
	//Control Section Start Index
	int cotidx = 0;
	//LTORG���� address
	int litidx = 0;
	//SYMTAB�����
	for (int i = 0; i <token_line; i++) {

		//locctr�� �����ϱ� ���� pass2���� ������ table�� �̸�����
		object_table[obj_line] = (object*)malloc(sizeof(object));
		object_table[obj_line]->record = 'E';//EMPTY
		object_table[obj_line]->instruction = -1;
		object_table[obj_line]->locctr = -1;// -1�� �ʱ�ȭ
		object_table[obj_line]->m_record[0] = NULL;
		object_table[obj_line]->m_record[1] = NULL;
		object_table[obj_line]->m_record[2] = NULL;
		object_table[obj_line]->format = 0;
		object_table[obj_line]->use_ref = 0;
		object_table[obj_line]->type = 0;
		/*
		* LABLE�� �����ϴ� ���, SYMTAB �����
		*/
		if (token_table[i]->label != NULL) {
			
			//���ο� Control Section ������ �� LOCCTR = 0���� ���ش�.
			if (strcmp(token_table[i]->operator,"CSECT") == 0) {
				//LOCCTR�� 0000���� �ʱ�ȭ
				locctr = 0;
				//���ο� Control Section ����
				cotidx = i;
				//Control Section�� �����ϱ� ���� NULL����
				memset(sym_table[sym_line].symbol, '\0', sizeof(sym_table[sym_line].symbol));
				sym_line++;
			}

			//Operator�� EQU�� ���
			if (strcmp(token_table[i]->operator,"EQU") == 0) {
				//*�ϰ�� ���� LOCCTR�� SYMTAB�� �ִ´�.
				if (strcmp(token_table[i]->operand[0], "*")==0) {
					strcpy(sym_table[sym_line].symbol, token_table[i]->label);
					sym_table[sym_line].addr = locctr;
					sym_line++;
				}
				//������ ���� ���
				else {
					//ex) BUFFEND - BUFFER : ��Ģ���� �������� ��ūȭ �� �ּҸ� ���꿡 �ִ´�.
					//ù��° �ǿ����� �ε��� ã��
					char tmp[20];
					strcpy(tmp, token_table[i]->operand[0]);
					char* tok = strtok(tmp, "+-");
					int idx1 = search_symbol_table(tok);
					//�ι�° �ǿ����� �ε��� ã��
					tok = strtok(NULL, "+-");
					int idx2 = search_symbol_table(tok);
					//SYMTAB�� �߰�
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
		//END�ϰ��
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
					//1. search lit_tbl -> ������ �߰� x
					int adr = search_literal_table(cont);
					//2. ������ �߰��ϱ�
					if (adr < 0) {
						strcpy(literal_table[lit_line].literal, cont);
						literal_table[lit_line].addr = locctr;	
						lit_line++;
						//�߰�
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
		//START�� ���
		else if (strcmp(token_table[i]->operator, "START") == 0) {
			object_table[obj_line]->record = 'H';
		}
		/*
		* LOCTTR ���� ��Ű��
		*/
		//inst_table�� �ش� operator�� ����
		char* opt = token_table[i]->operator;
		//inst_table�� �ش� operator �ε��� ã�ƿ���
        int idx = search_opcode(opt);
		//directives�� �ƴ� operator�� ���
		if (idx > 0) {
			int format = atoi(inst_table[idx]->format);
			//������ 1,2������ ���
			if (format == 1 || format == 2) {
				locctr += format;
			}
			//format�� 3,4������ ���
			else {
				//operator�� 4������ ���
				if (opt[0] == '+') {
					locctr += 4;
				}
				//operator�� 3������ ���
				else {
					locctr += 3;
				}
			}
		}
		/*
		* BYTE,WORD,REWB,RESW�� ��� LOCCTR�� ������Ų��.
		* EQU�� ������ ���
		*/
		else {
			//BYTE�� ��� 
			if (strcmp(opt, "BYTE") == 0) {
				//"'"�� �������� operand ��ūȭ
				char tmp[20];
				strcpy(tmp, token_table[i]->operand[0]);
				char * tok = strtok(tmp, "'");
				int len = 0;
				//Char�� ��� : �ڿ� ������ ���ڿ� ��ŭ �����ش�.
				if (strcmp(tok, "C") == 0) {
					tok = strtok(NULL, "'");
					int len = strlen(tok);
					locctr += len;
				}
				//16���� �ϰ�� : (�ڿ� ������ ���ڿ� / 2) ��ŭ �����ش�.
				else if (strcmp(tok, "X") == 0) {
					tok = strtok(NULL, "'");
					len = strlen(tok);
					locctr += len / 2;
				}
			}
			//WORD�� ��� 
			else if (strcmp(opt, "WORD") == 0) {
				locctr += 3;
			}
			//RESB�� ���
			else if (strcmp(opt, "RESB") == 0) {
				int num = atoi(token_table[i]->operand[0]);
				locctr += 1 * num;
			}
			//RESW�� ���
			else if (strcmp(opt, "RESW") == 0) {
				int num = atoi(token_table[i]->operand[0]);
				locctr += 3 * num;
			}

			//LTONG ������ �� literal table Ž�� 
			else if (strcmp(token_table[i]->operator,"LTORG") == 0) {
				char* plit = NULL;
				char* cont = NULL;
				int cur_locctr = locctr;
				//���� control section���� ���ͷ� �� �� ã�� plit�� ����
				for (int j = cotidx; j <= i; j++) {
					char* tmp = token_table[j]->operand[0];
					if(tmp != NULL && tmp[0] == '=') {
						plit = token_table[j]->operand[0];
						break;
					}
				}
				//literal�� ���� LOCCTR ���� ��Ű�� : C�϶�, B�϶�
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
				//1. search lit_tbl -> ������ �߰� x
				int adr = search_literal_table(cont);
				//2. ������ �߰��ϱ�
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ SYMBOL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	FILE* out;
	
	make_symtab();
	//file_name�� NULL�ϰ�� ǥ��������� ȭ�鿡 ����Ѵ�.
	if (file_name == NULL) {
		out = stdout;
	}
	//file_name�� NULL�� �ƴ� ��� ���Ͽ� ����Ѵ�.
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ LITERAL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
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
* ���� : TokenTable�� �ش� ������ ������ nixbpe�� setting���ش�.
* �Ű� : TokenTable�� ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
* -----------------------------------------------------------------------------------
*/
int set_nixbpe(token* tok) {
	tok->nixbpe = 0;
	char* op = tok->operator;
	char* Xbit = tok->operand[1];
	char* operand = tok->operand[0];
	int idx = -1;
	if ((idx = search_opcode(op))> 0) {
		//RSUB�� ��� �ٷ� ���� : nixbpe =0
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

		//p = 1, b= 0 �⺻
		tok->nixbpe += 2;

		//operand�� label�̾ƴ� ������ ���
		if (isdigit(operand[0]) != 0) {
			//p=0,b=0
			tok->nixbpe -= 2;
		}
		return idx;
	}

	return idx;
}
/* ----------------------------------------------------------------------------------
* ���� : �ش� token line�� instruction���� ��ȯ���ش�.
* �Ű� : token_table�� idx
* ��ȯ : ����
* ���� :
* -----------------------------------------------------------------------------------
*/
void make_instruction(int i) {
	int idx;
	//opcode�� ������� opertor �ε��� ��ȣ�� ������ + nixbpe�� �����Ѵ�.(operator�� ���)
	idx = set_nixbpe(token_table[i]);
	//operator�� ���
	if (idx > 0) {
		inst* pinst = inst_table[idx];
		char* format = pinst->format;
		//opcode �� ������ ��ȯ��
		char* stop;
		int opcode = (int)strtol(pinst->opcode, &stop, 16);
		/*
		 * ���Ŀ� ���� instruction format �޸��ϱ�
		*/
		//1. 1���� : opcode -> 1byte(char)
		if (strcmp(format, "1") == 0) {
			object_table[i]->record = 'T';
			object_table[i]->instruction = opcode;
			object_table[i]->format = 1;
		}//2. 2���� : opcode<<8 | r1<<4 | r2<<4  -> 2byte(char*2)
 		else if (strcmp(format, "2") == 0) {
			char r[2];
			memset(r, 0, sizeof(r));
			//r1,r2 ���ڸ� ���ڿ��� �����Ѵ�.
			for (int j = 0; j < atoi(pinst->operand); j++) {
				r[j] = token_table[i]->operand[j][0];
			}
			//r1,r2�� ���ڿ��� �ش� Register ��ȣ�� �°� �����Ѵ�.
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
			//4. 4���� : opcode << 24 | nixbpe<<20 | addr -> 4byte
			char* operator = token_table[i]->operator;
			char* operand = token_table[i]->operand[0];
			if (operator[0] == '+') {
				int nix = token_table[i]->nixbpe;
				int adr=0;
				//#,@ ���� ���� �Ͽ� Ž��
				if (operand[0] == '#' || operand[0] == '@') {
					operand = operand + 1;
				}
				//ref_table�ȿ� �ִ� ������ Ȯ���Ѵ�.
				int idx = search_ref_table(operand);
				if (idx >= 0) {
					adr = 0;
					object_table[i]->use_ref = 1;
					object_table[i]->m_record[0] = operand;
					object_table[i]->opt[0] = '+';
				}
				//sym_table�ȿ� �ִ� ������ Ȯ���Ѵ�.
				else if (search_symbol_table_sect(operand,sect_cnt) > 0) {
					adr = search_symbol_table_sect(operand, sect_cnt);
				}
				object_table[i]->record = 'T';
				
				object_table[i]->instruction = (opcode << 24) | (nix<<20) | (adr);
				object_table[i]->format = 4;
			}
			//3. 3���� : opcode<<16 | nixbpe<<12 | addr(symtab -locctr) -> 3byte
			else {
				int nix = token_table[i]->nixbpe;
				int adr = 0;
				int symtab = 0;
				int pc = 0;
				if (object_table[i]->locctr != -1) {
					pc = object_table[i]->locctr;
				}
				if (operand!=NULL) {
					//#,@ ���� ���� �Ͽ� Ž��
					if (operand[0] == '#' || operand[0] == '@') {
						operand = operand + 1;
					}

					//operand�� ������ ��� -> p = 0 , b = 0 
					if ((nix & 2) == 0) {
						adr = atoi(operand);
					}//literal�� ��� -> p =1, b = 0
					else if (operand[0] == '=') {
						char tmp[10];
						strcpy(tmp, operand);
						char* tok = strtok(tmp, "'");
						tok = strtok(NULL, "'");
						symtab = search_literal_table(tok);
						adr = symtab - pc;
					}
					//opernad�� memory�� ��� -> p = 1, b = 0
					else {
						symtab = search_symbol_table_sect(operand, sect_cnt);
						adr = symtab - pc;
					}
				}
				object_table[i]->record = 'T';
				//adr ���� �ϰ�� ���� 12��Ʈ ���� ���� 0
				object_table[i]->instruction = (opcode << 16) | (nix << 12) | (0xFFF&adr);
				object_table[i]->format = 3;
			}
		}
	}//direcrives - instruction ���ٸ� 
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
		//CSECT�� ��
		else if (strcmp(op, "CSECT") == 0) {
			object_table[i]->record = 'H';
			sect_cnt++;
		}
		//BYTE,WORD�� ��� instruction ����
		else if (strcmp(op, "WORD") == 0 || strcmp(op, "BYTE") == 0) {
			char* opr = operand;
			//tmp : ��ū���� �ӽ����� ����, stop : strtol�������� ����
			char tmp[20];
			strcpy(tmp, opr);
			char* stop;
			
			//BYTE�� ���
			if (opr[0] == 'C' || opr[0] == 'X') {
				char* tok = strtok(tmp, "'");
				tok = strtok(NULL, "'");
				//'C'�� ���
				if (opr[0] == 'C')
				{
					int len = strlen(tok);
					for (int j = 0; j < len; j++) {
						object_table[i]->instruction = (object_table[i]->instruction << 8) | tmp[j];
						object_table[i]->format += 1;
						object_table[i]->record = 'T';
					}
				}
				//'X'�ϰ��
				else {
					object_table[i]->instruction = (int)strtol(tok, &stop, 16);
					object_table[i]->format += 1;
					object_table[i]->record = 'T';
				}
			}
			//WORD�� ���
			else {
				//Operand�� +,-���� ��
				if (strchr(opr, '+') != NULL || strchr(opr, '-') != NULL) {
					//+�� ���
					if (strchr(opr, '+') != NULL) {
						char* tok = strtok(tmp, "+");
						char* op1 = tok;//ù��° �ǿ�����
						tok = strtok(NULL, "+");
						char* op2 = tok;//�ι�° �ǿ�����
						//EXTREF���� Ȯ���Ѵ�.
						int idx1 = search_ref_table(op1);
						object_table[i]->m_record[0] = op1;
						int idx2 = search_ref_table(op2);
						object_table[i]->m_record[1] = op2;
						int TA1 = 0;
						int TA2 = 0;

						//ù��° �ǿ����ڰ� EXTREF �ƴҰ�� SYMTAB�� ã�ƺ���.
						if (idx1 < 0) {
							int sym_adr = search_symbol_table(op1);
							TA1 = sym_adr;
						}//EXTREF ����
						else {
							object_table[i]->opt[0] = '+';
							object_table[i]->use_ref = 1;
						}
						//�ι�° �ǿ�����EXTREF �ƴҰ�� STMTAB�� ã�ƺ���.
						if (idx2 < 0) {
							int sym_adr = search_symbol_table(op2);
							TA2 = sym_adr;
						}//EXTREF ����
						else {
							object_table[i]->opt[1] = '+';
							object_table[i]->use_ref = 1;
						}
						object_table[i]->record = 'T';
						object_table[i]->instruction = TA1 + TA2;
						object_table[i]->format = 3;
					}
					// -�� ���
					else if (strchr(opr, '-') != NULL) {
						//EXTREF���� Ȯ���Ѵ�.
						char* tok = strtok(tmp, "-");
						char* op1 = tok;//ù��° �ǿ�����
						tok = strtok(NULL, "-");
						char* op2 = tok;//�ι�° �ǿ�����
						//EXTREF���� Ȯ���Ѵ�.
						int idx1 = search_ref_table(op1);
						object_table[i]->m_record[0] = (char*)malloc(sizeof(strlen(op1)));
						strcpy(object_table[i]->m_record[0], op1);
						int idx2 = search_ref_table(op2);
						object_table[i]->m_record[1] = (char*)malloc(sizeof(strlen(op2)));
						strcpy(object_table[i]->m_record[1], op2);
						int TA1 = 0;
						int TA2 = 0;

						//ù��° �ǿ����ڰ� EXTREF �ƴҰ�� SYMTAB�� ã�ƺ���.
						if (idx1 < 0) {
							int sym_adr = search_symbol_table(op1);
							TA1 = sym_adr;
						}//EXTREF ����
						else {
							object_table[i]->opt[0] = '+';
							object_table[i]->use_ref = 1;
						}
						//�ι�° �ǿ�����EXTREF �ƴҰ�� STMTAB�� ã�ƺ���.
						if (idx2 < 0) {
							int sym_adr = search_symbol_table(op2);
							TA2 = sym_adr;
						}//EXTREF ��� ��
						else {
							object_table[i]->opt[1] = '-';
							object_table[i]->use_ref = 1;
						}
						object_table[i]->record = 'T';
						object_table[i]->instruction = TA1 - TA2;
						object_table[i]->format = 3;
					}
				}
				//+,- ���� ���
				else{
					object_table[i]->instruction = (int)strtol(opr, &stop, 16);
				}
			}
			
		}
		//literal�� ��쵵 instruction ���� 
		else if (strcmp(op, "LTORG") == 0 || strcmp(op, "END") == 0) {
			object_table[i]->instruction = 0;
			//liteal Table ����������
			char * ptr = literal_table[literal_cnt].literal;
			char tmp[10];
			strcpy(tmp, ptr);
			int len = strlen(tmp);
			char* stop;//16���� ���ϱ� ���� ����
			//���ڶ��
			if (object_table[i]->type == 'C') {
				object_table[i]->record = 'T';
				for (int j = 0; j < len; j++) {
					object_table[i]->instruction = (object_table[i]->instruction << 8) | tmp[j];
					object_table[i]->format += 1;
				}
			}
			//���ڶ��
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
 * ���� : �Է� ���ڿ��� ref_table�� �ִ��� Ȯ���ϴ� �Լ��̴�.
 * �Ű� : ref(operand�� ���´�)
 * ��ȯ : �������� = ���̺� ��Ī�ϴ� �ε��� , ���� < 0
 * ���� : ����
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
* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
*		   ������ ���� �۾��� ����Ǿ� ����.
*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
* �Ű� : ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
	int errno;
	int sect_cnt = 0;//�� ��° control sector���� �����ϴ� ����
	for (int i = 0; i < token_line; i++) {
		make_instruction(i);
	}

	return errno;
}

/* ----------------------------------------------------------------------------------
* ���� : Sector Control�� ���̸� �����ִ� �Լ��̴�.
* �Ű� : �� ��° Sector Control ����(0,1,2)
* ��ȯ : ���̸� ��ȯ
* ���� : 
*
* -----------------------------------------------------------------------------------
*/
int get_address_len(int sect) {
	int s_cnt = 0;//���� ���° Sector����
	int start_ad = 0;
	int end_ad = 0;

	for (int i = 0; i < obj_line; i++) {
		if (s_cnt == sect) {
			//���� �ּ�
			if (object_table[i]->record == 'H') {
				start_ad = object_table[i]->locctr;
			}
			//������ �ּ�(���� ���� H�� ��)
			if (i < obj_line-1 && object_table[i + 1]->record == 'H') {
				end_ad = object_table[i]->locctr;
				break;
			}
			//������ Section Control�� ���
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	
	char record;//record ex)H D R T M E��
	int loc;//locctr
	int inst;//objcet code
	int format;//��ɾ �� ��������
	int end = object_table[0]->locctr;//���α׷� ���� �ּ�
	int start_sect = 0;//Section control ���� ��
	
	//�������� �ʱ�ȭ : ó���ٺ��� Ž�� ����
	sect_cnt = 0;
	literal_cnt = 0;


	//��� ��ġ
	FILE* out;
	
	//file_name�� ����� ��� ǥ�� ���
	if (file_name == NULL) {
		out = stdout;
	}//"output_20160283.txt�� ���
	else
		out = fopen(file_name,"w");
	
	for (int i = 0; i < obj_line; i++) {
		record = object_table[i]->record;
		loc = object_table[i]->locctr;
		inst = object_table[i]->instruction;
		format = object_table[i]->format;

		//record�� H�� ���
		if (record == 'H') {
			//ù���� H�� �ƴ϶�� ���๮�� �־��ش�.
			if (i != 0) {
				fputs("\n\n", out);
			}
			//���α׷� �̸�
			fprintf(out, "%c%-6s",'H',token_table[i]->label);

			//���� �ּ�
			fprintf(out, "%06X", object_table[i]->locctr);

			//���α׷� ����
			int len = get_address_len(sect_cnt++);
			fprintf(out, "%06X", len);

			start_sect = i;
		}
		//record�� D�� ���
		else if (record == 'D') {
			//D : �ܺη� ������ �����̸� + ��ġ
			fputs("\n", out);
			fputs("D", out);
			for (int j = 0; j < MAX_OPERAND; j++) {
				if (token_table[i]->operand[j] != NULL) {
					int sym_loc = search_symbol_table(token_table[i]->operand[j]);
					fprintf(out, "%s%06X", token_table[i]->operand[j],sym_loc);
				}
			}
		}
		//record�� R�� ���
		else if (record == 'R') {
			//R : EXTREF �ּ��̸�
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
			int num = 0;//i������ �ǵ����� ���� ����
			int sum = 0;//�� ������ ����
			//���� ���ϱ� : 
			while ((i < obj_line) && (object_table[i]->record == 'T')) {
				
				//literal �϶�
				if (format == 0) {
					sum += object_table[i]->format;
				}
				else
					sum += object_table[i]->format;

				if (sum > 30) {
					sum -= object_table[i]->format;//�ʰ��Ȱ� �����·� �ǵ�����
					break;
				}
				num++;
				i++;
			}
			//i��ġ �ʱ�ȭ
			i -= num;
			//Object Code ���� �ּ� �� ���� ���
			fprintf(out, "%06X%02X", object_table[i - 1]->locctr, sum);

			//ObjectCode ���
			for(int j = 0 ;j<num;j++) {
				format = object_table[i]->format;
				
				if (format == 1) {
					fprintf(out, "%02X", object_table[i]->instruction);
				}
				if (format == 2) {
					fprintf(out, "%04X", object_table[i]->instruction);
				}

				//3����
				if (format == 3) {
					fprintf(out, "%06X",object_table[i]->instruction);
				}
				//4����
				if (format == 4) {
					fprintf(out, "%08X", object_table[i]->instruction);
				}
				i++;
			}
			i--;
		}
		
		//M Record �� E Record 
		if ((i != obj_line - 1 && object_table[i + 1]->record == 'H')||i== obj_line-1) {
			//M Record ���
			for (int j = start_sect; j <= i; j++) {
				//REF����� �� or ������ 4������ ��
				if (object_table[j]->use_ref == 1 || object_table[j]->format == 4) {
					int startidx = 0;
					int target_bit = 0;
					//4������ ��
					if (object_table[j]->format == 4) {
						startidx = (int)(object_table[j]->locctr - 2.5);
						target_bit = 5;
					}
					//3�����϶�
					else {
						startidx = object_table[j]->locctr - 3;
						target_bit = 6;
					}
					char* ref;
					char opt;
					for (int k = 0; k < MAX_OPERAND; k++) {
						if (object_table[j]->m_record[k] == NULL) {
							//ref���� ������ 4����
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

