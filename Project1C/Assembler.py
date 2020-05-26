# %%
from functools import reduce
from InstTable import *
from SymbolTable import *
from LIteralTable import *
from TokenTable import TokenTable
"""
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
"""

class Assembler:
    def __init__(self,filename):
        # instruction 명세를 저장한 공간 
        self.instTable = InstTable(filename)
        # 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. 
        self.lineList = list()
        # 프로그램의 section별로 symbol table을 저장하는 공간
        self.symtabList = list()
        # 프로그램의 section별로 literal table을 저장하는 공간
        self.literaltabList = list()
        # 프로그램의 section별로 프로그램을 저장하는 공간
        self.TokenList = list()
        # 최정 결과를 저장한는 공간
        self.codeList = list()
    
    def pass1(self):
        # SYMTAB과 LITTAB를 생성한 뒤  list에 넣어주기
        symtab = SymbolTable()
        littab = LiteralTable()
        self.symtabList.append(symtab)
        self.literaltabList.append(littab)

        # TokenTable 생성 뒤 SYMTAB와 LITTAB 링크해주기
        tokenTab = TokenTable(symtab,littab,self.instTable)
        self.TokenList.append(tokenTab)

        for line in self.lineList:
            if line[0] == '.':
                continue

            if 'CSECT' in line:
                symtab = SymbolTable()
                littab = LiteralTable()
                tokenTab = TokenTable(symtab,littab,self.instTable)
                self.symtabList.append(symtab)
                self.literaltabList.append(littab)
                self.TokenList.append(tokenTab)

            tokenTab.putToken(line)


    # inputFile을 읽어들여서 lineList에 저장한다.
    def loadInputFile(self,inputFile):
        with open(inputFile,'r') as file:
            for line in file.readlines():
                self.lineList.append(line)
    
    # 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
    def printSymbolTable(self,filename=None):
        with open(filename,'w') as file:
            for st in self.symtabList:
                for s in st.symtab:
                    file.write("{0:6}\t{1:6X}\n".format(s,st.search(s)))
                file.write('\n')

    def printLiteralTable(self,filename=None):
        with open(filename,'w') as file:
            for littab in self.literaltabList:
                for lit in littab.litTab:
                    file.write("{0:6}\t{1:6X}\n".format(lit,littab.search(lit)))

    def pass2(self):
        for tt in self.TokenList:
            outputList = list()
            i = 0
            while i < len(tt.tokenList):
            # for i in range(len(tt.tokenList)):
                tt.makeObjectcode(i)
                t = tt.tokenList[i]
                # H Record 출력
                if t.record is 'H':
                    outputList.append('H')
                    # 프로그램 이름
                    outputList.append('{0:6}'.format(t.label))
                    # 프로그램 시작 주소
                    outputList.append('{0:06}'.format(t.locctr))
                    # 프로그램 길이
                    outputList.append('{0:06X}'.format(reduce(lambda x,y: x+y,map(lambda x: x.byteSize,tt.tokenList))))
                    outputList.append('\n')
                # D Record 출력
                elif t.record is 'D':
                    outputList.append('D')
                    for extdef in t.operand:
                        outputList.append('{0:6}'.format(extdef))
                    outputList.append('\n')
                # R Record 출력
                elif t.record is 'R':
                    outputList.append('R')
                    for extref in tt.refList:
                        outputList.append('{0:6}'.format(extref))
                    outputList.append('\n')
                # T Record 출력
                elif t.record is 'T':
                    outputList.append('T')
                    # 시작 주소 출력
                    outputList.append('{0:06X}'.format(tt.tokenList[i-1].locctr))

                    line_sum = 0 # 해당 라인의 길이
                    idx_t = 0 # 현재 토큰 인덱스
                    for j in range(i,len(tt.tokenList)):
                        tt.makeObjectcode(j)
                        if tt.tokenList[j].objectCode == None and tt.tokenList[j].record is not 'T':
                            idx_t = j
                            break
                        line_sum += tt.tokenList[j].byteSize
                        if line_sum >30 :
                            idx_t = j
                            line_sum -= tt.tokenList[j].byteSize
                            break
                        if j == len(tt.tokenList) -1:
                            idx_t = j+1
                    # line의 크기 출력
                    outputList.append('{0:02X}'.format(line_sum))
                    # object Code 출력
                    for j in range(i,idx_t):
                        outputList.append(tt.tokenList[j].objectCode)
                    i = idx_t -1
                    outputList.append('\n')
                elif t.record is 'E':
                    pass

                # M 출력
                if i is len(tt.tokenList) -1:
                    for tok in tt.tokenList:
                        for extref in range(tok.memCnt):
                            outputList.append('M')
                            outputList.append('{0:06X}'.format(tok.locctr-3)) # 시작 주소
                            outputList.append('05' if tok.byteSize is 4 else '06') # 위치
                            spliter = '+' if '+' in tok.operand[0] or extref is 0 else '-' # 출력할 부호
                            outputList.append(spliter) 
                            spliter = '-' if '-' in tok.operand[0] else '+' # 토큰화 변수
                            outputList.append(tok.operand[0].split(spliter)[extref]) # 변수 이름
                            outputList.append('\n')
                    outputList.append('E')
                    if tt.isMain:
                        outputList.append('{0:06X}'.format(0))
                    outputList.append('\n')

                i += 1
            outputList.append('\n')
            self.codeList.append("".join(outputList))
    
    def printObjectCode(self,fileName=None):
        with open(fileName,'w') as file:
            for obj in self.codeList:
                file.write(obj)

            
# %% main
assembler = Assembler("inst.data")
assembler.loadInputFile("input.txt")
assembler.pass1()
assembler.printSymbolTable("symtab_20160283.txt")
assembler.printLiteralTable("literaltab_20160283.txt")
assembler.pass2()
assembler.printObjectCode('output_20160283.txt')


# %%
