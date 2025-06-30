import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {
    public static void main(String[] args) throws IOException {
        // 建立一個綁定在 localhost:8000 的 server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        // 綁定 API 路由
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/customers", new custHandler());

        server.createContext("/api/1/customers", new TableDataHandler("select * from customers"));
        server.createContext("/api/1/orders", new TableDataHandler("select * from orders"));
        server.createContext("/api/1/clothing", new TableDataHandler("select * from clothing"));
        server.createContext("/api/1/shippers", new TableDataHandler("select * from shippers"));
        server.createContext("/api/1/employees", new TableDataHandler("select * from employees"));

        server.createContext("/api/2/insert", new InsertDataHandler());
        server.createContext("/api/2/update", new UpdateDataHandler());

        server.createContext("/chat/send", new ChatHandler.SendHandler());
        server.createContext("/chat/messages", new ChatHandler.MessageHandler());

        server.createContext("/postdemo", new postdemo());
        server.createContext("/getdemo", new postdemo());

        // 啟動 server
        server.start();
        System.out.println("Server started at http://localhost:8000");
    }

    static class postdemo implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String file = "temppost.txt";
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(body);
            }
            exchange.sendResponseHeaders(200, -1);
        }
    }

    // 1140517 add 櫃台人員
    static class getdemo implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello Welcome!";
            // 指定回傳格式:text, 開放完全存取 *
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            // 格式化回傳的內容 文字->bytes
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            // 啟動回傳，通知成功與長度，再回傳內容
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    static class TableDataHandler implements HttpHandler {
        String sql = "";

        public TableDataHandler(String sql) {
            this.sql = sql;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "HI  anan";
            StringBuilder json = new StringBuilder();
            json.append("[");
            try {
                ResultSet rs = DBConnect.selectQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                boolean firstRow = true;
                while (rs.next()) {
                    if (!firstRow)
                        json.append(",");
                    firstRow = false;

                    json.append("{");
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = meta.getColumnLabel(i);
                        String value = rs.getString(i);
                        if (i > 1)
                            json.append(",");
                        json.append("\"").append(columnName).append("\":");
                        json.append("\"").append(value == null ? "" : escapeJson(value)).append("\"");
                    }
                    json.append("}");
                }
                rs.getStatement().getConnection().close(); // 關閉連線
            } catch (Exception e) {
                json = new StringBuilder("{\"error\":\"" + e.getMessage() + "\"}");
            }

            json.append("]");

            response = json.toString();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            byte[] responseBytes = response.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private static String escapeJson(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
        }

    }

    // 定義 handler
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "HI anan";
            /*
             * try (ResultSet rs = DBConnect.selectQuery("SELECT * FROM orders")) {
             * while (rs.next()) {
             * response +="\n"+(rs.getString("idOrders") + " | " +
             * rs.getString("ClothingID") + " | " + rs.getString("NUM") + " | " +
             * rs.getString("CustomersID") + " | " + rs.getString("ShippersID") );
             * }
             * } catch (Exception e) {
             * e.printStackTrace();
             * }
             * /*
             */
            StringBuilder json = new StringBuilder();
            json.append("[");
            try {
                ResultSet rs = DBConnect.selectQuery("SELECT * FROM orders");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    first = false;
                    String id = rs.getString("idOrders");
                    String clothingID = rs.getString("ClothingID");

                    json.append("{")
                            .append("\"idOrders\":\"").append(id).append("\",")
                            .append("\"ClothingID\":\"").append(id).append("\"")
                            .append("}");
                }
                rs.getStatement().getConnection().close(); // 手動關閉連線
            } catch (Exception e) {
                json = new StringBuilder("{\"error\":\"" + e.getMessage() + "\"}");
            }
            json.append("]");
            response = json.toString();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            byte[] responseBytes = response.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    // 定義 customers
    static class custHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "HI anan";

            StringBuilder json = new StringBuilder();
            json.append("[");
            try {
                ResultSet rs = DBConnect.selectQuery("SELECT * FROM customers");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    first = false;
                    String id = rs.getString("idCustomers");
                    String CustomerName = rs.getString("CustomerName");

                    json.append("{")
                            .append("\"idCustomers\":\"").append(id).append("\",")
                            .append("\"CustomerName\":\"").append(id).append("\"")
                            .append("}");
                }
                rs.getStatement().getConnection().close(); // 手動關閉連線
            } catch (Exception e) {
                json = new StringBuilder("{\"error\":\"" + e.getMessage() + "\"}");
            }
            json.append("]");
            response = json.toString();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            byte[] responseBytes = response.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    static class InsertDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // ✅ 處理 CORS 預檢請求（OPTIONS）
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1); // 204 No Content
                return;
            }
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining());
                Map<String, String> map = DBConnect.parseJson(body);
                String targetTable = map.get("table"); // table:customers
                switch (targetTable) {
                    case "customers":
                        int cid = DBConnect.getNextId("customers", "idCustomers");
                        String cname = map.get("CustomerName");
                        String country = map.get("country");
                        String address = map.get("Address");
                        String PhoneNumber = map.get("PhoneNumber");
                        DBConnect.executeUpdate(
                                "INSERT INTO customers (idCustomers, CustomerName, Country, Address, PhoneNumber) VALUES (?, ?, ?, ?, ?)",
                                cid, cname, country, address, PhoneNumber);
                        break;

                    case "clothing":
                        int pid = DBConnect.getNextId("clothing", "idClothing");
                        String pname = map.get("Name");
                        String style = map.get("style");
                        String price = map.get("price");
                        String size = map.get("size");
                        String color = map.get("color");
                        String isPreorder = map.get("isPreorder");
                        DBConnect.executeUpdate(
                                "INSERT INTO clothing (idClothing, style, Name, price, Size, Color, isPreorder) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                pid, style, pname, price, size, color, isPreorder);
                        break;

                    case "orders":
                        int oid = DBConnect.getNextId("orders", "idOrders");
                        String pide = map.get("ClothingID");
                        String cide = map.get("CustomersID");
                        String eide = map.get("EmployeesID");
                        String side = map.get("ShippersID");
                        String remark = map.get("remark");
                        int num = Integer.parseInt(map.get("num"));
                        DBConnect.executeUpdate(
                                "INSERT INTO orders (idOrders, ClothingID, CustomersID, EmployeesID, ShippersID, NUM, OrderDate, remark) VALUES (?, ?, ?, ?, ?, ?,current_timestamp() , ?)",
                                oid, pide, cide, eide, side, num ,remark);
                        break;

                    case "employees":
                        int eid = DBConnect.getNextId("employees", "idEmployees");
                        String ename = map.get("Employeename");
                        String salaries = map.get("EmployeeSalaries");
                        String eaddr = map.get("address");
                        String econtact = map.get("EmergencyContactName");
                        String ephone = map.get("EmergencyContactPhone");
                        DBConnect.executeUpdate(
                                "INSERT INTO employees (idEmployees, EmployeeSalaries, Employeename, address, EmergencyContactPhone, EmergencyContactName) VALUES (?, ?, ?, ?, ?, ?)",
                                eid, salaries, ename, eaddr, ephone, econtact);
                        break;

                    case "shippers":
                        int sid = DBConnect.getNextId("shippers", "idShippers");
                        String sname = map.get("Shippername");
                        String sphone = map.get("phone");
                        DBConnect.executeUpdate(
                                "INSERT INTO shippers (idShippers, Shippername, Phone, ShippingOrder, Date) VALUES (?, ?, ?,current_timestamp() ,current_timestamp())",
                                sid, sname, sphone);
                        break;

                    default:
                        String errorResponse = "{\"error\": \"Unknown table: " + targetTable + "\"}";
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
                        exchange.getResponseBody().write(errorResponse.getBytes());
                        exchange.getResponseBody().close();
                        return;
                }
                exchange.sendResponseHeaders(200, -1);

            } catch (Exception e) {
                e.printStackTrace();
                String response = "{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        }
    }

     static class UpdateDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // ✅ 處理 CORS 預檢請求（OPTIONS）
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1); // 204 No Content
                return;
            }
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            try {
                String body = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining());
                System.out.println("test1");
                System.out.println(body.toString());
                Map<String, String> map = DBConnect.parseJson(body);
               
                String targetTable = map.get("table"); // table:orders
                System.out.println(targetTable);
                switch (targetTable) {
                    case "customers":
                        String cid = map.get("idCustomers");
                        String cname = map.get("CustomerName"); // table:customers,name:我的名字,phone:0910101010
                        String country = map.get("Country");
                        String address = map.get("Address");
                        String PhoneNumber = map.get("PhoneNumber");
                        int result = DBConnect.executeUpdate(
                        "UPDATE customers SET CustomerName=?, Country=?, Address=?, PhoneNumber=? WHERE idCustomers=?",
                             cname, country, address, PhoneNumber, cid);
                        break;

                    case "clothing":
                        String pid = map.get("idClothing");
                        String pname = map.get("Name");
                        String style = map.get("style");
                        String price = map.get("price");
                        String size = map.get("Size");
                        String color = map.get("Color");
                        String isPreorder = map.get("isPreorder");
                        int result2 = DBConnect.executeUpdate(
                           "UPDATE clothing SET `Name` = ?, `style` = ?, `price` = ?, `Size` = ?, `Color` = ?, `isPreorder` = ? WHERE (`idClothing` = ?)",
                                    pname, style, price, size, color, isPreorder, pid);
                        exchange.sendResponseHeaders(200, -1);
                        break;

                    case "orders"://{id:1,pname:1,odate:20xx-xx-ss 12:32:33,cname:1,sname:1,num:10,state:-1}
                        System.out.println("test2");
                        String oid = map.get("idOrders");
                        String o_pname = map.get("ClothingID");
                        String odate = map.get("OrderDate");
                        String o_cname = map.get("CustomersID");
                        String o_sname = map.get("ShippersID");
                        String o_ename = map.get("EmployeesID");
                        String remark = map.get("remark");
                        String o_num = map.get("NUM");
                        String state = map.get("state");//{state:-1} {state:0}
                        System.out.println(oid+","+o_pname+","+odate+","+o_cname+","+o_sname+","+o_ename+","+state+","+remark+","+o_num);
                        int result3 = DBConnect.executeUpdate(
                                "UPDATE `orders` SET `ClothingID` = ?, `OrderDate` = ?, `NUM` = ?, `remark` = ?,`CustomersID` = ?, `ShippersID` = ?,`EmployeesID` = ?, `state` = ? WHERE (`idOrders` = ?)"  //
                                ,o_pname,odate, o_num,remark, o_cname, o_sname,o_ename,state,oid);

                            exchange.sendResponseHeaders(200, -1);
                            break;

                    case "employees":
                        String eid = map.get("idEmployees");
                        String ename = map.get("Employeename");
                        String salars = map.get("EmployeeSalaries");
                        String eaddr = map.get("address");
                        String econtact = map.get("EmergencyContactName");
                        String ephone = map.get("EmergencyContactPhone");
                        int result4 = DBConnect.executeUpdate(
                        "UPDATE employees SET Employeename=?, EmployeeSalaries=?, address=?, EmergencyContactPhone=?, EmergencyContactName=? WHERE idEmployees=?",
                           ename, salars, eaddr, ephone, econtact, eid);
                        exchange.sendResponseHeaders(200, -1);
                           break;

                    case "shippers":
                        String sid = map.get("idShippers");
                        String sname = map.get("shippername");
                        String sphone = map.get("phone");
                        int result5 = DBConnect.executeUpdate(
                        "UPDATE shippers SET shippername=?, phone=? WHERE `idShippers` = ?",
                              sname, sphone, sid);
                        exchange.sendResponseHeaders(200, -1);
                        break;



                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                String response = "{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        }
    }
    }


