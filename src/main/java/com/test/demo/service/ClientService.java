package com.test.demo.service;

import com.test.demo.dto.ClientDTO;
import com.test.demo.dto.ResultDTO;
import com.test.demo.model.Client;
import com.test.demo.model.Role;
import com.test.demo.model.Subscription;
import com.test.demo.repository.ClientRepository;
import com.test.demo.repository.RoleRepository;
import com.test.demo.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private ClientRepository clientRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder bCryptPasswordEncoder;
    private SubscriptionRepository subscriptionRepository;
    private Logger log = Logger.getLogger(ClientService.class.getName());

    @Autowired
    public ClientService(PasswordEncoder bCryptPasswordEncoder, ClientRepository userRepository, RoleRepository roleRepository, SubscriptionRepository subscriptionRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.clientRepository = userRepository;
        this.roleRepository = roleRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Subscription> getRandomElement(List<Subscription> list, int totalItems) {
        Random rand = new Random();

        List<Subscription> newList = new ArrayList<>();
        for (int i = 0; i < totalItems; i++) {
            int randomIndex = rand.nextInt(list.size());
            newList.add(list.get(randomIndex));
        }
        return newList;
    }

    private List<Subscription> randomizeSubscriptions() {
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptionRepository.findAll().forEach(subscriptions::add);

        return subscriptions;
    }

    public void seedClients() {
        seedClient(1, "admin", "Cristian", "Guta", "1234567890", "Adresa 1", "cristian.guta@domain.com", "password", false, null);
        seedClient(2, "user", "Cezar", "Ionescu", "23894723894728", "Adresa 2", "cristi98feb@yahoo.com", "password", false, subscriptionRepository.getById(2));
        seedClient(3, "user1", "First", "last", "467234", "Adresa 3", "first@domain.com", "password", false, subscriptionRepository.getById(2));
    }

    private void seedClient(int id, String username, String firstName, String lastName, String cnp, String address, String email, String password, boolean deactivated, Subscription subscription) {
        Client client = clientRepository.findByUsername(username);
        LocalDate date = LocalDate.now();


        if (client == null) {
            String roleName = "USER";
            if (username.equals("admin")) {
                roleName = "ADMIN";
            }

            Role role = roleRepository.findByName(roleName);
            client = new Client()
                    .setId(id)
                    .setUsername(username)
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setCnp(cnp)
                    .setAddress(address)
                    .setEmail(email)
                    .setPassword(bCryptPasswordEncoder.encode(password))
                    .setStatus(deactivated)
                    .setRole(role)
                    .setSubscription(subscription);
            clientRepository.save(client);
        }
    }

    public ClientDTO getCurrentClient(Principal principal) {
        log.info("Fetching current client...");
        if (clientRepository.findByUsername(principal.getName()) == null) {
            return new ClientDTO(clientRepository.findClientByEmail(principal.getName()));
        } else {
            return new ClientDTO(clientRepository.findByUsername(principal.getName()));
        }
    }

    public Page<ClientDTO> getAll(int page, int size) {
        log.info("Listing all clients...");

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Client> pageResult = clientRepository.findAll(pageRequest);

        List<ClientDTO> clients = pageResult
                .stream()
                .map(ClientDTO::new)
                .collect(Collectors.toList());

        clients.forEach(clientDTO -> {
            String cnp = new String();
            for(int i=0; i<clientDTO.getCnp().length()-1; i++){
                cnp+="*";

            }
            clientDTO.setCnp(cnp);
        });
        return new PageImpl<>(clients, pageRequest, pageResult.getTotalElements());
    }

    public ClientDTO updateClient(Principal principal, Integer id, ClientDTO updatedClient) {
        log.info("Updating client's informations...");

        Client reqClient = clientRepository.findById(id).get();
        Client currentClient = new Client();
        if(clientRepository.findByUsername(principal.getName()) == null){
            currentClient = clientRepository.findClientByEmail(principal.getName());
        }
        else{
            currentClient = clientRepository.findByUsername(principal.getName());
        }
        if (reqClient != null && reqClient.getId() == currentClient.getId()) {
            currentClient.setAddress(updatedClient.getAddress());
            currentClient.setUsername(updatedClient.getUsername());
            currentClient.setFirstName(updatedClient.getFirstName());
            currentClient.setLastName(updatedClient.getLastName());
            currentClient.setEmail(updatedClient.getEmail());
            currentClient.setCnp(updatedClient.getCnp());

            log.info("Saving new state of Client entity...");
            return new ClientDTO(clientRepository.save(currentClient));
        } else {
            log.info("Something went wrong while executing updateClient(...) method...");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found!");
        }
    }

    public ResultDTO deactivateClient(Integer id) {
        log.info("Deactivating client...");

        return changeClientStatus(id, true);
    }

    public ResultDTO activateClient(Integer id) {
        log.info("Activating client...");

        return changeClientStatus(id, false);
    }

    private ResultDTO changeClientStatus(Integer id, Boolean status) {
        log.info("Changing client's status...");

        Optional<Client> client = clientRepository.findById(id);
        if (client.isPresent()) {
            client.get().setStatus(status);
            clientRepository.save(client.get());
            log.info("Client's status updated...");
            return new ResultDTO().setStatus(true).setMessage("Successfully changed user status!");
        } else {
            log.info("Something went wrong while executing changeClientStatus(...) method...");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
    }
}
