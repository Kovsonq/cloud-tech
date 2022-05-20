package com.alexcloud.customer.service;

import com.alexcloud.customer.dto.CustomerRegistrationRequest;
import com.alexcloud.customer.model.Customer;
import com.alexcloud.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public record CustomerService(CustomerRepository customerRepository) {
    public void registerCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        Customer customer = Customer.builder()
                .firstName(customerRegistrationRequest.firstName())
                .lastName(customerRegistrationRequest.lastName())
                .email(customerRegistrationRequest.email())
                .build();

        //TODO check if email valid
        //email not taken

        customerRepository.save(customer);
        //store customer in db
    }
}
