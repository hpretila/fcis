import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CMSTest {
    private static final String REPORT_DIRECTORY = System.getProperty("java.io.tmpdir");
    private static final String REPORT_NAME = String.format("cms-%s.json", LocalDateTime.now().format(ISO_DATE));
    private static final File REPORT_FILE = new File(REPORT_DIRECTORY, REPORT_NAME);

    @BeforeAll
    public static void run_cms() {
        REPORT_FILE.delete();
        new CMS().storeCmsSummaryReport();
    }

    @Test
    public void cms_writes_report_file() {
        assertTrue(REPORT_FILE.exists(),
                String.format("Report must exist: %s", REPORT_FILE.getAbsolutePath()));
    }

    @Test
    public void report_file_contains_cms_data() throws IOException {
        JSONObject json = new JSONObject(Files.readString(REPORT_FILE.toPath()));
        assertEquals(100, json.getInt("posts"), "posts should be 100");
        assertEquals(10, json.getInt("users"), "users should be 10");
    }

    @Test
    public void calculate_mean_users() throws IOException {
        JSONObject json = new JSONObject(Files.readString(REPORT_FILE.toPath()));
        assertEquals(100, json.getInt("posts"), "posts should be 100");
    }

    /// Unit tests
    /// Extract
    // A -- patch with fixtures
    public String fixturePostsJson() {
        return """
            [
                {
                    \"userId\": 1,
                    \"id\": 1,
                    \"title\": \"sunt aut facere repellat provident occaecati excepturi optio reprehenderit\",
                    \"body\": \"quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto\"
                },
                {
                    \"userId\": 2,
                    \"id\": 2,
                    \"title\": \"qui est esse\",
                    \"body\": \"est rerum tempore vitae\\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\\nqui aperiam non debitis possimus qui neque nisi nulla\"
                },
                {
                    \"userId\": 3,
                    \"id\": 3,
                    \"title\": \"doloribus ad provident suscipit at\",
                    \"body\": \"qui consequuntur ducimus possimus quisquam amet similique\\nsuscipit porro ipsam amet\\neos veritatis officiis exercitationem vel fugit aut necessitatibus totam\\nomnis rerum consequatur expedita quidem cumque explicabo\"
                },
                {
                    \"userId\": 3,
                    \"id\": 4,
                    \"title\": \"asperiores ea ipsam voluptatibus modi minima quia sint\",
                    \"body\": \"repellat aliquid praesentium dolorem quo\\nsed totam minus non itaque\\nnihil labore molestiae sunt dolor eveniet hic recusandae veniam\\ntempora et tenetur expedita sunt\"
                },
                {
                    \"userId\": 2,
                    \"id\": 5,
                    \"title\": \"doloribus ad provident suscipit at\",
                    \"body\": \"qui consequuntur ducimus possimus quisquam amet similique\\nsuscipit porro ipsam amet\\neos veritatis officiis exercitationem vel fugit aut necessitatibus totam\\nomnis rerum consequatur expedita quidem cumque explicabo\"
                },
                {
                    \"userId\": 2,
                    \"id\": 6,
                    \"title\": \"doloribus ad provident suscipit at\",
                    \"body\": \"qui consequuntur ducimus possimus quisquam amet similique\\nsuscipit porro ipsam amet\\neos veritatis officiis exercitationem vel fugit aut necessitatibus totam\\nomnis rerum consequatur expedita quidem cumque explicabo\"
                }
            ]
        """;
    }

    public List<Integer> fixtureUserIdsList() {
        return Arrays.asList(1,2,3);
    }

    // B Deserialise valid JSON
    @Test
    public void testDeserialisePostsJson() {
        // Deserialise
        JSONArray posts = new CMS().deserialisePostsJson(
            fixturePostsJson()
        );

        // Assert length
        assertEquals(6, posts.length());

        // Introspect post
        JSONObject post = posts.getJSONObject(0);
        // Assert userID
        assertEquals(1, post.get("userId"));
    }

    /// TRANSFORM
    // C Take deserialised JSON and accumulate all unique user IDs
    @Test 
    public void testExtractUserIDs() {
        // TODO: Generate fixtures
        JSONArray fixturePosts = new CMS().deserialisePostsJson(
            fixturePostsJson()
        );
        
        // Extract user IDs
        List<Integer> userIds = new CMS().extractUserIDs(
            fixturePosts
        );

        // Assert accumulated users
        assertEquals(3, userIds.size());
    }

    // D Extract metrics
    @Test
    public void testTransformSummary() {
        // Extract user IDs
        JSONArray postArray = new CMS().deserialisePostsJson(
            fixturePostsJson()
        );
        List<Integer> fixtureUserIds = fixtureUserIdsList();

        // Extract metrics
        String transformSummary = new CMS().generateTransformSummary(
            postArray,
            fixtureUserIds
        );

        assertEquals("{ \"posts\": 6, \"users\": 3, \"mean_posts_per_user\": 2 }",
                    transformSummary);
    }

    /// LOAD
    // F Dump
}
