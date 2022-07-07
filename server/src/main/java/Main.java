import static spark.Spark.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Main {

    public static void main(String[] args) {
        //set relative paths
        String dis_dir = ".\\resources\\Distributors.xlsx";
        String inv_dir = ".\\resources\\Inventory.xlsx";

        //list of list strings to hold parsed data
        List<List<String>> invData;
        List<List<String>> disData;
        List<List<String>> restock;

        //functions parsing and checking worksheets for validity
        disData = parseExcel(dis_dir);
        disData = checkDistrib(disData);
        invData = parseExcel(inv_dir);
        restock = findRestock(invData);

        JSONArray jRestock = JsonConverter(restock);


        //This is required to allow GET and POST requests with the header 'content-type'
        options("/*",
                (request, response) -> {
                    response.header("Access-Control-Allow-Headers",
                            "content-type");

                    response.header("Access-Control-Allow-Methods",
                            "GET, POST");


                    return "OK";
                });

        //This is required to allow the React app to communicate with this API
        before((request, response) -> response.header("Access-Control-Allow-Origin", "http://localhost:3000"));

        //TODO: Return JSON containing the candies for which the stock is less than 25% of it's capacity
        get("/low-stock", (request, response) -> {
            System.out.println("Items to be restocked");
            return jRestock;
        });

        //TODO: Return JSON containing the total cost of restocking candy
        List<List<String>> finalDistributorsData = disData;
        post("/restock-cost", (request, response) -> {
            System.out.println("Cost of restocking");
            return getPost(request.body(), finalDistributorsData);
        });
    }

    public static List<List<String>> parseExcel(String dir) {
        File disParse = new File(dir);
        List<List<String>> arrList = new ArrayList<>();
        try {
            FileInputStream dis = new FileInputStream(disParse);
            XSSFWorkbook thisWorkbook = new XSSFWorkbook(dis);

            for (int i = 0; i < thisWorkbook.getNumberOfSheets(); i++) {
                XSSFSheet thisWork = thisWorkbook.getSheetAt(i);
                Iterator<Row> rowIterator = thisWork.iterator();

                while (rowIterator.hasNext()) {
                    //iterate through each row
                    Row curRow = rowIterator.next();
                    Iterator<Cell> cellIterator = curRow.cellIterator();
                    List<String> str = new ArrayList<>();
                    //iterate through each cell
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        //convert to strings and add to arrList of strings
                        if (!cell.toString().equals("")) {
                            str.add(cell.toString());
                        }
                    }
                    //after row is finished, add row as list of lists
                    arrList.add(str);
                }
            }
            thisWorkbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrList;
    }

    public static JSONArray getPost(String body, List<List<String>> disPost){
        JSONArray jArr = new JSONArray(body);
        //set basetotalCost
        double totalCost = 0.0;
        //hashmap of key value pairs to parse unique id's
        Map<Integer, List<String>> itemList = new HashMap<Integer, List<String>>();

        for(int i = 0;i < jArr.length();i++){
            JSONObject jObj = jArr.getJSONObject(i);
            System.out.println("Object " + i + " ");
            System.out.println(jObj);

            List<String> temp = new ArrayList<>(Arrays.asList(jObj.getString("ID"), jObj.getString("value")));

            itemList.put(i, temp);
        }

        for(int i = 0;i < itemList.size();i++){
            String itemID = itemList.get(i).get(0);
            //high cost to be greater than
            double tempCost = 99999.99;

            for(int k = 0;k < disPost.size();i++){
                //disPost = parseExcel List<List<String>> get(k)=row,.get(1)= second col entry
                if(Objects.equals(disPost.get(k).get(1), itemID)){
                    tempCost = Math.min(tempCost, Double.parseDouble(disPost.get(k).get(2)));
                }
            }
            totalCost = totalCost+(tempCost*Integer.parseInt(itemList.get(i).get(1)));
        }
        System.out.println("Total cost: " + totalCost);
        //object and array for objects
        JSONArray x = new JSONArray();
        JSONObject y = new JSONObject();
        //specify decimal placement
        y.put("cost", String.format("%.2f%", totalCost));
        //insert JSON objects into JSON array
        x.put(y);
        return(x);
    }

    public static JSONArray JsonConverter(List<List<String>> jList){
        //Converting data into JSONObject and inserting into JSONArray
        String[] cols = {"Name", "Inventory", "Capacity", "ID"};
        JSONArray jArr = new JSONArray();
        for(int i = 0; i < jList.size();i++){
            //make JSON object to be instantiated
            JSONObject jObj = new JSONObject();
            for(int k = 0;k < 4;k++){
                //put index of pos in col and a jList value
               jObj.put(cols[k], jList.get(k));
            }
            jArr.put(jObj);
        }
        return jArr;
    }

    public static List<List<String>> findRestock(List<List<String>> list){
        List<List<String>> restock = new ArrayList<>();
        //iterate through row entries started after col name
        //
        // for(List<String> row : list.subList(1, list.size())){
        for(List<String> row : list.subList(1, list.size())){
            //calc if stock is 25% and add to list to return
            if(Float.parseFloat(row.get(1))/Float.parseFloat(row.get(2)) < 0.25){
                System.out.println(row.get(0) + " needs to be refilled");
                restock.add(row);
            }
        }
        return restock;
    }

    public static List<List<String>> checkDistrib(List<List<String>> list){
        List<List<String>> cleaned = new ArrayList<>();
        for (List<String> x : list){
            try{
                if (Float.parseFloat(x.get(1)) > 0.0001){
                    cleaned.add(x);
                }
            }catch(Exception e){
                continue;
            }

        }

        return cleaned;
    }

}
