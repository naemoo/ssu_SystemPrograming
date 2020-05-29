package SP20_simulator;

import java.util.Arrays;

// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

public class InstLuncher {
	int disp;
	int ta;
	int val;
    ResourceManager rMgr;
    Instruction inst;
    public InstLuncher(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
    }

    public void execInst(Instruction inst) {
    	int opcode = inst.opcode;
    	this.inst = inst;
    	disp = 0;
    	ta = 0;
    	val = 0;
    	
		switch(opcode) {
		// STL일 경우
		case 0x14:
			stl();
			break;
		//JSUB
		case 0x48:
			jsub();
			break;
		//LDA
		case 0x00:
			lda();
			break;
		//COMP
		case 0x28:
			comp();
			break;
		//JEQ
		case 0x30:
			jeq();
			break;
		//J
		case 0x3C:
			j();
			break;
		//STA
		case 0x0C:
			sta();
			break;
		//LDT
		case 0X74:
			ldt();
			break;
		//TD
		case 0XE0:
			td();
			break;
		//RD
		case 0XD8:
			rd();
			break;
		//COMPR
		case 0XA0:
			compr();
			break;
		//STCH
		case 0X54:
			stch();
			break;
		//TIXR
		case 0XB8:
			tixr();
			break;
		//JLT
		case 0X38:
			jlt();
			break;
		//STX
		case 0X10:
			stx();
			break;
		//RSUB
		case 0X4C:
			rsub();
			break;
		//CLEAR
		case 0XB4:
			clear();
			break;
		//LDCH
		case 0X50:
			ldch();
			break;
		//WD
		case 0XDC:
			wd();
			break;
		}
    }
    public void stl() {
    	// m..m+2 <- LReg
    	char[] data = rMgr.intToChar(rMgr.getRegister(2), 3);
    	rMgr.setMemory(inst.ta, data,3);
    }
    public void jsub() {
    	// L <- (PC); PC <- m
    	int pc = rMgr.getRegister(8);
    	rMgr.setRegister(2,pc);
    	rMgr.setRegister(8, inst.ta);
    }
    public void lda() { 
    	// AReg <- m..m+2
    	rMgr.setRegister(0, inst.val);
    }
    public void comp() { 
    	// A : m..m+2
    	int regA = rMgr.getRegister(0);
    	int m = inst.val;
    	if(regA != m) {
    		rMgr.setRegister(9, -1);
    	}
    	else
    		rMgr.setRegister(9, 0);
    }
    public void jeq() {
    	// PC <- m if CC set to 0
    	if(rMgr.getRegister(9) == 0) {
    		rMgr.setRegister(8, inst.ta);
    	}
    }
    public void j() {
    	// pc<- m
    	rMgr.setRegister(8, inst.ta);
    	
    	if(rMgr.getRegister(8) == 0) {
    		rMgr.end = true;
    		rMgr.closeDevice();
    	}
    	
    }
    public void sta() {
    	// m..m+2 <- regA
    	char[] data = rMgr.intToChar(rMgr.getRegister(0), 3);
    	rMgr.setMemory(inst.ta, data,3);
    }
    public void ldt() { 
    	// TReg <- m...m+2   m = Mem(TA)
    	rMgr.setRegister(5, inst.val);
    }
    public void td() { 
    	// TestDevice(m)
    	int m = rMgr.byteToInt(rMgr.getMemory(inst.ta, 1));
    	rMgr.testDevice(String.format("%02X", m));
    }
    public void rd() {
    	// m : DevName 
    	char dev = rMgr.getMemory(inst.ta, 1)[0];
    	String devName = String.format("%02X", (int)dev);
    	char[] readData = rMgr.readDevice(devName,1);
    	rMgr.setRegister(0, rMgr.byteToInt(readData));
    }
    public void stch() {
    	// m <- A
    	rMgr.setMemory(inst.ta, rMgr.intToChar(rMgr.getRegister(0), 1), 1);
    }
    public void compr() {
    	// r1:r2
    	// r1과 r2 같다면 0 설정
    	if(rMgr.getRegister(inst.r1)==rMgr.getRegister(inst.r2)) {
    		rMgr.setRegister(9,0);
    	}
    	// 같지 않으면 1 설정
    	else
    		rMgr.setRegister(9, 1);
    }
    public void tixr() {
    	// X++; X:r1
    	rMgr.setRegister(1, rMgr.getRegister(1)+1);
    	if(rMgr.getRegister(1) == rMgr.getRegister(inst.r1)) {
    		rMgr.setRegister(9, 0);
    	}
    	else {
    		rMgr.setRegister(9, -1);
    	}
    }
    public void jlt() {
    	// pc <- m if CC<0 
    	if(rMgr.getRegister(9) != 0) {
    		rMgr.setRegister(8, inst.ta);
    	}
    }
    public void stx() {
    	// m..m+2 <- X
    	rMgr.setMemory(inst.ta, rMgr.intToChar(rMgr.getRegister(1), 3), 3);
    }
    public void rsub() {
    	// pc <- LReg
    	rMgr.setRegister(8, rMgr.getRegister(2));
    }
    public void clear() {
    	// r1 <- 0
    	rMgr.setRegister(inst.r1, 0);
    }
    public void ldch() {
    	// A Reg <- m
    	int val = rMgr.byteToInt(rMgr.getMemory(inst.ta, 1));
    	rMgr.setRegister(0, val);
    }
    public void wd() {
    	// A -> Device
    	char dev = rMgr.getMemory(inst.ta, 1)[0];
    	String devName = String.format("%02X", (int)dev);
    	char[] data = rMgr.intToChar(rMgr.getRegister(0), 1);
    	rMgr.writeDevice(devName, data, 1);
    }
    
}

