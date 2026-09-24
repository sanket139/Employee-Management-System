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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.getDepartmentName())) {
            throw new DuplicateResourceException("Department already exists: " + request.getDepartmentName());
        }
        Department department = Department.builder()
                .departmentName(request.getDepartmentName())
                .description(request.getDescription())
                .status(DepartmentStatus.ACTIVE)
                .build();
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> departmentMapper.toResponse(d, employeeRepository.countByDepartment_Id(d.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return departmentMapper.toResponse(department, employeeRepository.countByDepartment_Id(id));
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (!department.getDepartmentName().equalsIgnoreCase(request.getDepartmentName())
                && departmentRepository.existsByDepartmentNameIgnoreCase(request.getDepartmentName())) {
            throw new DuplicateResourceException("Department already exists: " + request.getDepartmentName());
        }

        department.setDepartmentName(request.getDepartmentName());
        department.setDescription(request.getDescription());
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved, employeeRepository.countByDepartment_Id(id));
    }

    @Transactional
    public DepartmentResponse setStatus(Long id, DepartmentStatus status) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        department.setStatus(status);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved, employeeRepository.countByDepartment_Id(id));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        long employeeCount = employeeRepository.countByDepartment_Id(id);
        if (employeeCount > 0) {
            throw new BadRequestException("Cannot delete a department that still has " + employeeCount
                    + " employee(s) assigned. Reassign or deactivate them first.");
        }
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public long countDepartments() {
        return departmentRepository.count();
    }
}
