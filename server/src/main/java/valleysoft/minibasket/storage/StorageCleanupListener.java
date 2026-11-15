package valleysoft.minibasket.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class StorageCleanupListener {

    private final StorageService storageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void deleteUnusedFiles(FilesNoLongerUsedEvent event) {
        for (String key : event.keys()) {
            try {
                storageService.delete(key);
            } catch (RuntimeException ex) {
                log.warn("Could not delete stored file {}: {}", key, ex.getMessage());
            }
        }
    }
}
