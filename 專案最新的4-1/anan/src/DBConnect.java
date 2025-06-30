import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DBConnect {
    // MySQL 連線資訊
    private static final String URL = "jdbc:mysql://localhost:3306/3/26";
    private static final String USER = "root";
    private static final String PASSWORD = "Hhipp5411";

    static {
        try {
            // 載入 MySQL 驅動
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("⚠️ 無法載入 MySQL 驅動程式", e);
        }
    }

    /**
     * 執行 SELECT 查詢
     * 
     * @param query  SQL 查詢語句
     * @param params 可變參數（用於 PreparedStatement）
     * @return ResultSet 查詢結果
     */
    public static ResultSet selectQuery(String query, Object... params) {
        try {
            //連線
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            //下查詢語法
            PreparedStatement stmt = conn.prepareStatement(query);
            //執行查詢並回傳
            return stmt.executeQuery(); // ⚠️ 注意：ResultSet 需要手動關閉
        } catch (SQLException e) {
            throw new RuntimeException("⚠️ 查詢失敗：" + e.getMessage(), e);
        }
    }

    /**
     * 執行 INSERT / UPDATE / DELETE 操作
     * 
     * @param query  SQL 語句
     * @param params 可變參數（用於 PreparedStatement）
     * @return 受影響的行數
     */
    public static int executeUpdate(String query, Object... params) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            // 設定參數
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("⚠️ 資料更新失敗：" + e.getMessage(), e);
        }
    }
      
    public static int getNextId(String tableName, String columnName) {
        try (ResultSet rs = DBConnect.selectQuery("SELECT MAX(" + columnName + ") FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    public static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim().replaceAll("[{}\"]", "");
        for (String pair : json.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2)
                map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }
    public static void main(String[] args) {
        // 測試 SELECT 查詢
        try (ResultSet rs = selectQuery("SELECT * FROM `3/26`.clothing")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getString("idClothing") + ", Name: " + rs.getString("Name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 測試 INSERT
        int insertResult = executeUpdate("INSERT INTO clothing (Name, style, price, Size, Color, isPreorder) VALUES (?, ?, ?, ?, ?, ?)", 
     "襪子", "日系", 199.99, "M", "黑", "現貨"); // "現貨" 作為字符串
System.out.println("🔹 插入成功，影響行數：" + insertResult);

        

        // 測試 UPDATE
        int updateResult = executeUpdate("UPDATE clothing SET Name = ?, style = ?, price = ?, Size = ?, Color= ?, isPreorder = ? WHERE idClothing = ?", 
        "襪子", "日系", 199.99, "M", "黑", "現貨", String.valueOf(14)); // 使用 idClothing 作為條件
    System.out.println("🔹 更新成功，影響行數：" + updateResult);

        // 測試 DELETE
        int deleteResult = executeUpdate("DELETE FROM clothing WHERE idClothing = ?", "102");
        System.out.println("🔹 刪除成功，影響行數：" + deleteResult);
    }
}

