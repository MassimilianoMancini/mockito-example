package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class EmployeeManagerTest {

	private EmployeeManager employeeManager;
	private EmployeeRepository employeeRepository;
	private BankService bankService;

	@BeforeEach
	void setup() {
		employeeRepository = mock(EmployeeRepository.class);
		bankService = mock(BankService.class);
		employeeManager = new EmployeeManager(employeeRepository, bankService);
	}

	@Test
	void testPayEmployeesWhenNoEmployeesArePresent() {
		when(employeeRepository.findAll()).thenReturn(emptyList());
		assertThat(employeeManager.payEmployees()).isZero();
	}

	@Test
	void testPayEmployeesWhenOneEmployeeIsPresent() {
		when(employeeRepository.findAll()).thenReturn(asList(new Employee("1", 1000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		verify(bankService).pay("1", 1000);
	}
	
	@Test
	void testPayEmployeesWhenSeveralEmployeesArePresent() {
		when(employeeRepository.findAll())
			.thenReturn(asList(
				new Employee("1", 1000),
				new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);
		InOrder inOrder = inOrder(bankService);
		
		inOrder.verify(bankService).pay("1", 1000);
		inOrder.verify(bankService).pay("2", 2000);
		
		verifyNoMoreInteractions(bankService);
	}
	
	@Test
	void testPayEmployeesWhenSeveralEmployeesArePresentWithArgumentCaptor() {
		when(employeeRepository.findAll())
			.thenReturn(asList(
				new Employee("1", 1000),
				new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);
		ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);
		
		verify(bankService, times(2)).pay(idCaptor.capture(), amountCaptor.capture());
		assertThat(idCaptor.getAllValues()).containsExactly("1", "2");
		assertThat(amountCaptor.getAllValues()).containsExactly(1000.0, 2000.0);
		verifyNoMoreInteractions(bankService);
	}
}
