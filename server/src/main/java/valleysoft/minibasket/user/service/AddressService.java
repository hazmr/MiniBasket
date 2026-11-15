package valleysoft.minibasket.user.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import valleysoft.minibasket.common.exception.DomainException;
import valleysoft.minibasket.common.exception.ErrorCode;
import valleysoft.minibasket.user.dto.AddressRequest;
import valleysoft.minibasket.user.dto.AddressResponse;
import valleysoft.minibasket.user.entity.Address;
import valleysoft.minibasket.user.mapper.AddressMapper;
import valleysoft.minibasket.user.repository.AddressRepository;
import valleysoft.minibasket.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(Long userId) {
        return addressRepository.findAllByUserIdOrderByIdAsc(userId).stream().map(addressMapper::toResponse).toList();
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        List<Address> existing = addressRepository.findAllByUserIdOrderByIdAsc(userId);
        Address address = addressRepository.save(
                new Address(userRepository.getReferenceById(userId), request.street().trim(), request.city().trim()));

        if (request.defaultAddress() || existing.isEmpty()) {
            makeDefault(address, existing);
        }
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = findAddress(userId, addressId);
        address.update(request.street().trim(), request.city().trim());
        if (request.defaultAddress()) {
            makeDefault(address, addressRepository.findAllByUserIdOrderByIdAsc(userId));
        }
        return addressMapper.toResponse(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        addressRepository.delete(findAddress(userId, addressId));
    }

    private Address findAddress(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new DomainException(ErrorCode.ADDRESS_NOT_FOUND, Map.of("id", addressId)));
    }

    private void makeDefault(Address chosen, List<Address> userAddresses) {
        userAddresses.forEach(address -> address.setDefaultAddress(false));
        chosen.setDefaultAddress(true);
    }
}
