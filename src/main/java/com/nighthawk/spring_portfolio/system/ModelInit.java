package com.nighthawk.spring_portfolio.system;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonDetailsService;
import com.nighthawk.spring_portfolio.mvc.person.PersonRole;
import com.nighthawk.spring_portfolio.mvc.person.PersonRoleJpaRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Component
@Configuration // Scans Application for ModelInit Bean, this detects CommandLineRunner
public class ModelInit {  
    @Autowired PersonRoleJpaRepository roleJpaRepository;
    @Autowired PersonDetailsService personDetailsService;

    @Bean
    @Transactional
    CommandLineRunner run() {  // The run() method will be executed after the application starts
        return args -> {

            // Person database is populated with starting people
            Person[] personArray = Person.init();
            for (Person person : personArray) {
                // Name and email are used to lookup the person
                List<Person> personFound = personDetailsService.list(person.getName(), person.getEmail());  // lookup
                if (personFound.size() == 0) { // add if not found
                    // Roles are added to the database if they do not exist
                    List<PersonRole> updatedRoles = new ArrayList<>();
                    for (PersonRole role : person.getRoles()) {
                        // Name is used to lookup the role
                        PersonRole roleFound = roleJpaRepository.findByName(role.getName());  // JPA lookup
                        if (roleFound == null) { // add if not found
                            // Save the new role to database
                            roleJpaRepository.save(role);  // JPA save
                            roleFound = role;
                        }
                        // Accumulate reference to role from database
                        updatedRoles.add(roleFound);
                    }
                    // Update person with roles from role databasea
                    person.setRoles(updatedRoles); // Object reference is updated

                    // Save person to database
                    personDetailsService.save(person); // JPA save

                }
            }

        };
    }
}

