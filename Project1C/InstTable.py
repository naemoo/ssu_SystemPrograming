"""
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
"""
class InstTable:
    # Dictionary 구조에 명령어를 저장
    def __init__(self,filename):
        self.instMap = dict()
        self.openFile(filename)

    # filename을 입력 받아 Instruction Instance 생성
    def openFile(self,filename):
        with open(filename,'r') as file:
            for line in file.readlines():
                i = instruction(line)
                self.instMap[i.instruction] = i;
    
    # 입력받은 명령어의 format을 return 
    def getFormat(self,str):
        return self.instMap[str].format

    # 입력받은 명령어의 opcode를 출력 올바르지 않은 입력이면 -1 반환
    def getOpcode(self,str):
        try:
            return self.instMap[str].opcode
        except:
            return -1

"""
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
"""
class instruction:
    # 명령어 이름, 인자 갯수, format, opcode를 저장한다.
    def __init__(self,line):
        tok = line.split("\t")
        self.instruction = tok[0]
        self.numberOfOperand = int(tok[1])
        self.format = 3 if tok[2] == '3/4' else int(tok[2])
        self.opcode = int(tok[3],16)