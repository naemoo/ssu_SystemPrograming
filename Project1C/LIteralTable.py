"""
 * literal과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
"""
class LiteralTable:
    # Literal과 이에 대응하는 주소를 dictionary 구조로 저장한다
    def __init__(self):
        self.litTab = dict()
    # 신규 literal을 저장하고 그에 해당하는 정보를 저장한다 ex) location, C인지 X인지
    # 기존 literal일 경우 수정한다.
    # Imformation은 list구조이고 [address,Type]을 저장한다.
    def putLiteral(self,literal,litImformation):
        self.litTab[literal] = litImformation
    
    # literal의 주소를 반환한다. 없다면 -1을 반환
    def search(self,literal):
        return self.litTab[literal][0] if literal in self.litTab else -1
    