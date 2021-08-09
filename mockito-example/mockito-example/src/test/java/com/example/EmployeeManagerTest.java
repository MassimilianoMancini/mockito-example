package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeManagerTest {

	private EmployeeManager employeeManager;
	private EmployeeRepository employeeRepository;

	@BeforeEach
	void setup() {
		employeeRepository = mock(EmployeeRepository.class);
		employeeManager = new EmployeeManager(employeeRepository);
	}

	@Test
	void testPayEmployeesWhenNoEmployeesArePresent() {
		when(employeeRepository.findAll()).thenReturn(emptyList());
		assertThat(employeeManager.payEmployees()).isZero();
	}

	@Test
	void testPayEmployeesWhenOneEmployeeIsPresent() {
		List<Employee> employees = new ArrayList<>();
		employees.add(new Employee("1", 1000));
		when(employeeRepository.findAll()).thenReturn(employees);
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
	}

}
