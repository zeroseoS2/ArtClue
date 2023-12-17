package ArtClue_Client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


public class ArtClue extends JFrame{
	public Image screenImage;
	public Graphics screenGraphic;
	public ArtClue thisclass=this;
    private Color newColor = Color.BLACK; // 색상변경을 위함
	private int mouseX, mouseY;//창 이동
    private Color currentColor;

    public Color getCurrentColor() {
        return currentColor;
    }
	//drawing board member
	public Image iDrawing = null;
	public Graphics gDrawing = null;
	public int x,y;

	public Image BackGroundImage = new ImageIcon(Main.class.getResource("/Image/background.png")).getImage();
	public JButton exit = new JButton(new ImageIcon(Main.class.getResource("/Image/exit.png")));
	public JLabel title = new JLabel(new ImageIcon(Main.class.getResource("/Image/title.png")));

	//login and connection
	public RoundJTextField name = new RoundJTextField(1);
	public JButton loginButton = new JButton(new ImageIcon(Main.class.getResource("/Image/loginbtn.png")));
	public JTextField ServerIP = new JTextField("127.0.0.1");

	//Loby group
	public ImageIcon profileImage;
	public JLabel lblProfile;
	private int profileNum = 0;

	public JLabel myInfo = new JLabel();
	public JLabel lobyInfototal = new JLabel();
	public JLabel lobyInfo = new JLabel();
	public JTextField chatInput = new JTextField();
	public JTextArea chatArea = new JTextArea();
	public JButton Room[] = new JButton[4];
	public JScrollPane chatSP = new JScrollPane(chatArea,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	public JButton colorButton = new JButton(new ImageIcon(Main.class.getResource("/Image/ColorChange.png")));
	//Communication
	public Socket socket=null;
	public PrintWriter pw=null;
	public BufferedReader br = null;

	//game member group
	public JPanel DrawArea = new JPanel();
	public JLabel Roomnum = new JLabel();
	public JLabel Roompeople = new JLabel();
	public JButton Erase = new JButton(new ImageIcon(Main.class.getResource("/Image/Eraser.png")));
	public JButton GoBack = new JButton(new ImageIcon(Main.class.getResource("/Image/backbtn.png")));
	public String nickname=null;
	public JLabel nicknamelabel = new JLabel();
	public int WhereIAm=0;
	public JButton Start = new JButton(new ImageIcon(Main.class.getResource("/Image/startbtn.png")));

	int i;

    public void startApplication() {
        Music introMusic = new Music("src/Music/CatchMind.wav", true);
        introMusic.start();
    }

	public ArtClue() {
		this.setUndecorated(true);
		this.setTitle("Catch Mind");
		this.setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(null);

	    currentColor = Color.BLACK;

		//창 이동
		addMouseListener(new MouseAdapter() {
            @Override
			public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
			public void mouseDragged(MouseEvent e) {
                int newX = e.getXOnScreen() - mouseX;
                int newY = e.getYOnScreen() - mouseY;

                setLocation(newX, newY);
            }
        });
		// 그림판 초기화
		iDrawing = createImage(540, 420);
		gDrawing = iDrawing.getGraphics();
		gDrawing.setColor(Color.WHITE);
		gDrawing.fillRect(0, 0, 540, 420);
		repaint();


        // 색상 변경 버튼 추가
		colorButton.setVisible(false);
		colorButton.setBounds(73, 635, 375, 50);
		colorButton.setBorderPainted(false);
		colorButton.setContentAreaFilled(false);
		colorButton.setFocusPainted(false);
		colorButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent arg0) {
                // 색상 선택 다이얼로그 띄우기
                Color newColor = JColorChooser.showDialog(thisclass, "Choose Color", Color.BLACK);

                // 색상이 선택되었을 때만 색상 변경
                if (newColor != null) {
                    setColor(newColor);
                    sendColorToOtherClients(newColor);
                }
            }
        });
        getContentPane().add(colorButton);

		//제목
		title.setVisible(true);
		title.setBounds(520, 120, 200, 200);
		getContentPane().add(title);

		name.setVisible(true);
		name.setText("");
		name.setBounds(533,500,100,40);
		name.setOpaque(false);
		name.setFont(new Font("맑은 고딕",Font.BOLD,20));
		name.setForeground(Color.BLACK);
		name.requestFocus();
		getContentPane().add(name);


		ServerIP.setVisible(true);
		ServerIP.setBounds(8,0,80,30);
		ServerIP.setBorder(null);
		ServerIP.setBackground(new Color(0,0,0,0));
		getContentPane().add(ServerIP);

		loginButton.setVisible(true);
		loginButton.setBounds(637, 488, 70, 60);
		loginButton.setBorderPainted(false);
		loginButton.setContentAreaFilled(false);
		loginButton.setFocusPainted(false);
		loginButton.addActionListener(new ActionListener() {
			//Process after clicked LoginButton
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String S_IP = ServerIP.getText();
				try {
					//server connect try
					if(socket == null||socket.toString().equals("Socket[unconnected]")) {
						socket = new Socket();
						socket.connect(new InetSocketAddress(S_IP, Main.Port));
						pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
						br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
					}
		            System.out.println(socket);

		            //this is login process
		            //send my nickname and join to loby chat room
		            pw.println("login");
		            String request = "join:" + "0" +"\r\n";
		            System.out.println(name.getText());
		            pw.println(name.getText());
		            pw.println(request);
		            nickname=name.getText()+"쿵야";
		            myInfo.setText(nickname);
		            nicknamelabel.setText(nickname);
					JOptionPane.showMessageDialog(null, "대기실로 이동합니다", "서버 접속 완료", JOptionPane.INFORMATION_MESSAGE);

					goLoby();

					new ArtClueReceiver(socket,thisclass).start();
				}
				catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "서버로 부터 반응이 없습니다.\n다시 시도해주세요.", "메롱", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		getContentPane().add(loginButton);


		//////////// LobyButton From Here
		////////////
		////////////
		profileImage=getProfileImage(profileNum);
		lblProfile =new JLabel(profileImage);
		lblProfile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ProfileFrame profileFrame = new ProfileFrame(thisclass);
				profileFrame.setVisible(true);
				System.out.println(profileImage);
			}
		});
		lblProfile.setBounds(857, 14, 62, 72);
		lblProfile.setVisible(false);
		getContentPane().add(lblProfile);

		lobyInfo.setVisible(false);
		lobyInfo.setText("?");
		lobyInfo.setBounds(440,18,26,26);
		lobyInfo.setFont(new Font("맑은 고딕",Font.BOLD,20));
		lobyInfo.setForeground(Color.BLACK);
		getContentPane().add(lobyInfo);

		lobyInfototal.setVisible(false);
		lobyInfototal.setText("?");
		lobyInfototal.setBounds(440,54,26,26);
		lobyInfototal.setFont(new Font("맑은 고딕",Font.BOLD,20));
		lobyInfototal.setForeground(Color.BLACK);
		getContentPane().add(lobyInfototal);

		chatInput.setVisible(false);
		chatInput.setBounds(807,672,400,30);
		chatInput.setOpaque(false);
		chatInput.setFont(new Font("맑은 고딕",Font.BOLD,15));
		chatInput.setForeground(Color.WHITE);
		chatInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				char keyCode = e.getKeyChar();
                if (keyCode == KeyEvent.VK_ENTER) {
                	if(chatInput.getText().length()>50) {
                		chatInput.setText(chatInput.getText().substring(0, 50));
                	}
                    sendMessage();
                }
			}
		});
		getContentPane().add(chatInput);


		chatArea.setBounds(807,168,400,506);
		chatArea.setOpaque(false);
		chatArea.setFont(new Font("맑은 고딕",Font.BOLD,15));
		chatArea.setForeground(Color.WHITE);
		chatArea.setText("");
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		chatSP.setOpaque(false);
		chatSP.getViewport().setOpaque(false);
		chatSP.setVisible(false);
		chatSP.setBounds(807,168,400,506);
		chatSP.setBorder(null);
		chatSP.getVerticalScrollBar().setValue(chatSP.getVerticalScrollBar().getMaximum());
		getContentPane().add(chatSP);

		for(i=0;i<4;i++) {
			Room[i] = new JButton(new ImageIcon(Main.class.getResource("/Image/room"+Integer.toString(i+1)+".png")));
			Room[i].setVisible(false);
			Room[i].setBorderPainted(false);
			Room[i].setContentAreaFilled(false);
			Room[i].setFocusPainted(false);
			Room[i].addActionListener(new ActionListener() {
				int temp = i+1;
				@Override
				public void actionPerformed(ActionEvent arg0) {
					pw.println("quit:" + WhereIAm);
					pw.println("join:" + Integer.toString(temp) + ":" + nickname);
					WhereIAm = temp;
					Roomnum.setText(Integer.toString(WhereIAm));
					goGame();

					//drawing board show
					iDrawing=createImage(540,420);
					gDrawing=iDrawing.getGraphics();
					gDrawing.setColor(Color.WHITE);
					gDrawing.fillRect(0, 0, 540, 420);
					repaint();

				}
			});
			getContentPane().add(Room[i]);
		}
		Room[0].setBounds(130,240,246,148);
		Room[1].setBounds(440,240,246,148);
		Room[2].setBounds(130,423,246,148);
		Room[3].setBounds(440,423,246,148);

		myInfo.setBounds(942,36,170,25);
		myInfo.setVisible(false);
		myInfo.setFont(new Font("맑은 고딕",Font.BOLD,25));
		myInfo.setForeground(Color.BLACK);
		getContentPane().add(myInfo);

		//gameroom
		/////////////////////////
		//////////////////////////
		nicknamelabel.setBounds(320,36,170,25);
		nicknamelabel.setVisible(false);
		nicknamelabel.setFont(new Font("맑은 고딕",Font.BOLD,25));
		nicknamelabel.setForeground(Color.BLACK);
		getContentPane().add(nicknamelabel);
		
		Roomnum.setVisible(false);
		Roomnum.setBounds(690,18,26,26);
		Roomnum.setFont(new Font("맑은 고딕",Font.BOLD,20));
		Roomnum.setForeground(Color.BLACK);
		getContentPane().add(Roomnum);

		Roompeople.setVisible(false);
		Roompeople.setBounds(690,54,26,26);
		Roompeople.setFont(new Font("맑은 고딕",Font.BOLD,20));
		Roompeople.setForeground(Color.BLACK);
		Roompeople.setText("?");
		getContentPane().add(Roompeople);

		DrawArea.setVisible(false);
		DrawArea.setOpaque(false);
		DrawArea.setBounds(82,205,540,420);
		DrawArea.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent me) {
				x = me.getX();
				y = me.getY();
			}
			@Override
			public void mouseDragged(MouseEvent me) {
				if (me.getModifiersEx() != InputEvent.BUTTON1_DOWN_MASK)
					return;
				String msg = "draw:"+x+":"+y+":"+me.getX()+":"+me.getY()+":"+WhereIAm;
				pw.println(msg);

				x = me.getX();
				y = me.getY();
			}
		});
		getContentPane().add(DrawArea);

		GoBack.setVisible(false);
		GoBack.setBounds(1050, 33, 157, 62);
		GoBack.setBorderPainted(false);
		GoBack.setContentAreaFilled(false);
		GoBack.setFocusPainted(false);
		GoBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pw.println("quit:"+WhereIAm);
				pw.println("join:0");
				WhereIAm=0;
				goLoby();
			}
		});
		getContentPane().add(GoBack);

		Start.setBounds(858,33, 157, 62);
		Start.setVisible(false);
		Start.setBorderPainted(false);
		Start.setContentAreaFilled(false);
		Start.setFocusPainted(false);
		Start.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
		        System.out.println("Start button clicked!");

		        // 기존의 메시지 전송 로직
		        pw.println("rungame:" + WhereIAm);

		        chatInput.setText("");
		    }
		});
		getContentPane().add(Start);


		Erase.setVisible(false);
		Erase.setBounds(545,635, 88,50);
		Erase.setBorderPainted(false);
		Erase.setContentAreaFilled(false);
		Erase.setFocusPainted(false);
		Erase.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pw.println("draw:erase:" + WhereIAm);
				pw.flush();
			}
		});
		getContentPane().add(Erase);


		//exit button
		exit.setVisible(true);
		exit.setBounds(1200, 20, 64, 64);
		exit.setBorderPainted(false);
		exit.setContentAreaFilled(false);
		exit.setFocusPainted(false);
		exit.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				try {
					pw.println("quit:"+WhereIAm);
				}
				catch(Exception e1) {

				}
				System.exit(0);
			}
		});
		getContentPane().add(exit);
	}

	public void goLoby(){
		//main member invisible
		lblProfile.setVisible(true);
		lblProfile.setBounds(857, 14, 62, 72);
		title.setVisible(false);
		name.setVisible(false);
		ServerIP.setVisible(false);
		loginButton.setVisible(false);
		colorButton.setVisible(false);
		exit.setVisible(true);
		//loby member visible
		lobyInfo.setVisible(true);
		lobyInfototal.setVisible(true);
		chatInput.setVisible(true);
		chatSP.setVisible(true);
		myInfo.setVisible(true);
		for(int i=0;i<4;i++)
			Room[i].setVisible(true);

		//game member invisible
		Roomnum.setVisible(false);
		Roompeople.setVisible(false);
		GoBack.setVisible(false);
		Erase.setVisible(false);
		DrawArea.setVisible(false);
		Start.setVisible(false);
		nicknamelabel.setVisible(false);
		//change backGround
		BackGroundImage = new ImageIcon(Main.class.getResource("/Image/mainpage.png")).getImage();

		//채팅방 초기화
	    chatArea.setText("");
	}

	public void goGame() {
		//main member invisible
		title.setVisible(false);
		name.setVisible(false);
		ServerIP.setVisible(false);
		loginButton.setVisible(false);
		exit.setVisible(false);
		lblProfile.setVisible(true);
		lblProfile.setBounds(240, 14, 62, 72);
		//loby member invisible
		lobyInfo.setVisible(false);
		lobyInfototal.setVisible(false);
		chatInput.setVisible(true);
		chatSP.setVisible(true);
		myInfo.setVisible(false);
		for(int i=0;i<4;i++)
			Room[i].setVisible(false);

		//game member visible
		Roomnum.setVisible(true);
		Roompeople.setVisible(true);
		GoBack.setVisible(true);
		DrawArea.setVisible(true);
		Erase.setVisible(true);
		Start.setVisible(true);
		colorButton.setVisible(true);
		nicknamelabel.setVisible(true);
		//change backGround
		BackGroundImage = new ImageIcon(Main.class.getResource("/Image/gamepage.png")).getImage();

		//채팅방 초기화
	    chatArea.setText("");

	}

	public void sendMessage() {
	    try {
	        // 클라이언트가 "답"만 입력하는 경우를 처리
	        String answer = chatInput.getText().trim();
	        if (!answer.isEmpty()) {
	            pw.println("answer:" + answer);
	        } else {
	            System.out.println("답을 입력하세요.");
	            // 필요에 따라 사용자에게 메시지를 보여줄 수 있습니다.
	        }
	        pw.println(answer);
	        pw.flush();

	        chatInput.setText("");
	        chatInput.requestFocus();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	public void refreshInfo(String info[]) {
		int total = 0;
		for(int i=1;i<6;i++) {
			total+=Integer.parseInt(info[i]);
		}
		lobyInfo.setText(info[1]);
		lobyInfototal.setText(Integer.toString(total));

		Roompeople.setText(info[WhereIAm+1]);
	}

	@Override
	public void paint(Graphics g) {
		screenImage = createImage(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		screenGraphic = screenImage.getGraphics();
		screenDraw(screenGraphic);
		g.drawImage(screenImage, -4, 0, null);
	}
	public void screenDraw(Graphics g) {
		g.drawImage(BackGroundImage, 0, 0, null);
		if(WhereIAm!=0) {
			g.drawImage(iDrawing,82,205,null);
		}
		paintComponents(g);
		this.repaint();
	}

	// ArtClue 클래스 내부에 추가
	public void setColor(Color color) {
	    this.currentColor = color;
	    gDrawing.setColor(color);
	    repaint();
	}

	// 메소드 추가: 다른 클라이언트에게 색상 전송
	public void sendColorToOtherClients(Color color) {
	    pw.println("changeColor:" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ":" + WhereIAm);
	}

	public synchronized void updateColor(Color newColor) {
	    currentColor = newColor;
	    gDrawing.setColor(currentColor);
	    repaint();
	}
	public void changeProfileImage(int index) {
		profileNum = index;
		profileImage = getProfileImage(profileNum);
		lblProfile.setIcon(profileImage);
	}
	public int getProfileNum() {
		return profileNum;
	}
	private ImageIcon getProfileImage(int profileNum) {
		 return new ImageIcon(new ImageIcon(ProfileFrame.class.getResource(ProfileFrame.PROFILEPATH + "/profile" + profileNum + ".png")).getImage().getScaledInstance(62, 72, java.awt.Image.SCALE_SMOOTH));
	}
}