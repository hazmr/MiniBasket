package valleysoft.minibasket.storage;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties properties;

    public PresignedUpload presignImageUpload(String folder, String contentType, long size) {
        String extension = IMAGE_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new DomainException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
        if (size <= 0 || size > properties.maxUploadSize().toBytes()) {
            throw new DomainException(ErrorCode.IMAGE_TOO_LARGE, Map.of("maxMegabytes", properties.maxUploadSize().toMegabytes()));
        }

        String key = folder + "/" + UUID.randomUUID() + "." + extension;
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();
        var presigned = s3Presigner.presignPutObject(request -> request
                .signatureDuration(properties.uploadUrlExpiry())
                .putObjectRequest(putRequest));

        return new PresignedUpload(key, presigned.url().toString(), Instant.now().plus(properties.uploadUrlExpiry()));
    }

    public Optional<StoredFile> find(String key) {
        try {
            HeadObjectResponse head = s3Client.headObject(request -> request.bucket(properties.bucket()).key(key));
            return Optional.of(new StoredFile(key, head.contentType(), head.contentLength()));
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public void upload(String key, byte[] content, String contentType) {
        s3Client.putObject(request -> request.bucket(properties.bucket()).key(key).contentType(contentType),
                RequestBody.fromBytes(content));
    }

    public void delete(String key) {
        s3Client.deleteObject(request -> request.bucket(properties.bucket()).key(key));
    }

    public String publicUrl(String key) {
        String base = properties.publicUrl().endsWith("/") ? properties.publicUrl() : properties.publicUrl() + "/";
        return base + key;
    }

    public long maxUploadBytes() {
        return properties.maxUploadSize().toBytes();
    }
}
