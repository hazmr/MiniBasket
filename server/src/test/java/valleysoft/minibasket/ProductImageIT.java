package valleysoft.minibasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ProductImageIT extends IntegrationTest {

    private static final byte[] IMAGE = "pretend this is a PNG".getBytes();

    private final HttpClient http = HttpClient.newHttpClient();
    private String admin;
    private long productId;

    @BeforeEach
    void setUp() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "ADMIN");
        admin = login(email);
        productId = createProduct("Mug", "12.50", 5);
    }

    @Test
    void admin_uploadsStraightToStorage_thenTheImageIsPublic_andRemovingItDeletesTheFile() throws Exception {
        String upload = mockMvc.perform(post("/api/v1/admin/products/{id}/images/upload-url", productId)
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content("""
                                {"contentType": "image/png", "size": %d}
                                """.formatted(IMAGE.length)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String key = JsonPath.read(upload, "$.key");
        String uploadUrl = JsonPath.read(upload, "$.uploadUrl");
        assertThat(key).startsWith("products/" + productId + "/").endsWith(".png");

        HttpResponse<Void> put = http.send(HttpRequest.newBuilder(URI.create(uploadUrl))
                .header("Content-Type", "image/png")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(IMAGE)).build(), HttpResponse.BodyHandlers.discarding());
        assertThat(put.statusCode()).isEqualTo(200);

        String image = mockMvc.perform(post("/api/v1/admin/products/{id}/images", productId)
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content("""
                                {"key": "%s"}
                                """.formatted(key)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int imageId = JsonPath.read(image, "$.id");
        String imageUrl = JsonPath.read(image, "$.imageUrl");
        assertThat(imageUrl).endsWith("/" + TestInfrastructure.BUCKET + "/" + key);
        assertThat(jdbc.queryForObject("select storage_key from product_images where id = ?", String.class, imageId))
                .as("the database stores the key, not the URL").isEqualTo(key);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(jsonPath("$.images[0].imageUrl").value(imageUrl));
        assertThat(http.send(HttpRequest.newBuilder(URI.create(imageUrl)).build(), HttpResponse.BodyHandlers.ofByteArray()).body())
                .as("anyone can load the image").isEqualTo(IMAGE);

        mockMvc.perform(delete("/api/v1/admin/products/{productId}/images/{imageId}", productId, imageId)
                        .header("Authorization", admin))
                .andExpect(status().isNoContent());
        assertThat(http.send(HttpRequest.newBuilder(URI.create(imageUrl)).build(), HttpResponse.BodyHandlers.discarding()).statusCode())
                .as("the file is deleted after the commit").isEqualTo(404);
    }

    @Test
    void attachingAnImageThatWasNeverUploaded_isRefused() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products/{id}/images", productId)
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content("""
                                {"key": "products/%d/never-uploaded.png"}
                                """.formatted(productId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("IMAGE_NOT_UPLOADED"));
    }

    @Test
    void uploadUrlForANonImageFile_isRefused() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products/{id}/images/upload-url", productId)
                        .header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content("""
                                {"contentType": "text/html", "size": 100}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_IMAGE_TYPE"));
    }

    @Test
    void customersCannotAskForUploadUrls() throws Exception {
        String customer = uniqueEmail();
        createVerifiedUser(customer, "CUSTOMER");
        mockMvc.perform(post("/api/v1/admin/products/{id}/images/upload-url", productId)
                        .header("Authorization", login(customer)).contentType(MediaType.APPLICATION_JSON).content("""
                                {"contentType": "image/png", "size": 100}
                                """))
                .andExpect(status().isForbidden());
    }
}
