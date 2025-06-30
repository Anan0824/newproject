import java.sql.*;
import java.util.Scanner;

public class ShopMenu {
    private static Scanner scanner = new Scanner(System.in, "BIG5");

    public static void main(String[] args) {
        while (true) {
            showMenu01();
            int choice = getChoice();
            handleChoice(choice);
        }
    }

    // 顯示商店選單
    private static void showMenu01() {
        System.out.println("\n====== 商店選單 ======");
        System.out.println("1. 新增會員");
        System.out.println("2. 查詢會員");
        System.out.println("3. 預購服飾");
        System.out.println("4. 顯示服飾現貨庫存列表");
        System.out.println("5. 新增員工");
        System.out.println("6. 查詢員工");
        System.out.println("7. 新增shipper");
        System.out.println("8. shipper列表");
        System.out.println("9. 新增訂單");
        System.out.println("10. 訂單列表");
        System.out.println("11. 離開系統");
        System.out.print("請選擇功能（輸入數字）：");
    }

    // 讀取用戶輸入
    private static int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ 輸入錯誤，請輸入數字！");
            return -1;
        }
    }

    // 根據選擇執行對應功能
    private static void handleChoice(int choice) {
        switch (choice) {
            case 1:
                addCustomer();
                break;
            case 2:
                listCustomers();
                break;
            case 3:
                addclothing();
                break;
            case 4:
                listClothing();
                break;
            case 5:
                addEmployees();
                break;
            case 6:
                listEmployees();
                break;
            case 7:
                addShipper();
                break;
            case 8:
                listShippers();
                break;
            case 9:
                addOrder();
                break;
            case 10:
                listOrders();
                break;
            case 11:
                System.out.println("? 感謝光臨，歡迎下次再來！");
                System.exit(0);
            default:
                System.out.println("?? 無效選擇，請重新輸入！");
        }
    }

    // 新增會員
    private static void addCustomer() {
    int id = getNextId("customers", "idCustomers");
    
    System.out.print("輸入會員名稱：");
    String name = scanner.nextLine();
    
    System.out.print("輸入會員地址：");
    String address = scanner.nextLine();
    
    System.out.print("輸入會員電話：");
    String phone = scanner.nextLine();
    
    System.out.print("輸入會員國家：");
    String country = scanner.nextLine();
    
    int result = DBConnect.executeUpdate(
            "INSERT INTO customers (idCustomers, CustomerName, Address, PhoneNumber, Country) VALUES (?, ?, ?, ?, ?)",
            id, name, address, phone, country);
    
    System.out.println(result > 0 ? "✅ 會員新增成功！" : "❌ 新增失敗！");
}

    // 查詢會員
    private static void listCustomers() {
        try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM customers")) {
            System.out.println("ID | 會員名稱");
            while (rs.next()) {
                System.out.println(rs.getInt("idCustomers") + " | " + rs.getString("CustomerName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 新增服飾 (預購或現貨)
    private static void addclothing() {
        int id = getNextId("clothing", "idClothing");
        System.out.print("選擇服飾類型 (1. 預購, 2. 現貨)：");
        int typeChoice = getChoice(); // 讓用戶選擇預購或現貨

        // 根據用戶選擇，設置服飾類型
        boolean isPreorder = (typeChoice == 1); // 預購為 true，現貨為 false

        System.out.print("服飾風格：");
        String style = scanner.nextLine();
        System.out.print("輸入服飾名稱：");
        String name = scanner.nextLine();
        System.out.print("輸入價格預算：");
        int price = getChoice();
        System.out.print("輸入服飾尺寸：");
        String size = scanner.nextLine();
        System.out.print("輸入服飾顏色：");
        String color = scanner.nextLine();

        // 插入資料庫，包括服飾類型
        int result = DBConnect.executeUpdate(
                "INSERT INTO clothing (idClothing, style, `Clothing Name`, Price, Size, Color, isPreorder) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, style, name, price, size, color, isPreorder);

        System.out.println(result > 0 ? "✅ 服飾新增成功！" : "❌ 新增失敗！");
    }

    // 顯示預購與現貨列表
    private static void listClothing() {
        try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM clothing")) {
            System.out.println("ID | 產品名稱 | 價格 | 風格 | 尺寸 | 顏色 | 是否為預購");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("idClothing") + " | " +
                                rs.getString("Clothing Name") + " | $" +
                                rs.getInt("Price") + " | " +
                                rs.getString("style") + " | " +
                                rs.getString("Size") + " | " +
                                rs.getString("Color"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 新增員工
private static void addEmployees() {
    int id = getNextId("employees", "idEmployees");
    System.out.print("輸入員工名稱：");
    String name = scanner.nextLine();
    System.out.print("輸入員工薪資：");
    int salary = getChoice();
    System.out.print("輸入員工地址：");
    String address = scanner.nextLine();
    System.out.print("輸入緊急聯絡人名稱：");
    String emergencyContactName = scanner.nextLine();
    System.out.print("輸入緊急聯絡人電話：");
    String emergencyContactPhone = scanner.nextLine();
    int result = DBConnect.executeUpdate(
            "INSERT INTO employees (idEmployees, EmployeeName, EmployeeSalaries, address, EmergencyContactName, EmergencyContactPhone) VALUES (?, ?, ?, ?, ?, ?)",
            id, name, salary, address, emergencyContactName, emergencyContactPhone);
    System.out.println(result > 0 ? "✅ 員工新增成功！" : "❌ 新增失敗！");
}

    // 員工列表
    private static void listEmployees() {
        try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM employees")) {
            System.out.println("ID | 員工名稱");
            while (rs.next()) {
                System.out.println(rs.getInt("idEmployees") + " | " + rs.getString("EmployeeName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 新增出貨人
    private static void addShipper() {
    int id = getNextId("shippers", "idshippers");
    System.out.print("輸入 Shipper 名稱：");
    String name = scanner.nextLine();
    System.out.print("輸入電話號碼：");
    String phone = scanner.nextLine();
    System.out.print("輸入 Shipping Order：");
    String shippingOrder = scanner.nextLine();
    System.out.print("輸入出貨日期（yyyy-MM-dd）：");
    String dateInput = scanner.nextLine();
    
    // 解析輸入的日期格式
    java.sql.Date date = null;
    try {
        date = java.sql.Date.valueOf(dateInput);  // 將輸入的日期字符串轉換為 java.sql.Date
    } catch (IllegalArgumentException e) {
        System.out.println("日期格式錯誤，請使用 yyyy-MM-dd 格式。");
        return;
    }

    // 執行資料庫操作
    int result = DBConnect.executeUpdate("INSERT INTO shippers (idshippers, shippername, phone, ShippingOrder, Date) VALUES (?, ?, ?, ?, ?)",
            id, name, phone, shippingOrder, date);
    System.out.println(result > 0 ? "✅ Shipper 新增成功！" : "❌ 新增失敗！");
}

    // Shipper列表
    private static void listShippers() {
        try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM shippers")) {
            System.out.println("ID | Shipper 名稱 | 電話");
            while (rs.next()) {
                System.out.println(
    rs.getInt("idShippers") + " | " + rs.getString("shippername") + " | " + rs.getString("phone"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 新增訂單
   private static void addOrder() {
        int id = getNextId("orders", "idOrders");
        System.out.print("輸入產品 ID：");
        int productId = getChoice();
        System.out.print("輸入數量：");
        int num = getChoice();
        System.out.print("輸入客戶 ID：");
        int customerId = getChoice();
        System.out.print("輸入 Shipper ID：");
        int shipperId = getChoice();
        int result = DBConnect.executeUpdate("INSERT INTO orders (idOrders, ProductID, DateTime, Num, Remark, CustomerID, ShipperID) VALUES (?, ?, NOW(), ?, '無', ?, ?)", id, productId, num, customerId, shipperId);
        System.out.println(result > 0 ? "✅ 訂單新增成功！" : "❌ 新增失敗！");
    }


    // 顯示訂單列表
    private static void listOrders() {
        try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM orders")) {
            System.out.println("ID | 服飾 ID | 數量 | 客戶 ID | Shipper ID | 訂單類型");
            while (rs.next()) {
                System.out.println(rs.getInt("idOrders") + " | " +
                        rs.getInt("ClothingID") + " | " +
                        rs.getInt("Num") + " | " +
                        rs.getInt("CustomersID") + " | " +
                        rs.getInt("ShippersID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getNextId(String tableName, String columnName) {
        try (ResultSet rs = DBConnect.selectQuery("SELECT MAX(" + columnName + ") FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}