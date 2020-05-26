# %%
from functools import reduce
"""
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
"""
class TokenTable:
    # 최대 operand 갯수
    MAX_OPERAND = 3
    # bit 조작의 가독성을 위한 선언
    nFLAG = 32;
    iFLAG = 16;
    xFLAG = 8;
    bFLAG = 4;
    pFLAG = 2;
    eFLAG = 1;
    FORM ={1:'{0:02X}',2:'{0:04X}',3:'{0:06X}',4:'{0:08X}'}
    # symTab,litTab,instTab 링킹한다.
    # tokenList는 각 line을 토큰화한 후 저장
    def __init__(self,symTab,litTab,instTab):
        self.symtab = symTab
        self.litTab = litTab
        self.instTab = instTab
        self.tokenList = list()
        self.isMain = False
        self.locctr = 0
        #reg 번호 저장하는 dictoinary
        self.regTab = {'A':0,'X':1,'L':2,'B':3,'S':4,'T':5,'F':6,}
        #EXTREF를 저장하는 list
        self.refList = list()
    
    # 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
    def putToken(self,line):
        t = Token(line)
        self.tokenList.append(t)

        # H Record 설정
        if t.operator == 'START' or t.operator == 'CSECT':
            t.record = 'H'
            self.isMain = True if t.operator == 'START' else False

        # SYMTAB 채우기
        if t.label is not None and t.label !=  '':
            self.symtab.putSymbol(t.label,self.locctr)
        # LITTAB 채우기
        if t.operand != None and t.operand[0][0] == '=':
            tp = t.operand[0][1]
            lit = t.operand[0][3:-1]
            self.litTab.putLiteral(lit,[lit,tp])
        
        """
        * LOCCTR 증가
        """
        # 1,2,3 형식 명령어
        if self.instTab.getOpcode(t.operator) != -1:
            operator = t.operator
            t.byteSize = self.instTab.getFormat(operator)
            self.locctr += self.instTab.getFormat(operator)
            t.record = 'T'

            # RSUB가 아닌 명령어
            if(operator != 'RSUB'):
                t.setFlag(TokenTable.pFLAG,1)
                # operand에 #이 들어갈 때
                if t.operand[0][0]=='#':
                    t.setFlag(TokenTable.iFLAG,1)
                    if t.operand[0][1:].isdigit():
                        t.setFlag(TokenTable.pFLAG,0)
                    t.operand[0] = t.operand[0][1:]
                # operand에 @이 들어갈 때
                elif t.operand[0][0]=='@':
                    t.setFlag(TokenTable.nFLAG,1)
                    if t.operand[0][1:].isdigit():
                        t.setFlag(TokenTable.pFLAG,0)
                    t.operand[0] = t.operand[0][1:]
                # 그렇지 않은 경우
                else:
                    t.setFlag(TokenTable.iFLAG,1)
                    t.setFlag(TokenTable.nFLAG,1)
                # Xbit = 1인 경우
                if len(t.operand) >= 2 and t.operand[1] == 'X':
                    t.setFlag(TokenTable.xFLAG,1)
            # RSUB일 경우
            else :
                t.setFlag(TokenTable.iFLAG,1)
                t.setFlag(TokenTable.nFLAG,1)
                
        elif t.operator == 'LTORG' or t.operator == 'END':
            t.record = 'T'
            for i in self.litTab.litTab:
                # address 수정
                self.litTab.litTab[i][0] = self.locctr
                if self.litTab.litTab[i][1] == 'C':
                    self.locctr += len(i)
                    t.byteSize += len(i)
                elif self.litTab.litTab[i][1] == 'X':
                    self.locctr += len(i)//2
                    t.byteSize += len(i)//2
        # 4형식일 경우
        elif t.operator[0] == '+':
            self.locctr += 4
            t.setFlag(TokenTable.eFLAG,1)
            t.setFlag(TokenTable.nFLAG,1)
            t.setFlag(TokenTable.iFLAG,1)
            t.byteSize = 4
            t.record = 'T'

            if len(t.operand) >= 2 and t.operand[1] == 'X':
                t.setFlag(TokenTable.xFLAG,1)
        # RESW, RESB,WORD,BYTE,EXTREF,EXTDEF,START 처리
        elif t.operator == 'RESW':
            self.locctr += int(t.operand[0])*3
            t.byteSize = int(t.operand[0])*3
        elif t.operator == 'RESB':
            self.locctr += int(t.operand[0])
            t.byteSize = int(t.operand[0])
        elif t.operator == 'WORD':
            self.locctr += 3
            t.byteSize = 3
            t.record = 'T'
        elif t.operator == 'BYTE':
            t.record = 'T'
            if t.operand[0][0] == 'C':
                self.locctr += len(t.operand[0][2:-1])
                t.byteSize = len(t.operand[0][2:-1])
            elif t.operand[0][0] == 'X':
                self.locctr += len(t.operand[0][2:-1])//2
                t.byteSize = len(t.operand[0][2:-1])//2
        elif t.operator == 'EXTDEF':
            t.record = 'D'
        elif t.operator == 'EXTREF':
            t.record = 'R'
            self.refList = [r for r in t.operand]
        elif t.operator == 'START':
            self.isMain = True

        t.locctr = self.locctr

        # EQU일 경우
        if t.operator == "EQU":
            # *가 아닌 경우
            if '*' not in t.operand[0]:
                address = 0
                spliter = '+' if '+' in t.operand[0] else '-'
                tok = t.operand[0].split(spliter)
                if spliter == '+':
                    address = reduce(lambda x,y: x+y,map(self.symtab.search,tok))
                elif spliter == '-':
                    address = reduce(lambda x,y: x-y,map(self.symtab.search,tok))
                self.symtab.putSymbol(t.label,address)    
                t.locctr = address

    def makeObjectcode(self,idx):
        t = self.tokenList[idx]
        # objectcode
        i = 0
        if t.operator == "RSUB":
            t.objectCode = '4F0000'
            return
        # Opcode가 있는 operator 
        if self.instTab.getOpcode(t.operator) >= 0 or '+' in t.operator: 
            if t.byteSize is 1:
                i = self.instTab.getOpcode(t.operator)
            elif t.byteSize is 2:
                # operand가 2개일 경우
                try:
                    i = (self.instTab.getOpcode(t.operator)<<8)|(self.regTab[t.operand[0]]<<4)|(self.regTab[t.operand[1]])
                # operand가 1개일 경우
                except:
                    i = (self.instTab.getOpcode(t.operator)<<8)|(self.regTab[t.operand[0]]<<4)
            elif t.byteSize is 3:
                adr = 0
                # operand가 symbol일 경우
                if self.symtab.search(t.operand[0]) is not -1:
                    adr = self.symtab.search(t.operand[0]) - t.locctr
                # operand가 literal일 경우 
                elif '=' in t.operand[0]:
                    adr = self.litTab.search(t.operand[0][3:-1]) - t.locctr
                # operand가 숫자인 경우
                else:
                    adr = int(t.operand[0])
                i = (self.instTab.getOpcode(t.operator)<<16)|(t.nixbpe<<12)|(0xFFF&adr)
            elif t.byteSize is 4:
                adr = 0 
                if self.symtab.search(t.operand[0]) is not -1:
                    adr = self.symtab.search(t.operand[0])
                elif '=' in t.operand[0]:
                    adr = self.litTab.search(t.operand[0][3:-1])
                elif t.operand[0] in self.refList:
                    adr = 0
                else:
                    adr = int(t.operand[0])
                i = (self.instTab.getOpcode(t.operator[1:])<<24)|(t.nixbpe<<20)|(adr)
                t.memCnt += 1
            t.objectCode = TokenTable.FORM[t.byteSize].format(i)
        # Opcode가 없는 operator
        else:
            if t.operator == 'BYTE':
                if t.operand[0][0] == 'X':
                    i = int(t.operand[0][2:-1],16)
                    t.objectCode = TokenTable.FORM[t.byteSize].format(i)
                elif t.operand[0][0] == 'C':
                    t.objectCode = ''.join(list(map(lambda x:'{0:X}'.format(x),map(ord,[e for e in t.operand[0][2:-1]]))))
                return
            elif t.operator == 'WORD':
                # + - 연산이 있을 경우
                if '+' in t.operand[0] or '-' in t.operand[0]:
                    spliter = '+' if '+' in t.operand[0] else '-'
                    tok = t.operand[0].split(spliter)
                    # -일 경우 i에 첫번째 수를 2번 더해주고 시작한다.
                    if spliter is '-':
                        i = self.symtab.search(tok[0])*2 if self.symtab.search(tok[0]) is not -1 else 0
                    f1 = lambda x,y:x+y
                    f2 = lambda x,y:x-y
                    for itr in tok:
                        if self.symtab.search(itr) is not -1:
                            itr = self.symtab.search(itr)
                        elif itr in self.refList:
                            t.memCnt += 1
                            itr = 0 
                        if spliter is '+': 
                            i = reduce(f1,[i,itr])
                        else:
                            i = reduce(f2,[i,itr])
                # 숫자일 경우
                if t.operand[0].isdigit():
                    i = int(t.operand[0])
            elif (t.operator == 'LTORG') or (t.operator == 'END'):
                for lit in self.litTab.litTab:
                    if self.litTab.litTab[lit][1] is 'X':
                        t.objectCode = '{0:02X}'.format(int(lit,16))
                    else:
                        t.objectCode = ''.join(list(map(lambda x: '{0:X}'.format(x),map(ord,[e for e in lit]))))
                    return
            elif (t.operator == 'RESW') or (t.operator == 'RESB') or (t.operator == 'EQU'):
                t.objectCode = None
                return
            # objectCode가 없는 경우 None
            try:
                t.objectCode = TokenTable.FORM[t.byteSize].format(i)
            except:
                t.objectCode = None

            

# %%
"""
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
"""
class Token:
    # label, operator, operand,locctr를 저장
    def __init__(self,line):
        tok = line.split("\t")
        self.label = tok[0].strip() # label 명
        self.operator = tok[1].strip() # operator 명
        try:
            self.operand = tok[2].strip().split(",") if tok[2] != '' else None # operand를 ,단위로 토큰화 진행하여 저장한다.
        except:
            self.operand = None # 없을 경우 None
        self.locctr = 0 # locctr
        self.byteSize = 0 # 각 명령어 크기 
        self.memCnt = 0 # EXTREF를 사용하는 횟수
        self.nixbpe = 0 # nixbpe의 값 저장
        self.record = 'E' # Record ex) H,T,
        self.objectCode = ""

    # n,i,x,b,p,e flag를 설정한다.
    def setFlag(self,flag,value):
        self.nixbpe += flag if value == 1 else -flag
    # 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다
    # getFlag(nFlag|iFlag)
    def getFlag(self,flag):
        return self.nixbpe&flag
