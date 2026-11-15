package valleysoft.minibasket.user.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.user.dto.UserResponse;
import valleysoft.minibasket.user.mapper.UserMapper;
import valleysoft.minibasket.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return userRepository.findWithRoleById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND, Map.of("id", userId)));
    }
}
