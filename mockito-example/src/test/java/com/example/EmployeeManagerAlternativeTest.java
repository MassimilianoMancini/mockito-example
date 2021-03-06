package com.example;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

@ExtendWith(MockitoExtension.class)
class EmployeeManagerAlternativeTest {

	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private BankService bankService;

	@InjectMocks
	private EmployeeManager employeeManager;

	@Captor
	private ArgumentCaptor<String> idCaptor;

	@Captor
	private ArgumentCaptor<Double> amountCaptor;

	@Spy
	private Employee notToBePaid = new Employee("1", 1000);

	@Spy
	private Employee toBePaid = new Employee("2", 2000);

	@Spy
	private Employee employee = new Employee("1", 1000);

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
		when(employeeRepository.findAll()).thenReturn(asList(new Employee("1", 1000), new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);
		InOrder inOrder = inOrder(bankService);

		inOrder.verify(bankService).pay("1", 1000);
		inOrder.verify(bankService).pay("2", 2000);

		verifyNoMoreInteractions(bankService);
	}

	@Test
	void testPayEmployeesWhenSeveralEmployeesArePresentWithArgumentCaptor() {
		when(employeeRepository.findAll()).thenReturn(asList(new Employee("1", 1000), new Employee("2", 2000)));
		assertThat(employeeManager.payEmployees()).isEqualTo(2);

		// see annotation @Captor
		// ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
		// ArgumentCaptor<Double> amountCaptor = ArgumentCaptor.forClass(Double.class);

		verify(bankService, times(2)).pay(idCaptor.capture(), amountCaptor.capture());
		assertThat(idCaptor.getAllValues()).containsExactly("1", "2");
		assertThat(amountCaptor.getAllValues()).containsExactly(1000.0, 2000.0);
		verifyNoMoreInteractions(bankService);
	}

	@Test
	void testOtherEmployeesArePaidWhenBankServiceThrowsException() {
		// see annotation @Spy
		// Employee notToBePaid = spy(new Employee("1", 1000));
		// Employee toBePaid = spy(new Employee("2", 2000));
		when(employeeRepository.findAll()).thenReturn(asList(notToBePaid, toBePaid));
		doThrow(new RuntimeException()).doNothing().when(bankService).pay(anyString(), anyDouble());
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		verify(notToBePaid).setPaid(false);
		verify(toBePaid).setPaid(true);
	}

	@Test
	void testEmployeeSetPaidIsCalledAfterPaying() {
		// see annotation @Spy
		when(employeeRepository.findAll()).thenReturn(asList(employee));
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		InOrder inOrder = inOrder(bankService, employee);
		inOrder.verify(bankService).pay("1", 1000);
		inOrder.verify(employee).setPaid(true);
	}

	@Test
	void testPayEmployeesWhenBankServiceThrowsException() {
		// see annotation @Spy
		when(employeeRepository.findAll()).thenReturn(asList(employee));
		doThrow(new RuntimeException()).when(bankService).pay(anyString(), anyDouble());
		// number of payments must be 0
		assertThat(employeeManager.payEmployees()).isZero();
		// make sure that Employee.paid is updated accordingly
		verify(employee).setPaid(false);
	}

	@Test
	void testOtherEmployeesArePaidWhenBankServiceThrowsExceptionArgumentMatcher() {
		// see annotation @Spy
		when(employeeRepository.findAll()).thenReturn(asList(notToBePaid, toBePaid));
		doThrow(new RuntimeException()).when(bankService).pay(argThat(s -> s.equals("1")), anyDouble());
		assertThat(employeeManager.payEmployees()).isEqualTo(1);
		verify(notToBePaid).setPaid(false);
		verify(toBePaid).setPaid(true);
	}

}
