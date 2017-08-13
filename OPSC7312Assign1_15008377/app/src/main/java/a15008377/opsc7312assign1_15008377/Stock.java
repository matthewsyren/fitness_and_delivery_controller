/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class provides a template for a Stock object
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import static android.content.Context.MODE_APPEND;

@SuppressWarnings("WeakerAccess")
public class Stock implements Serializable{
    //Declarations
    private String stockID;
    private String stockDescription;
    private int stockQuantity;

    //Constructor
    public Stock(String stockID, String stockDescription, int stockQuantity){
        this.stockID = stockID;
        this.stockDescription = stockDescription;
        this.stockQuantity = stockQuantity;
    }

    //Default constructor
    public Stock(){}

    //Getter methods
    public String getStockID() {
        return stockID;
    }

    public String getStockDescription() {
        return stockDescription;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    //Setter methods
    public void setStockQuantity(int stockQuantity){
        this.stockQuantity = stockQuantity;
    }

    //Method returns true if the Stock object has valid values for all of its attributes, otherwise it returns false
    public boolean validateStock(Context context){
        boolean validStock = false;

        //If statements check numerous validation criteria for the Stock object.
        if(stockID.length() == 0){
            Toast.makeText(context, "Please enter a Stock ID", Toast.LENGTH_LONG).show();
        }
        else if(stockID.contains("|")){
            Toast.makeText(context, "Please remove all | symbols from the Stock ID", Toast.LENGTH_LONG).show();
        }
        else if(stockDescription.length() == 0){
            Toast.makeText(context, "Please enter a Stock Description", Toast.LENGTH_LONG).show();
        }
        else if(stockDescription.contains("|")){
            Toast.makeText(context, "Please remove all | symbols from the Stock Description", Toast.LENGTH_LONG).show();
        }
        else if(stockQuantity < 0){
            Toast.makeText(context, "Please enter a Stock Quantity that is more than or equal to 0", Toast.LENGTH_LONG).show();
        }
        else{
            validStock = true;
        }

        return validStock;
    }

    //Method returns an ArrayList of all Stock items in the Stock.txt text file
    public static ArrayList<Stock> readStockItems(Context context) throws IOException {
        String line;
        ArrayList<Stock> lstStock = new ArrayList<>();
        File file = new File(context.getFilesDir(), "Stock.txt");
        FileInputStream fileInputStream = context.openFileInput(file.getName());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        //Loops through the file and instantiate a Stock object for each line, and adding that Stock object to the lstStock ArrayList
        while((line = bufferedReader.readLine()) != null){
            String[] part = line.split("\\|");
            Stock stock = new Stock(part[0], part[1], Integer.parseInt(part[2]));
            lstStock.add(stock);
        }

        return lstStock;
    }

    //Method deletes a Stock item from the Stock.txt file
    public void deleteStockItem(String stockID, Context context) throws IOException{
        boolean foundStockID = false;

        //Loops through the Stock items until the item to be removed is found and removed
        ArrayList<Stock> lstStock = Stock.readStockItems(context);
        for(int i = 0; i < lstStock.size() && !foundStockID; i++) {
            if(lstStock.get(i).getStockID().equals(stockID)){
                lstStock.remove(i);
                i--;
                foundStockID = true;
            }
        }

        //Updates the contents of the Stock.txt text file
        rewriteFile(lstStock, context);
        Toast.makeText(context, "Stock item successfully deleted", Toast.LENGTH_LONG).show();
    }

    //Method deletes the contents of the Stock.txt file and rewrites its content (used once a Stock item has been updated or deleted)
    public void rewriteFile(ArrayList<Stock> lstStock, Context context) throws IOException{
        //Clears contents of file
        File file = new File(context.getFilesDir(), "Stock.txt");
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.close();

        //Writes updated data to the Stock.txt text file
        FileOutputStream fileOutputStream = context.openFileOutput(file.getName(), MODE_APPEND);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        for(int i = 0; i < lstStock.size(); i++){
            outputStreamWriter.write(lstStock.get(i).getStockID() + "|" + lstStock.get(i).getStockDescription() + "|" + lstStock.get(i).getStockQuantity() + "\n");
        }
        outputStreamWriter.close();
    }
}