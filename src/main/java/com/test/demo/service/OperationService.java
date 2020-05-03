package com.test.demo.service;

import com.test.demo.dto.OperationDTO;
import com.test.demo.model.Account;
import com.test.demo.model.Client;
import com.test.demo.model.Operation;
import com.test.demo.repository.AccountRepository;
import com.test.demo.repository.ClientRepository;
import com.test.demo.repository.OperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class OperationService {

    private OperationRepository operationRepository;
    private ClientRepository clientRepository;
    private AccountRepository accountRepository;
    private EmailService emailService;
    private Logger log = Logger.getLogger(OperationService.class.getName());

    @Autowired
    public OperationService(OperationRepository operationRepository, ClientRepository clientRepository, AccountRepository accountRepository, EmailService emailService) {
        this.operationRepository = operationRepository;
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.emailService = emailService;
    }

    public OperationDTO findOperationById(String id) {
        log.info("Fetching operation by id...");
        Optional<Operation> optionalOperation = operationRepository.findById(Integer.parseInt(id));
        if (optionalOperation.isPresent()) {
            Operation operation = optionalOperation.get();
            return new OperationDTO(operation);
        } else {
            return new OperationDTO();
        }
    }

    public List<OperationDTO> getAllOperations(Principal principal) {
        log.info("Listing operations...");

        Client client = clientRepository.findByUsername(principal.getName());
        List<OperationDTO> operations = new ArrayList<>();

        //get operations for a client
        if (!client.getUsername().equals("admin")) {
            log.info("User is admin, fetching ALL operations...");
            operationRepository.getOperationsByClientId(client.getId()).forEach(operation -> {
                OperationDTO op = new OperationDTO()
                        .setId(operation.getId())
                        .setAccount(operation.getAccount())
                        .setAmount(operation.getAmount())
                        .setClient(operation.getClient())
                        .setDate(operation.getDate())
                        .setType(operation.getType());
                operations.add(op);
            });
        } // get ALL operations
        else {
            log.info("User is not admin, fetching personal operations...");
            operationRepository.findAll().forEach(operation -> {
                OperationDTO operationDTO = new OperationDTO()
                        .setId(operation.getId())
                        .setAccount(operation.getAccount())
                        .setAmount(operation.getAmount())
                        .setClient(operation.getClient())
                        .setDate(operation.getDate())
                        .setType(operation.getType());

                operations.add(operationDTO);
            });
        }

        return operations;

    }

    public OperationDTO createOperation(Principal principal, int accountId, int transferId, String type, Double amount) throws IOException {
        log.info("New operation...");
        log.info("Set up transaction details...");

        LocalDate date = LocalDate.now();
        Account account = accountRepository.findAccountById(accountId);
        Client client = clientRepository.findByUsername(principal.getName());
        Operation operation = new Operation()
                .setAmount(amount)
                .setDate(date)
                .setType(type)
                .setClient(client);
        if (transferId != 0) {

            Account transfer = accountRepository.findAccountById(transferId);
            operation.setAccount(transfer);
            emailService.createPDF(operation, principal, transfer);
        } else {
            operation.setAccount(account);
            emailService.createPDF(operation, principal, null);
        }

        operationRepository.save(operation);
        return new OperationDTO()
                .setAccount(account)
                .setAmount(amount)
                .setDate(date)
                .setType(type)
                .setClient(client);
    }
}
