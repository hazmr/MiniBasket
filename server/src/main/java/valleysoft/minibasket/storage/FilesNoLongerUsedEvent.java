package valleysoft.minibasket.storage;

import java.util.List;

public record FilesNoLongerUsedEvent(List<String> keys) {
}
