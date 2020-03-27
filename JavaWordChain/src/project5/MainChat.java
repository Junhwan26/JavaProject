package project5;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class MainChat extends JFrame implements ActionListener, Runnable {

	JList<String> roomInfo, roomInwon, waitInfo;
	JScrollPane sp_roomInfo, sp_roomInwon, sp_waitInfo;
	JButton bt_create, bt_enter, bt_exit;
	JPanel p;
	ChatClient cc;
	String nickName;
	// ���� ����°�ü
	BufferedReader in;
	OutputStream out;
	String selectedRoom;
	int turn = 0;
	boolean duplicated;
	Vector<String> used = new Vector<String>();
	Timer timer;
	int time = 10;
	public MainChat() {
		setTitle("����");
		cc = new ChatClient();
		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("������"));
		roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				String str = roomInfo.getSelectedValue(); // "�ڹٹ�--1"
				if (str == null)
					return;
				System.out.println("������=" + str);
				selectedRoom = str.substring(0, str.indexOf("-"));
				// "�ڹٹ�" <---- substring(0,3)
				// ��ȭ�� ���� �ο�����
				sendMsg("170|" + selectedRoom);
			}

		});

		roomInwon = new JList<String>();
		roomInwon.setBorder(new TitledBorder("�ο�����"));
		waitInfo = new JList<String>();
		waitInfo.setBorder(new TitledBorder("��������"));

		sp_roomInfo = new JScrollPane(roomInfo);
		sp_roomInwon = new JScrollPane(roomInwon);
		sp_waitInfo = new JScrollPane(waitInfo);

		bt_create = new JButton("�游���");
		bt_enter = new JButton("�����");
		bt_exit = new JButton("������");

		p = new JPanel();

		sp_roomInfo.setBounds(10, 10, 300, 300);
		sp_roomInwon.setBounds(320, 10, 150, 300);
		sp_waitInfo.setBounds(10, 320, 300, 130);

		bt_create.setBounds(320, 330, 150, 30);
		bt_enter.setBounds(320, 370, 150, 30);
		bt_exit.setBounds(320, 410, 150, 30);

		p.setLayout(null);
		p.setBackground(Color.orange);
		p.add(sp_roomInfo);
		p.add(sp_roomInwon);
		p.add(sp_waitInfo);
		p.add(bt_create);
		p.add(bt_enter);
		p.add(bt_exit);

		add(p);
		setBounds(300, 200, 500, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		connect();// ��������õ� (in,out��ü����)
		new Thread(this).start();// �����޽��� ���
		sendMsg("100|");// (����)���� �˸�
		nickName = JOptionPane.showInputDialog(this, "��ȭ��:");
		sendMsg("150|" + nickName);// ��ȭ�� ����

		eventUp();

	}// ������

	private void eventUp() {// �̺�Ʈ�ҽ�-�̺�Ʈó���� ����

		// ����(MainChat)

		bt_create.addActionListener(this);
		bt_enter.addActionListener(this);
		bt_exit.addActionListener(this);

		// ��ȭ��(ChatClient)
		cc.sendTF.addActionListener(this);
		cc.bt_ready.addActionListener(this);
		cc.bt_start.addActionListener(this);
		cc.bt_exit.addActionListener(this);

	}

	@Override

	public void actionPerformed(ActionEvent e) {

		Object ob = e.getSource();

		if (ob == bt_create) {// �游��� ��û

			String title = JOptionPane.showInputDialog(this, "������:");
			// �������� �������� ����
			sendMsg("160|" + title);
			cc.setTitle("ä�ù�-[" + title + "]");
			sendMsg("175|");// ��ȭ�泻 �ο����� ��û

			setVisible(false);
			cc.setVisible(true); // ��ȭ���̵�
		} else if (ob == bt_enter) {// ����� ��û

			if (selectedRoom == null) {
				JOptionPane.showMessageDialog(this, "���� ����!!");
				return;
			}
			sendMsg("200|" + selectedRoom);

			sendMsg("175|");// ��ȭ�泻 �ο����� ��û

			setVisible(false);

			cc.setVisible(true);

		} else if (ob == cc.bt_exit) {// ��ȭ�� ������ ��û

			sendMsg("400|");
			cc.setVisible(false);
			cc.ta.selectAll();
		    cc.ta.replaceSelection("");
			setVisible(true);
			used.clear(); //�ߺ��ܾ� ����Ʈ �ʱ�ȭ

		} else if (ob == cc.sendTF) {// (TextField�Է�)�޽��� ������ ��û

			String msg = cc.sendTF.getText();

			if (turn == 1 && msg.length() > 0) {
				if(wordCheck(msg)==false) {
					cc.ta.append("�ùٸ��� ���� �ܾ��Դϴ�.\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());	
				}
				else {
					if (msg.length() > 0) {
						sendMsg("301|" + msg); //����
						cc.sendTF.setText("");
					}
					sendMsg("score|" + msg.length()*10);
					cc.sendTF.setText("");
					timer.cancel();
				}
			}
			else {
				if (msg.length() > 0) { 
					sendMsg("300|" + msg);
					cc.sendTF.setText("");
				}
			}
		} else if (ob == cc.bt_ready) {
			sendMsg("ready|");
		}

		else if (ob == cc.bt_start) {
			sendMsg("start|");
		}

		else if (ob == bt_exit) {// ������(���α׷�����) ��û

			System.exit(0);// ���� �������α׷� �����ϱ�

		}

	}// actionPerformed

	public void connect() {// (����)�������� ��û

		try {

			// Socket s = new Socket(String host<����ip>, int port<���񽺹�ȣ>);

			Socket s = new Socket("localhost", 5000);// ����õ�

			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			// in: �����޽��� �бⰴü ����-----msg------>Ŭ���̾�Ʈ

			out = s.getOutputStream();

			// out: �޽��� ������, ���ⰴü Ŭ���̾�Ʈ-----msg----->����

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}// connect
	
	
	public boolean wordCheck(String s) {
		if (used.contains(s)) {
			return false;
		}
		if(!used.isEmpty() && used.lastElement().charAt(used.lastElement().length()-1) != s.charAt(0)) {
	         return false;
	      }
		Connection conn = null;
        Statement stmt = null;
        //������ DBMS�� �ּ� (�������);
        String dataBase = "s18057"; //����
        String url = "jdbc:mysql://dev.gsa.hs.kr:5060/" + dataBase + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        //������ mysql id�� password �Է�.
        String id = "s18057"; //�⺻������ ���̵�� root
        String pw = "1111"; //���
        
        try {
            //Ŭ������ �ε� ��.. �ֽŹ��� �ڹٿ����� ���� ����
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            //DB�� ������ ���� Connection��ü ȹ��
            conn = DriverManager.getConnection(url,id,pw);  
            //DB�� ����� conn��ü�κ��� ������ü�� ȹ��
            stmt = conn.createStatement();
            
            //���� �����
            StringBuilder sb = new StringBuilder();
            String sql = sb.append("create table if not exists wordtable(")    //if not exists = �������� ���� ���� ����
                    .append("idx int,")
                    .append("word varchar(50),")
                    .append("len int")
                    .append(");").toString(); 
 
            //������ ������
            stmt.execute(sql);
            
            // ������ü�� ������ 3���� ���
            //1. execute -> ���̺� ����, ����,���� �� �����ͺ��̽� ���� ��ɾ� ���
            //2. excuteUpdate -> ���ڵ� ���� ���� ������ ������ ���� ��ɾ� ���
            //3. excuteQuery -> ���ڵ� ��ȸ, ���̺� ��ȸ �� ��ȸ ��ɾ� ���
            StringBuilder sb2 = new StringBuilder();
            String sql2 = sb2.append("select * from wordtable where")
                    .append(" word = \"")
                    .append(s)
                    .append("\";").toString();
            try {
                ResultSet rs = stmt.executeQuery(sql2);
                rs.last();
                System.out.println(rs.getRow());
                if(rs.getRow()==0) {
                	return false;
                }
                else {
                	return true;
                }
                
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {

            try {
                //����� connection �ݱ�
                if(conn != null && !conn.isClosed())
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
	}
	public void sendMsg(String msg) {// �������� �޽��� ������

		try {
			out.write((msg + "\n").getBytes());

		} catch (IOException e) {

			e.printStackTrace();

		}

	}// sendMsg

	public void run() {// ������ ���� �޽��� �б�

		// �� run�޼ҵ� ���? GUI���α׷����࿡ ���� ��ġ���ʴ� �ڵ� �ۼ�.

//�޼ҵ�ȣ���� �������� ����!!  ������޼ҵ�� ���ý���(��ٸ��� �ʴ� ���� ����)!!

		try {

			while (true) {

				String msg = in.readLine();// msg: ������ ���� �޽���

				// msg==> "300|�ȳ��ϼ���" "160|�ڹٹ�--1,����Ŭ��--1,JDBC��--1"

				String msgs[] = msg.split("\\|");

				String protocol = msgs[0];

				switch (protocol) {

				case "300":

					cc.ta.append(msgs[1] + "\n");

					cc.ta.setCaretPosition(cc.ta.getText().length());

					break;

				case "160":// �游���

					// �������� List�� �Ѹ���

					if (msgs.length > 1) {

						// ������ ���� �Ѱ� �̻��̾����� ����

						// ������ ����� ----> msg="160|" ������ ����

						String roomNames[] = msgs[1].split(",");

						// "�ڹٹ�--1,����Ŭ��--1,JDBC��--1"

						roomInfo.setListData(roomNames);

					}

					break;

				case "170":// (���ǿ���) ��ȭ�� �ο�����
					String roomInwons[] = msgs[1].split(",");
					roomInwon.setListData(roomInwons);
					break;

				case "175":// (��ȭ�濡��) ��ȭ�� �ο�����

					String myRoomInwons[] = msgs[1].split(",");

					
					cc.li_inwon.setListData(myRoomInwons);

					break;

				case "180":// ���� �ο�����

					String waitNames[] = msgs[1].split(",");

					waitInfo.setListData(waitNames);

					break;

				case "200":// ��ȭ�� ����

					cc.ta.append("=========[" + msgs[1] + "]�� ����=========\n");

					cc.ta.setCaretPosition(cc.ta.getText().length());

					break;

				case "400":// ��ȭ�� ����

					cc.ta.append("=========[" + msgs[1] + "]�� ����=========\n");

					cc.ta.setCaretPosition(cc.ta.getText().length());

					break;

				case "202":// ������ ���� Ÿ��Ʋ ���� ���
					cc.setTitle("ä�ù�-[" + msgs[1] + "]");
					break;
				case "debug":
					System.out.println(msgs[1]);
					break;
				case "turn":
					turn = Integer.parseInt(msgs[1]);
					cc.bt_ready.setText("10");
					
					if (turn == 1) {
						cc.ta.append("----My turn----\n");
						cc.ta.setCaretPosition(cc.ta.getText().length());		
						cc.bt_start.setText("������!");
						time = 10;
						 timer = new Timer(true);
		                  Task task = new Task();
		                  timer.schedule(task, 1000, 1000);

						
						
						
						
						
					}
					else {
						cc.bt_start.setText("�����ʾƴ�");
					}
					break;
				case "usedword":
					used.add(msgs[1]);
					break;
				case "start":
					cc.bt_start.setText("�����ʾƴ�");
					cc.bt_ready.setText(" ");
					//cc.bt_ready.setEnabled(false);
					cc.bt_start.setEnabled(false);
					
					cc.bt_exit.setEnabled(false);
					
					break;
				case "btchange":
					System.out.println(msgs[1].getClass());
					if (msgs[1].equals("1")) {
						cc.bt_ready.setText("�غ�Ϸ�");
						cc.bt_ready.setBackground(Color.YELLOW);
						System.out.println("ready");
					} else {
						cc.bt_ready.setText("�غ�");
						cc.bt_ready.setBackground(Color.WHITE);
						System.out.println("cancel");
					}
					cc.setVisible(true);
					break;
				}// Ŭ���̾�Ʈ switch
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}// run

	public static void main(String[] args) {

		new MainChat();

	}
	class Task extends TimerTask{
	      public void run() {
	         time -= 1;
	         cc.bt_ready.setText(Integer.toString(time));
	         if (time == 0) {
	            cc.ta.append("�ð��ʰ�!!\n");
	            cc.bt_ready.setText("�ð��ʰ�!");
	            cc.ta.setCaretPosition(cc.ta.getText().length());   
	            sendMsg("score|0");
	            timer.cancel();
	         }
	      }
	   }

}

