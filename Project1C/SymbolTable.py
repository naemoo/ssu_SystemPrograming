"""
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
"""
class SymbolTable:
    # Symbol과 이에 대응하는 location을 저장한다.
    def __init__(self):
        self.symtab = dict()

    # 새로운 symbol은 저장하고 기존 symbol은 수정한다.
    def putSymbol(self,symbol,location):
        self.symtab[symbol] = location
    
    # symbol의 주소를 반환한다 없으면 -1을 반환한다.
    def search(self,symbol):
        return self.symtab[symbol] if symbol in self.symtab else -1
        