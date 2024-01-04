package com.example.rokuremote;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpAsyncTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String postData = params[1]; // Data to be sent in the POST request

        try {
            return makeHttpPostRequest(urlString, postData);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // Handle the result (UI updates, etc.)
        // For example, you can update a TextView with the result.
    }

    private String makeHttpPostRequest(String urlString, String postData) throws IOException {
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            // Write the data to the output stream
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(postData.getBytes());
            outputStream.flush();
            outputStream.close();

            // Read the response from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result.toString();
    }
}
