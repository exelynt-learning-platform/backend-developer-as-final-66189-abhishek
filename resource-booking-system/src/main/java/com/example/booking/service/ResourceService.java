package com.example.booking.service;

import com.example.booking.dto.*;
import com.example.booking.entity.Resource;
import com.example.booking.exception.NotFoundException;
import com.example.booking.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository repository;

    public ResourceService(ResourceRepository repository) {
        this.repository = repository;
    }

    public List<ResourceResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ResourceResponse findById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found: " + id)));
    }

    public ResourceResponse create(ResourceRequest request) {
        Resource r = new Resource();
        r.setName(request.name());
        r.setDescription(request.description());
        r.setActive(request.active() == null || request.active());
        return toResponse(repository.save(r));
    }

    public ResourceResponse update(Long id, ResourceRequest request) {
        Resource r = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found: " + id));
        r.setName(request.name());
        r.setDescription(request.description());
        if (request.active() != null) r.setActive(request.active());
        return toResponse(repository.save(r));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new NotFoundException("Resource not found: " + id);
        repository.deleteById(id);
    }

    private ResourceResponse toResponse(Resource r) {
        return new ResourceResponse(r.getId(), r.getName(), r.getDescription(), r.isActive());
    }
}
