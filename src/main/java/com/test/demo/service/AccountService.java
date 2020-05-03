package com.test.demo.service;

import com.test.demo.dto.AccountDTO;
import com.test.demo.dto.ResultDTO;
import com.test.demo.model.Account;
import com.test.demo.model.Client;
import com.test.demo.repository.AccountRepository;
import com.test.demo.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private AccountRepository accountRepository;
    private ClientRepository clientRepository;
    private OperationService operationService;
    private Logger log = Logger.getLogger(AccountService.class.getName());

    @Autowired
    public AccountService(AccountRepository accountRepository, ClientRepository clientRepository, OperationService operationService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.operationService = operationService;
    }

    public void seedAccounts() {
        seedAccount(1, 334.5, "Account 1", "detail 1", /*clientRepository.findByUsername("admin")*/ clientRepository.findByUsername("user1"));
        seedAccount(2, 33345.4, "Account 2", "detail 2", clientRepository.findByUsername("user"));
        seedAccount(3, 33.3, "Account 3", "detail 3", clientRepository.findByUsername("user1"));
    }

    private void seedAccount(int id, Double amount, String name, String details, Client client) {
        Account account = accountRepository.findAccountsByName(name);
        if (account == null) {
            Account newAccount = new Account()
                    .setId(id)
                    .setAmount(amount)
                    .setName(name)
                    .setDetails(details)
                    .setClient(client);
            accountRepository.save(newAccount);
        }
    }

    public List<AccountDTO> getAllAccounts(Principal principal) {

        log.info("Listing ALL accounts...");

        List<AccountDTO> accounts = new ArrayList<>();
        accountRepository.findAll().forEach(account -> {
            AccountDTO acc = new AccountDTO()
                    .setId(account.getId())
                    .setName(account.getName())
                    .setAmount(account.getAmount())
                    .setDetails(account.getDetails())
                    .setClient(account.getClient());
            accounts.add(acc);
        });
        return accounts;
    }


    public List<AccountDTO> getAccountsByClientCnp(Principal principal) {
        log.info("Listing all accounts based on client's CNP...");

        Client client = clientRepository.findByUsername(principal.getName());
        List<AccountDTO> accounts = new ArrayList<>();

        accountRepository.findAccountsByClient_Cnp(client.getCnp()).forEach(x -> {
            AccountDTO acc = new AccountDTO()
                    .setId(x.getId())
                    .setAmount(x.getAmount())
                    .setClient(x.getClient())
                    .setName(x.getName())
                    .setDetails(x.getDetails());
            accounts.add(acc);
        });

        return accounts.stream().collect(Collectors.toList());
    }

    public AccountDTO createAccount(@RequestBody AccountDTO account, Principal principal) {
        log.info("Creating account...");

        Client client = clientRepository.findByUsername(principal.getName());
        Account newAccount = new Account()

                .setAmount(account.getAmount())
                .setName(account.getName())
                .setDetails(account.getDetails())
                .setClient(client);

        log.info("Account created...");
        return new AccountDTO(accountRepository.save(newAccount));
    }

    public ResultDTO deleteAccount(int id) {

        log.info("Deleting account...");

        Account deleteAccount = accountRepository.findAccountById(id);
        if (deleteAccount != null) {
            accountRepository.deleteAccountById(id);
            log.info("Account deleted...");
            return new ResultDTO().setStatus(true).setMessage("Account deleted!");
        } else {
            log.info("Something went wrong while executing deleteAccount(...) method...");
//            return new ResultDTO().setStatus(false).setMessage("No account with this id found!");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found!");
        }
    }

    public AccountDTO updateAccount(int id, AccountDTO accountDTO) {
        log.info("Updating account's informations...");

        Account updateAccount = accountRepository.findAccountById(id);
        updateAccount.setId(accountDTO.getId())
                .setName(accountDTO.getName())
                .setAmount(accountDTO.getAmount())
                .setDetails(accountDTO.getDetails());
        accountRepository.save(updateAccount);

        log.info("Account updated...");
        return new AccountDTO(updateAccount);
    }

    public ResultDTO depositMoney(Principal principal, int accountId, Double amount) throws IOException {
        log.info("Depositing money into account...");

        Account account = accountRepository.findAccountById(accountId);
        Double total = account.getAmount() + amount;
        account.setAmount(total);

        log.info("Saving new account state...");
        accountRepository.save(account);

        log.info("Creating new operation and preparing mail summary...");
        operationService.createOperation(principal, account.getId(), 0, "deposit", amount);
        return new ResultDTO().setStatus(true).setMessage("Money deposed!");
    }

    public ResultDTO withdrawMoney(Principal principal, int accountId, Double amount) throws IOException {
        log.info("Withdrawing money...");

        Account account = accountRepository.findAccountById(accountId);
        Double total = account.getAmount() - amount;
        account.setAmount(total);

        log.info("Saving new account state...");
        accountRepository.save(account);

        log.info("Creating operation and preparing mail summary...");
        operationService.createOperation(principal, account.getId(), 0, "deposit", amount);
        return new ResultDTO().setStatus(true).setMessage("Money deposed!");
    }

    public ResultDTO transferMoney(Principal principal, int senderAccountId, int receiverAccountId, Double amount) throws IOException {
        log.info("Transfering money...");

        Account account = accountRepository.findAccountById(senderAccountId);
        Account toSendTo = accountRepository.findAccountById(receiverAccountId);

        Double senderAmount = account.getAmount() - amount;
        account.setAmount(senderAmount);

        log.info("Saving new state of sender's account...");
        accountRepository.save(account);

        Double receiverAmount = toSendTo.getAmount() + amount;
        toSendTo.setAmount(receiverAmount);

        log.info("Saving new state of receiver's account...");
        accountRepository.save(toSendTo);

        log.info("Creating operation and preparing mail summary...");
        operationService.createOperation(principal, account.getId(), toSendTo.getId(), "transfer", amount);
        return new ResultDTO().setStatus(true).setMessage("Amount successfully transfered!");
    }

    public AccountDTO getAccountById(int id) {
        log.info("Listing account by id...");

        Account account = accountRepository.findAccountById(id);
        if (account != null) {
            AccountDTO acc = new AccountDTO()
                    .setId(account.getId())
                    .setDetails(account.getDetails())
                    .setClient(account.getClient())
                    .setAmount(account.getAmount())
                    .setName(account.getName());

            return acc;
        } else {
            log.info("Exception while listing account by id...");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found!");
        }
    }
}
