package com.example.demo.service.implement;

import com.example.demo.exception.InValidException;
import com.example.demo.exception.NoContentException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.helpers.Helper;
import com.example.demo.message.ContractMessage;
import com.example.demo.model.DTO.ContractDTO;
import com.example.demo.model.entity.Apartment;
import com.example.demo.model.entity.Contract;
import com.example.demo.model.entity.Customer;
import com.example.demo.model.validator.ContractValidator;
import com.example.demo.repository.ContractRepository;
import com.example.demo.service.interfaces.ApartmentService;
import com.example.demo.service.interfaces.ContractService;
import com.example.demo.service.interfaces.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Log4j2
public class ContractServiceImp implements ContractService {
    private final ContractRepository contractRepository;
    private final CustomerService customerService;
    private final ApartmentService apartmentService;
    private final ModelMapper mapper;

    @Override
    public List<ContractDTO> getAll() throws NoContentException {
        List<Contract> contractList = contractRepository.findAll();
        if (contractList.isEmpty()) throw new NoContentException(ContractMessage.EMPTY_LIST);

        return mapper.map(contractList, new TypeToken<List<ContractDTO>>() {
        }.getType());
    }

    @Override
    public ContractDTO getOneById(String id) throws NotFoundException {
        Contract contractFromDB = contractRepository.findById(id).orElse(null);
        if (contractFromDB == null) throw new NotFoundException(ContractMessage.NOT_FOUND);

        log.info(contractFromDB);

        return contractEntityToDTO(contractFromDB);
    }

    @Override
    public ContractDTO create(ContractDTO contractDTO) throws InValidException {
        Helper.setAllFieldNullToEmpty(contractDTO);

        ContractValidator.validator(contractDTO);
        log.info(contractDTO.toString());

        // Find customer and apartment via id
        Customer customerFromDB = customerService.customerDTOToEntity(customerService.getOneById(contractDTO.getCustomerId()));
        Apartment apartmentFromDB = apartmentService.apartmentDTOToEntity(apartmentService.getOneById(contractDTO.getApartmentId()));

        if (customerFromDB == null) throw new InValidException(ContractMessage.NOT_EXIST_CUSTOMER_ID);
        if (apartmentFromDB == null) throw new InValidException(ContractMessage.NOT_EXIST_APARTMENT_ID);

        // Create a contract to save
        Contract contractToCreate = contractDTOToEntity(contractDTO);
        log.info(contractToCreate.toString());

        contractToCreate.setCustomer(customerFromDB);
        contractToCreate.setApartment(apartmentFromDB);

        return contractEntityToDTO(contractToCreate);
    }

    @Override
    public ContractDTO update(String id, Map<String, Object> payload) {
        return null;
    }

    @Override
    public ContractDTO delete(String id) throws NotFoundException {
        Contract contractFromDB = contractRepository.findById(id).orElse(null);
        if (contractFromDB == null) throw new NotFoundException(ContractMessage.NOT_FOUND);

        contractRepository.delete(contractFromDB);

        return contractEntityToDTO(contractFromDB);
    }

    private Contract contractDTOToEntity(ContractDTO contractDTO) {
        return mapper.map(contractDTO, Contract.class);
    }

    private ContractDTO contractEntityToDTO(Contract contract) {
        return mapper.map(contract, ContractDTO.class);
    }
}
