package project5;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
 
public class SqlTest {
    Connection conn = null;
    Statement stmt = null;
    String table;
 
    public SqlTest(Connection conn, String table) {
        this.conn = conn;
        this.table = table;
        try {
            this.stmt = conn.createStatement();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    // 삽입
    public void insert(int idx, String word, int len) {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("insert into " + table + " values(")
                .append(idx + ",")
                .append("'" + word + "',")
                .append(len)
                .append(");")
                .toString();
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    // 삭제
    public void delete(int idx) {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("delete from " + table + " where id = ")
                .append(idx)
                .append(";")
                .toString();
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    // 수정
    public void update(int idx, String word, int len) {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("update " + table + " set")
                .append(" word = ")
                .append("'" + word + "',")
                .append(" grade = ")
                .append(len)
                .append(" where id = ")
                .append(idx)
                .append(";").toString();
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    // 모든 검색
    public void selectAll() {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("select * from " + table)
                .append(";").toString();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.print("idx");
            System.out.print("\t");
            System.out.print("word");
            System.out.print("\t");
            System.out.print("len");
            System.out.print("\n");
            System.out.println("────────────────────────");
            
              while(rs.next()){
                     System.out.print(rs.getInt("idx"));
                     System.out.print("\t");
                     System.out.print(rs.getString("word"));
                     System.out.print("\t");
                     System.out.print(rs.getString("len"));
                     System.out.print("\n");
                }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    // 검색
    public void select(int idx) {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("select * from " + table + " where")
                .append(" idx = ")
                .append(idx)
                .append(";").toString();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.print("idx");
            System.out.print("\t");
            System.out.print("word");
            System.out.print("\t");
            System.out.print("len");
            System.out.print("\n");
            System.out.println("────────────────────────");
            
              while(rs.next()){
                     System.out.print(rs.getInt("idx"));
                     System.out.print("\t");
                     System.out.print(rs.getString("word"));
                     System.out.print("\t");
                     System.out.print(rs.getString("len"));
                     System.out.print("\n");
                }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void select(String word) {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("select * from " + table + " where")
                .append(" word = \"")
                .append(word)
                .append("\";").toString();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.print("idx");
            System.out.print("\t");
            System.out.print("word");
            System.out.print("\t");
            System.out.print("len");
            System.out.print("\n");
            System.out.println("────────────────────────");
            
              while(rs.next()){
                     System.out.print(rs.getInt("idx"));
                     System.out.print("\t");
                     System.out.print(rs.getString("word"));
                     System.out.print("\t");
                     System.out.print(rs.getString("len"));
                     System.out.print("\n");
                }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public boolean isselect(String word) {
        StringBuilder sb = new StringBuilder();
        String sql = sb.append("select * from " + table + " where")
                .append(" word = \"")
                .append(word)
                .append("\";").toString();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.last();
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
    }
}


