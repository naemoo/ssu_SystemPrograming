package SP20_simulator;

import java.util.Arrays;
import java.util.stream.IntStream;

/*
 * ��ɾ ���� ���� ����
 */
public class Instruction {
	ResourceManager rMgr;
	char[] objectCode;
	int opcode;
	String opName;
	int format;
	boolean nReg;
	boolean iReg;
	boolean xReg;
	boolean bReg;
	boolean pReg;
	boolean eReg;
	
	// 3����, 4���� �� ���
	int disp;
	int ta;
	int val;
	
	// 2���Ŀ� ���
	int r1;
	int r2;
	
	public Instruction(int opcode,ResourceManager resourceManager) throws Exception{
		rMgr = resourceManager;
		disp = ta = val = 0;
		r1 = r2 = 0;
		setOP(opcode);
		setFlag();
	}
	
	// ��ɾ� �̸��� ������ �Լ� �� opcode�� ����
	public void setOP(int opcode) throws Exception{
		this.opcode = opcode&0xFC; // bit Masking : OPcode�� ����
		switch(this.opcode) {
		// COMPR�� ���
		case 0xA0:
			this.opName = "COMPR";
			this.format = 2;
			break;
		// TIXR�� ���
		case 0xB8:
			this.opName = "TIXR";
			format = 2;
			break;
		// CLEAR�� ���
		case 0xB4:
			this.opName = "CLEAR";
			format = 2;
			break;
		// STL�� ���
		case 0x14:
			this.opName = "STL";
			format = 3;
			break;
		//JSUB
		case 0x48:
			this.opName = "JSUB";
			format = 3;
			break;
		//LDA
		case 0x00:
			this.opName = "LDA";
			format = 3;
			break;
		//COMP
		case 0x28:
			this.opName = "COMP";
			format = 3;
			break;
		//JEQ
		case 0x30:
			this.opName = "JEQ";
			format = 3;
			break;
		//J
		case 0x3C:
			this.opName = "J";
			format = 3;
			break;
		//STA
		case 0x0C:
			this.opName = "STA";
			format = 3;
			break;
		//LDT
		case 0X74:
			this.opName = "LDT";
			format = 3;
			break;
		//TD
		case 0XE0:
			this.opName = "TD";
			format = 3;
			break;
		//RD
		case 0XD8:
			this.opName = "RD";
			format = 3;
			break;
		//STCH
		case 0X54:
			this.opName = "STCH";
			format = 3;
			break;
		//JLT
		case 0X38:
			this.opName = "JLT";
			format = 3;
			break;
		//STX
		case 0X10:
			this.opName = "STX";
			format = 3;
			break;
		//RSUB
		case 0X4C:
			this.opName = "RSUB";
			format = 3;
			break;
		//LDCH
		case 0X50:
			this.opName = "LDCH";
			format = 3;
			break;
		//WD
		case 0XDC:
			this.opName = "WD";
			format = 3;
			break;
		default:
			throw new Exception();
		}
	}
	
	public void setFlag() {
		int pc = rMgr.getRegister(8);
		if(this.format ==3) {
			objectCode = rMgr.getMemory(pc, 3);
			boolean e= (rMgr.byteToInt(objectCode) & 0x1000) != 0;
			//4������ ���
			if(e) {
				objectCode = rMgr.getMemory(pc, 4);
				rMgr.setRegister(8, pc+4);
				pc += 4;
			}
			else {
				rMgr.setRegister(8, pc+3);
				pc += 3;
			}
		}
		else if(this.format==2) {
			objectCode = rMgr.getMemory(pc, 2);
			rMgr.setRegister(8, pc+2);
			pc += 2;
			r1 = (objectCode[1]&0xF0)>>4;
			r2 = (objectCode[1]&0x0F);
			rMgr.targetAddress = 0;
			return;
		}
		nReg = (objectCode[0] & 0x02) != 0;
    	iReg = (objectCode[0] & 0x01) != 0;
    	xReg = (objectCode[1] & 0x80) != 0;
    	bReg = (objectCode[1] & 0x40) != 0;
    	pReg = (objectCode[1] & 0x20) != 0;
    	eReg = (objectCode[1] & 0x10) != 0;
    	
    	// disp ���ϱ� (eȮ��)
    	// 4������ ��
    	if(this.eReg) {
    		disp = rMgr.byteToInt(Arrays.copyOfRange(objectCode, 1, 4));
    		disp = disp&0xFFFFF;
    		ta = disp;
    	}
    	// 3���� �� ��
    	else {
    		disp = rMgr.byteToInt(Arrays.copyOfRange(objectCode, 1, 3));
    		disp = disp&0xFFF;
    	}
    	
    	// ta ���ϱ� (b,p,x Ȯ��)
    	if(this.bReg) {
    		int base = rMgr.getRegister(3);
    		ta = base + disp;
    	}
    	else if(this.pReg) {
    		// PC�� ���� ������ �ִ�.
    		if((disp&0x800)!= 0) {
    			disp |= 0xfffff000; 
    		}
    		ta = pc + disp;
    	}
    	else {
    		ta = disp;
    	}
    	
    	if(this.xReg) {
    		ta += rMgr.getRegister(1); 
    	}
    	
    	rMgr.targetAddress = ta;
    	
    	// val ���ϱ�(n,i Ȯ��)
    	// n = 1, i = 1 �� ��
    	if(this.nReg && this.iReg) {
    		char[] valMem = rMgr.getMemory(ta, 3);
    		val = rMgr.byteToInt(valMem);
    	}
    	// n = 1, i = 0 �� ��
    	else if(this.nReg) {
    		char[] valMem = rMgr.getMemory(ta, 3);
    		ta = rMgr.byteToInt(valMem);
    		rMgr.targetAddress = ta;
    		valMem = rMgr.getMemory(ta, 3);
    		val = rMgr.byteToInt(valMem);
    	}
    	// n = 0, i = 1 �� ��
    	else if(this.iReg) {
    		val = ta;
    	}
	}
	public int getFormat() {
		return format;
	}
}
