package com.alexcloud.customer.controller;

import com.alexcloud.customer.dto.CustomerRegistrationRequest;
import com.alexcloud.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
public record CustomerController (CustomerService customerService) {

    @PostMapping
    public void registerCustomer(@RequestBody CustomerRegistrationRequest customerRegistrationRequest){
        log.info("Customer created {}", customerRegistrationRequest);
        customerService.registerCustomer(customerRegistrationRequest);
    }
}
