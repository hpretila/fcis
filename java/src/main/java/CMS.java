import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_DATE;

public class CMS {
    private static final String REPORT_DIRECTORY = System.getProperty("java.io.tmpdir");
    private static final String REPORT_NAME = String.format("cms-%s.json", LocalDateTime.now().format(ISO_DATE));
    private static final File REPORT_FILE = new File(REPORT_DIRECTORY, REPORT_NAME);

    public void storeCmsSummaryReport() {
        System.out.println("Getting CMS data");
        
        // A Posts text fetch
        String postsText = "";
        try {
            URL postsUrl = new URL("https://jsonplaceholder.typicode.com/posts");
            try (InputStream is = postsUrl.openStream()) {
                postsText = new String(is.readAllBytes(), UTF_8);
            }
        } catch (IOException ex) {
            System.err.printf("No valid JSON response from CMS, error %s", ex.getMessage());
        }

        // B Deserialise
        JSONArray posts = new JSONArray(postsText);
        System.out.printf("CMS data has %d records%n", posts.length());

        // C Count/accumulate based on posts user IDs
        List<Integer> userIds = new ArrayList<>();
        for(int i = 0; i < posts.length(); i++) {
            JSONObject post = (JSONObject)posts.get(i);
            int userId = post.getInt("userId");
            if (!userIds.contains(userId)) {
                userIds.add(userId);
            }
        }

        // Dump
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE))) {
            // D Retrieval
            // Retrieve post length
            int postCount = posts.length();
            // Retrieve user count
            int userCount = userIds.size();

            // E Transform
            // Transform into raw JSON string
            String summary = String.format("{ \"posts\": %d, \"users\": %d, \"mean_posts_per_user\": %d }",
                    postCount, userCount, Math.round((float)postCount / (float)userCount));
            
            // F Dump
            // Dump JSON string
            writer.write(summary);
        } catch (IOException e) {
            System.err.printf("Unable to write report %s: %s%n",
                    REPORT_FILE.getAbsolutePath(), e.getMessage());
        }
        System.out.println("Wrote CMS report");
    }

    public static void main(String[] args) {
        new CMS().storeCmsSummaryReport();
    }
}
