package com.bazar;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.*;

public class App {
    public static void main(String[] args) {
        port(4569); // Order server

        post("/purchase/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            String catalogUrl = "http://localhost:4568/info/" + id;

            // Call catalog to get book info
            URL url = new URL(catalogUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                jsonResponse.append(line);
            }
            in.close();

            Gson gson = new Gson();
            JsonObject book = gson.fromJson(jsonResponse.toString(), JsonObject.class);
            int quantity = book.get("quantity").getAsInt();

            if (quantity <= 0) {
                res.status(400);
                return "Purchase failed: Book is out of stock.";
            }

            // Decrease quantity by 1
            int newQty = quantity - 1;
            book.addProperty("quantity", newQty);

            // Ideally you'd call a real update endpoint here!
            // For now, just simulate a success
            System.out.println("Purchased book: " + book.get("title").getAsString());

            return "Successfully purchased: " + book.get("title").getAsString();
        });
    }
}
