package SP20_simulator;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator extends JFrame implements ActionListener{
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	
	// GUI를 위한 Component Resource
	private JPanel contentPane;
	private JTextField fileNmaeTF;
	private JTextField programNameTF;
	private JTextField startProgramTF;
	private JTextField lengthProgramTF;
	private JTextField regADecTF;
	private JTextField regAHexTF;
	private JTextField regXDecTF;
	private JTextField regXHexTF;
	private JTextField regLHexTF;
	private JTextField regLDecTF;
	private JTextField regPCDecTF;
	private JTextField regPCHexTF;
	private JTextField regSWTF;
	private JPanel panel_2;
	private JTextField regBDecTF;
	private JTextField regBHexTF;
	private JTextField regSHexTF;
	private JTextField regSDecTF;
	private JTextField regTDecTF;
	private JTextField regTHexTF;
	private JTextField regFTF;
	private JPanel panel_3;
	private JTextField firstAddressTF;
	private JTextField startAddressMemTF;
	private JTextField targetAddressTF;
	private JTextField usingDeviceTF;
	private JButton oneStepBTN;
	private JButton allStepBTN;
	private JButton exitBTN;
	private JList<String> instList;
	private JList<String> logList;
	private JButton openBTN;
	private JScrollPane jsp;
	
	// VisualSimulator 생성자
	public VisualSimulator() {
		makeFrame();
		listen();
	}
	// GUI 생성 메소드
	private void makeFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 511, 731);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblFileName = new JLabel("File Name :");
		lblFileName.setBounds(22, 10, 73, 15);
		contentPane.add(lblFileName);
		
		fileNmaeTF = new JTextField();
		fileNmaeTF.setBounds(109, 7, 136, 21);
		contentPane.add(fileNmaeTF);
		fileNmaeTF.setColumns(10);
		
		openBTN = new JButton("open");
		openBTN.setBounds(257, 6, 91, 23);
		contentPane.add(openBTN);
		
		JPanel panel = new JPanel();
		panel.setBounds(22, 38, 223, 117);
		Border border = BorderFactory.createTitledBorder("H (Header Record)");
		panel.setBorder(border);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Program Name : ");
		lblNewLabel.setBounds(12, 23, 96, 15);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("<HTML>Start Address of<br/> Object Program : <HTML/>");
		lblNewLabel_1.setBounds(12, 48, 96, 34);
		panel.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Length Program : ");
		lblNewLabel_2.setBounds(12, 92, 106, 15);
		panel.add(lblNewLabel_2);
		
		programNameTF = new JTextField();
		programNameTF.setBounds(120, 23, 86, 21);
		panel.add(programNameTF);
		programNameTF.setColumns(10);
		programNameTF.setEditable(false);
		
		startProgramTF = new JTextField();
		startProgramTF.setBounds(120, 61, 86, 21);
		panel.add(startProgramTF);
		startProgramTF.setColumns(10);
		startProgramTF.setEditable(false);
		
		lengthProgramTF = new JTextField();
		lengthProgramTF.setBounds(120, 92, 86, 21);
		panel.add(lengthProgramTF);
		lengthProgramTF.setColumns(10);
		lengthProgramTF.setEditable(false);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(22, 165, 223, 178);
		border = BorderFactory.createTitledBorder("Register");
		panel_1.setBorder(border);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel_3 = new JLabel("Dec");
		lblNewLabel_3.setBounds(84, 23, 50, 15);
		panel_1.add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel("Hex");
		lblNewLabel_4.setBounds(163, 23, 50, 15);
		panel_1.add(lblNewLabel_4);
		
		regADecTF = new JTextField();
		regADecTF.setBounds(70, 45, 67, 21);
		panel_1.add(regADecTF);
		regADecTF.setColumns(10);
		regADecTF.setEditable(false);
		
		regAHexTF = new JTextField();
		regAHexTF.setColumns(10);
		regAHexTF.setBounds(146, 45, 67, 21);
		panel_1.add(regAHexTF);
		regAHexTF.setEditable(false);
		
		regXDecTF = new JTextField();
		regXDecTF.setColumns(10);
		regXDecTF.setBounds(70, 70, 67, 21);
		panel_1.add(regXDecTF);
		regXDecTF.setEditable(false);
		
		regXHexTF = new JTextField();
		regXHexTF.setColumns(10);
		regXHexTF.setBounds(146, 70, 67, 21);
		panel_1.add(regXHexTF);
		regXHexTF.setEditable(false);
		
		regLHexTF = new JTextField();
		regLHexTF.setColumns(10);
		regLHexTF.setBounds(146, 95, 67, 21);
		panel_1.add(regLHexTF);
		regLHexTF.setEditable(false);
		
		regLDecTF = new JTextField();
		regLDecTF.setColumns(10);
		regLDecTF.setBounds(70, 95, 67, 21);
		panel_1.add(regLDecTF);
		regLDecTF.setEditable(false);
		
		regPCDecTF = new JTextField();
		regPCDecTF.setColumns(10);
		regPCDecTF.setBounds(70, 120, 67, 21);
		panel_1.add(regPCDecTF);
		regPCDecTF.setEditable(false);
		
		regPCHexTF = new JTextField();
		regPCHexTF.setColumns(10);
		regPCHexTF.setBounds(146, 120, 67, 21);
		panel_1.add(regPCHexTF);
		regPCHexTF.setEditable(false);
		
		regSWTF = new JTextField();
		regSWTF.setBounds(70, 145, 143, 21);
		panel_1.add(regSWTF);
		regSWTF.setColumns(10);
		regSWTF.setEditable(false);
		
		JLabel lblNewLabel_5 = new JLabel("A(#0)");
		lblNewLabel_5.setBounds(8, 48, 67, 15);
		panel_1.add(lblNewLabel_5);
		
		JLabel lblNewLabel_6 = new JLabel("X(#1)");
		lblNewLabel_6.setBounds(8, 73, 67, 15);
		panel_1.add(lblNewLabel_6);
		
		JLabel lblNewLabel_7 = new JLabel("L(#2)");
		lblNewLabel_7.setBounds(8, 98, 67, 15);
		panel_1.add(lblNewLabel_7);
		
		JLabel lblNewLabel_8 = new JLabel("PC(#8)");
		lblNewLabel_8.setBounds(8, 123, 67, 15);
		panel_1.add(lblNewLabel_8);
		
		JLabel lblNewLabel_9 = new JLabel("SW(#9)");
		lblNewLabel_9.setBounds(8, 148, 67, 15);
		panel_1.add(lblNewLabel_9);
		
		panel_2 = new JPanel();
		panel_2.setBounds(22, 353, 223, 144);
		border = BorderFactory.createTitledBorder("Register(for XE)");
		panel_2.setBorder(border);
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		
		JLabel lblNewLabel_12 = new JLabel("Hex");
		lblNewLabel_12.setBounds(161, 20, 50, 15);
		panel_2.add(lblNewLabel_12);
		
		JLabel lblNewLabel_13 = new JLabel("Dec");
		lblNewLabel_13.setBounds(90, 20, 50, 15);
		panel_2.add(lblNewLabel_13);
		
		JLabel lblNewLabel_14 = new JLabel("B(#3)");
		lblNewLabel_14.setBounds(12, 38, 67, 15);
		panel_2.add(lblNewLabel_14);
		
		regBDecTF = new JTextField();
		regBDecTF.setColumns(10);
		regBDecTF.setBounds(74, 35, 67, 21);
		panel_2.add(regBDecTF);
		regBDecTF.setEditable(false);
		
		regBHexTF = new JTextField();
		regBHexTF.setColumns(10);
		regBHexTF.setBounds(150, 35, 67, 21);
		panel_2.add(regBHexTF);
		regBHexTF.setEditable(false);
		
		regSHexTF = new JTextField();
		regSHexTF.setColumns(10);
		regSHexTF.setBounds(150, 60, 67, 21);
		panel_2.add(regSHexTF);
		regSHexTF.setEditable(false);
		
		regSDecTF = new JTextField();
		regSDecTF.setColumns(10);
		regSDecTF.setBounds(74, 60, 67, 21);
		panel_2.add(regSDecTF);
		regSDecTF.setEditable(false);
		
		JLabel lblNewLabel_15 = new JLabel("S(#4)");
		lblNewLabel_15.setBounds(12, 63, 67, 15);
		panel_2.add(lblNewLabel_15);
		
		JLabel lblNewLabel_16 = new JLabel("T(#5)");
		lblNewLabel_16.setBounds(12, 88, 67, 15);
		panel_2.add(lblNewLabel_16);
		
		regTDecTF = new JTextField();
		regTDecTF.setColumns(10);
		regTDecTF.setBounds(74, 85, 67, 21);
		panel_2.add(regTDecTF);
		regTDecTF.setEditable(false);
		
		regTHexTF = new JTextField();
		regTHexTF.setColumns(10);
		regTHexTF.setBounds(150, 85, 67, 21);
		panel_2.add(regTHexTF);
		regTHexTF.setEditable(false);
		
		regFTF = new JTextField();
		regFTF.setColumns(10);
		regFTF.setBounds(74, 110, 143, 21);
		panel_2.add(regFTF);
		regFTF.setEditable(false);
		
		JLabel lblNewLabel_17 = new JLabel("F(#6)");
		lblNewLabel_17.setBounds(12, 113, 67, 15);
		panel_2.add(lblNewLabel_17);
		
		instList = new JList<>();
		instList.setBounds(259, 229, 100, 257);
		contentPane.add(instList);
		
		JScrollPane instScroll = new JScrollPane(instList);
		instScroll.setBounds(259, 229, 100, 257);
		contentPane.add(instScroll);
		
		logList = new JList<>();
		logList.setBounds(22, 517, 406, 166);
		contentPane.add(logList);
		
		instScroll = new JScrollPane(logList);
		instScroll.setBounds(22, 517, 406, 166);
		contentPane.add(instScroll);
		
		JLabel lblNewLabel_10 = new JLabel("Log(명령어 수행 관련):");
		lblNewLabel_10.setBounds(22, 496, 238, 15);
		contentPane.add(lblNewLabel_10);
		
		panel_3 = new JPanel();
		panel_3.setBounds(257, 39, 228, 69);
		border = BorderFactory.createTitledBorder("E(End Record)");
		panel_3.setBorder(border);
		contentPane.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblNewLabel_11 = new JLabel("<HTML>Address of First Instruction<br/> in Object Program<HTML/>");
		lblNewLabel_11.setBounds(12, 20, 162, 37);
		panel_3.add(lblNewLabel_11);
		
		firstAddressTF = new JTextField();
		firstAddressTF.setBounds(120, 38, 96, 21);
		panel_3.add(firstAddressTF);
		firstAddressTF.setColumns(10);
		firstAddressTF.setEditable(false);
		
		JLabel lblNewLabel_18 = new JLabel("Start Address in Memory");
		lblNewLabel_18.setBounds(257, 118, 171, 15);
		contentPane.add(lblNewLabel_18);
		
		startAddressMemTF = new JTextField();
		startAddressMemTF.setBounds(357, 134, 114, 21);
		contentPane.add(startAddressMemTF);
		startAddressMemTF.setColumns(10);
		startAddressMemTF.setEditable(false);
		
		JLabel lblStartAddressIn = new JLabel("Target Address:");
		lblStartAddressIn.setBounds(257, 165, 91, 15);
		contentPane.add(lblStartAddressIn);
		
		targetAddressTF = new JTextField();
		targetAddressTF.setColumns(10);
		targetAddressTF.setBounds(357, 162, 114, 21);
		contentPane.add(targetAddressTF);
		targetAddressTF.setEditable(false);
		
		JLabel lblInstruction = new JLabel("Instruction:");
		lblInstruction.setBounds(257, 199, 91, 15);
		contentPane.add(lblInstruction);
		
		JLabel label = new JLabel("사용중인 장치");
		label.setBounds(371, 260, 86, 15);
		contentPane.add(label);
		
		usingDeviceTF = new JTextField();
		usingDeviceTF.setBounds(380, 285, 91, 21);
		contentPane.add(usingDeviceTF);
		usingDeviceTF.setColumns(10);
		usingDeviceTF.setEditable(false);
		
		oneStepBTN = new JButton("실행(1 Step)");
		oneStepBTN.setBounds(371, 353, 114, 23);
		contentPane.add(oneStepBTN);
		oneStepBTN.setEnabled(false);
		
		allStepBTN = new JButton("실행(All)");
		allStepBTN.setBounds(371, 388, 114, 23);
		contentPane.add(allStepBTN);
		allStepBTN.setEnabled(false);
		
		exitBTN = new JButton("종료");
		exitBTN.setBounds(371, 425, 114, 23);
		contentPane.add(exitBTN);
		this.setVisible(true);
	}
	
	// Event 발생 시 실행하는 메소드
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==openBTN) {
			// 파일 대화상자 열어 파일 경로 얻어오기
			FileDialog  fileDialog = new FileDialog(this, "파일선택", FileDialog.LOAD);
			fileDialog.setVisible(true);
			try {
				String dirPath = fileDialog.getDirectory();
				if(dirPath != null) {
					String fileName = fileDialog.getFile();
					String path = dirPath+fileName;
					File file = new File(path);
					fileNmaeTF.setText(fileName);
					
					// load 시작
					load(file);
					
					init_update();
				}
			}
			catch(Exception err) {
				err.printStackTrace();
			}
		}
		else if(e.getSource()==oneStepBTN) {
			oneStep();
			update();
		}
		else if(e.getSource()==allStepBTN) {
			allStep();
			update();
		}
		else if(e.getSource()==exitBTN) {
			this.dispose();
		}
	}
	
	// Component Event활성화
	private void listen() {
		openBTN.addActionListener(this);
		oneStepBTN.addActionListener(this);
		allStepBTN.addActionListener(this);
		exitBTN.addActionListener(this);
	}

	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		resourceManager.end = false;
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		sicSimulator.allStep();
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		regADecTF.setText(String.format("%d", resourceManager.getRegister(0)));
		regAHexTF.setText(String.format("%X", resourceManager.getRegister(0)));
		regXDecTF.setText(String.format("%d", resourceManager.getRegister(1)));
		regXHexTF.setText(String.format("%X", resourceManager.getRegister(1)));
		regLHexTF.setText(String.format("%X", resourceManager.getRegister(2)));
		regLDecTF.setText(String.format("%d", resourceManager.getRegister(2)));
		regPCDecTF.setText(String.format("%d", resourceManager.getRegister(8)));
		regPCHexTF.setText(String.format("%X", resourceManager.getRegister(8)));
		regSWTF.setText(String.format("%d", resourceManager.getRegister(9)));
		regBDecTF.setText(String.format("%d", resourceManager.getRegister(3)));
		regBHexTF.setText(String.format("%X", resourceManager.getRegister(3)));
		regSHexTF.setText(String.format("%X", resourceManager.getRegister(4)));
		regSDecTF.setText(String.format("%d", resourceManager.getRegister(4)));
		regTDecTF.setText(String.format("%d", resourceManager.getRegister(5)));
		regTHexTF.setText(String.format("%X", resourceManager.getRegister(5)));
		regFTF.setText(String.format("%f", resourceManager.register_F));
		targetAddressTF.setText(String.format("%X", resourceManager.targetAddress));;
		instList.setListData(sicSimulator.instList);;
		usingDeviceTF.setText(resourceManager.usingDevice);;
		logList.setListData(sicSimulator.logList);
		
		if(resourceManager.end) {
			allStepBTN.setEnabled(false);
			oneStepBTN.setEnabled(false);
		}
	};
	
	public void init_update() {
		regADecTF.setText(String.format("%d", resourceManager.getRegister(0)));
		regAHexTF.setText(String.format("%X", resourceManager.getRegister(0)));
		regXDecTF.setText(String.format("%d", resourceManager.getRegister(1)));
		regXHexTF.setText(String.format("%X", resourceManager.getRegister(1)));
		regLHexTF.setText(String.format("%X", resourceManager.getRegister(2)));
		regLDecTF.setText(String.format("%d", resourceManager.getRegister(2)));
		regPCDecTF.setText(String.format("%d", resourceManager.getRegister(8)));
		regPCHexTF.setText(String.format("%X", resourceManager.getRegister(8)));
		regSWTF.setText(String.format("%d", resourceManager.getRegister(9)));
		regBDecTF.setText(String.format("%d", resourceManager.getRegister(3)));
		regBHexTF.setText(String.format("%X", resourceManager.getRegister(3)));
		regSHexTF.setText(String.format("%X", resourceManager.getRegister(4)));
		regSDecTF.setText(String.format("%d", resourceManager.getRegister(4)));
		regTDecTF.setText(String.format("%d", resourceManager.getRegister(5)));
		regTHexTF.setText(String.format("%X", resourceManager.getRegister(5)));
		regFTF.setText(String.format("%f", resourceManager.register_F));
		programNameTF.setText(sicLoader.mainName);;
		startProgramTF.setText(String.format("%06X", sicLoader.startAddr));
		firstAddressTF.setText(sicLoader.startObjAddr);
		lengthProgramTF.setText(String.format("%X", sicLoader.programLen));
		startAddressMemTF.setText(String.format("%X",resourceManager.memIdx));
		
		// 버튼 활성화
		oneStepBTN.setEnabled(true);
		allStepBTN.setEnabled(true);
	}

	public static void main(String[] args) {
		VisualSimulator v = new VisualSimulator();
	}
}



