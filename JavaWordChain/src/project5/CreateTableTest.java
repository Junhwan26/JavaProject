package project5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
 
 
//java�ڵ�� min��� �����ؼ�
//student ���̺��� ����
//create table student(
//    id int,
//    name varchar(20),
//    grade int
//);
//----- ��� ���̺귯�� ���.
//Class �ε�
//DBMS�� Ư�� DB�� ������ ���� Connection��ü ȹ��.
//���� ���� �������� ǰ�� Statement ��ü ����.
//Connection ��ü�� Statement������ü�� ����.
 
 
public class CreateTableTest {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        //������ DBMS�� �ּ� (�������);
        String dataBase = //����
        String url = "";
        //������ mysql id�� password �Է�.
        String id = "";//�⺻������ ���̵�� root
        String pw = ""; //���
        
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
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
        	SqlTest test = new SqlTest(conn,"wordtable");
        	test.select("���콺");
            try {
                //����� connection �ݱ�
                if(conn != null && !conn.isClosed())
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        

    }
 
}
