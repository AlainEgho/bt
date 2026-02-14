package com.example.backend.repository;

import com.example.backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByParentIsNullAndUserIsNullAndActiveTrueOrderBySortOrderAscIdAsc();

    List<MenuItem> findByParentIsNullAndUser_IdAndActiveTrueOrderBySortOrderAscIdAsc(Long userId);

    List<MenuItem> findByParentIsNullOrderBySortOrderAscIdAsc();

    List<MenuItem> findByParent_IdOrderBySortOrderAscIdAsc(Long parentId);
}
