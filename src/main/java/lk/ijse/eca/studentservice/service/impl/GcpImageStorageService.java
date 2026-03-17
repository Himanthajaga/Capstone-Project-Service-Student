package lk.ijse.eca.studentservice.service.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lk.ijse.eca.studentservice.exception.FileOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GcpImageStorageService {
    private final Storage storage;

    @Value("${gcp.storage.bucket}")
    private String bucket;

    @Value("${gcp.storage.prefix:students}")
    private String prefix;

    public void save(String pictureId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileOperationException("Picture file must not be empty");
        }

        try {
            BlobId blobId = BlobId.of(bucket, objectName(pictureId));
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());
        } catch (IOException e) {
            throw new FileOperationException("Failed to upload picture: " + pictureId, e);
        }
    }

    public byte[] read(String pictureId) {
        byte[] data = storage.readAllBytes(bucket, objectName(pictureId));
        if (data == null || data.length == 0) {
            throw new FileOperationException("Picture not found or empty: " + pictureId);
        }
        return data;
    }

    public void delete(String pictureId) {
        boolean deleted = storage.delete(bucket, objectName(pictureId));
        if (!deleted) {
            throw new FileOperationException("Failed to delete picture: " + pictureId);
        }
    }
    public void update(String pictureId, MultipartFile file) {
        delete(pictureId);
        save(pictureId, file);
    }

    private String objectName(String pictureId) {
        return prefix + "/" + pictureId;
    }
}
