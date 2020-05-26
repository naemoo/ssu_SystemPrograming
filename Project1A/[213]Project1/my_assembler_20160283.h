/* 
 * my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�. 
 * 
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

/*
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ������ ������ instruction set�� ��Ŀ� ���� ���� �����ϵ�
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
 */
struct inst_unit
{//appendix ����
    char* mnemonic;//mnemonic code
    char* operand;//oprand ����
    char* format;//����
    char* opcode;//opcode
};

// instruction�� ������ ���� ����ü�� �����ϴ� ���̺� ����
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;

/*
 * ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
 */
struct token_unit
{
	char *label;				//��ɾ� ���� �� label
	char *operator;				//��ɾ� ���� �� operator
	char *operand[MAX_OPERAND]; //��ɾ� ���� �� operand
	char *comment;				//��ɾ� ���� �� comment
	char nixbpe;				//���� 6bit ���: _ _ n i x b p e
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;

/*
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 */
struct symbol_unit
{
	char symbol[10];
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];
//sym_table_line�� ������ �����ϱ� ���� ����
static int sym_line;

/*
* ���ͷ��� �����ϴ� ����ü�̴�.
* ���ͷ� ���̺��� ���ͷ��� �̸�, ���ͷ��� ��ġ�� �����ȴ�.
*/
struct literal_unit
{
	char literal[10];
	int addr;
};

typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
//literal_table�� ������ �����ϱ� ���� ����
static int lit_line;

static int locctr;

//object code�� �ۼ��ϱ� ���� ���̺� ����
struct object_unit
{
    char type;
    char record;//RECORD
    char * m_record[MAX_OPERAND];//EXTREF ����ϴ� ��� ���� �̸��� ����
    int instruction;//object code
    int locctr;//LOCTTR
    int format;//�� ��������
    char opt[MAX_OPERAND];
    char use_ref;
};

typedef struct object_unit object;
object * object_table[MAX_LINES];
static int obj_line;

//EXTREF�� �����ϴ� list
char* ref_list[MAX_OPERAND];
//object code ���� �� �� ��° Sector control
static int sect_cnt;
static int literal_cnt;


//--------------

static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int token_parsing(char *str);
int search_opcode(char *str);
static int assem_pass1(void);
//void make_opcode_output(char *file_name);

void make_symtab_output(char *file_name);
void make_literaltab_output(char *file_name);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);

void make_symtab();
int search_symbol_table(char* label);
int search_symbol_table_sect(char* label, int sect);
int search_literal_table(char* literal);
int set_nixbpe(token* tok);
void make_instruction(int idx);
int search_ref_table(char* ref);
int get_address_len(int sect);