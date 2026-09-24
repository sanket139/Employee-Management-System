package com.employeemanagement.service;

import com.employeemanagement.dto.request.DepartmentRequest;
import com.employeemanagement.dto.response.DepartmentResponse;
import com.employeemanagement.entity.Department;
import com.employeemanagement.entity.enums.DepartmentStatus;
import com.employeemanagement.exception.BadRequestException;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.mapper.DepartmentMapper;
import com.employeemanagement.repository.DepartmentRepository;
import com.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    private DepartmentRequest request;
    private Department department;

    @BeforeEach
    void setUp() {
        request = new DepartmentRequest();
        request.setDepartmentName("IT");
        request.setDescription("Information Technology");

        department = Department.builder().id(1L).departmentName("IT")
                .description("Information Technology").status(DepartmentStatus.ACTIVE).build();
    }

    @Test
    void createDepartment_success() {
        when(departmentRepository.existsByDepartmentNameIgnoreCase("IT")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));
        when(departmentMapper.toResponse(any(Department.class), anyLong()))
                .thenReturn(DepartmentResponse.builder().id(1L).departmentName("IT").build());

        DepartmentResponse response = departmentService.createDepartment(request);

        assertThat(response.getDepartmentName()).isEqualTo("IT");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_duplicateName_throwsException() {
        when(departmentRepository.existsByDepartmentNameIgnoreCase("IT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void updateDepartment_notFound_throwsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteDepartment_withEmployees_throwsBadRequest() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartment_Id(1L)).thenReturn(3L);

        assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("3 employee");

        verify(departmentRepository, never()).delete(any());
    }

    @Test
    void deleteDepartment_noEmployees_deletesSuccessfully() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartment_Id(1L)).thenReturn(0L);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(department);
    }
}
