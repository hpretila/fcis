import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
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
    private static final String ENDPOINT_URI = "https://jsonplaceholder.typicode.com/posts";

    public static void main(String[] args) {
        new CMS().storeCmsSummaryReport();
    }

    public void storeCmsSummaryReport() {
        System.out.println("Getting CMS data");
        
        /// Extract
        // A. Posts text fetch
        String postsText = fetchPostsString(ENDPOINT_URI);

        // B. Deserialise
        JSONArray posts = deserialisePostsJson(postsText);
        System.out.printf("CMS data has %d records%n", posts.length());

        /// TRANSFORM
        // C. Count/accumulate based on posts user IDs
        List<Integer> userIds = extractUserIDs(posts);

        // D. Generate summary
        String summary = generateTransformSummary(posts, userIds);
        
        /// LOAD
        // E. Dump JSON string
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE))) {
            writer.write(summary);
        } catch (IOException e) {
            System.err.printf("Unable to write report %s: %s%n",
                    REPORT_FILE.getAbsolutePath(), e.getMessage());
        }
        System.out.println("Wrote CMS report");
    }

    /**
     * Side-effect-ful code.
     * 
     * @param endpoint
     * @return 
     */
    public String fetchPostsString(String endpoint) {
        String postsText = "";

        try {
            URL postsUrl = new URI(endpoint).toURL();
            try (InputStream is = postsUrl.openStream()) {
                postsText = new String(is.readAllBytes(), UTF_8);
            }
        } catch (URISyntaxException ex) {
            System.err.printf("Failed URI Syntax, error %s", ex.getMessage());
        } catch (IOException ex) {
            System.err.printf("No valid JSON response from CMS, error %s", ex.getMessage());
        }

        return postsText;
    }

    /**
     * Deserialise Posts JSON
     * 
     * @param postsJsonString
     * @return
     */
    public JSONArray deserialisePostsJson(String postsJsonString) {
        return new JSONArray(postsJsonString);
    }

    /**
     * Extract unique user IDs from Posts JSON into a list.
     * 
     * @param posts
     * @return
     */
    public List<Integer> extractUserIDs(JSONArray posts) {
        List<Integer> userIds = new ArrayList<>();
        for(int i = 0; i < posts.length(); i++) {
            JSONObject post = (JSONObject)posts.get(i);
            int userId = post.getInt("userId");
            if (!userIds.contains(userId)) {
                userIds.add(userId);
            }
        }
        return userIds;
    }

    /**
     * Generate a summary from extracted metrics.
     * 
     * @param postArray
     * @param fixtureUserIds
     * @return
     */
	public String generateTransformSummary(JSONArray postArray, List<Integer> userIds) {
        Integer postCount = postArray.length();
        Integer userCount = userIds.size();
		return String.format("{ \"posts\": %d, \"users\": %d, \"mean_posts_per_user\": %d }",
                    postCount, userCount, Math.round((float)postCount / (float)userCount));
	}
}
