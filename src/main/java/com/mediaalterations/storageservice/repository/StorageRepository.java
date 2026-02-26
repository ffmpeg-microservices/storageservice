package com.mediaalterations.storageservice.repository;

import com.mediaalterations.storageservice.dto.StorageDeleteProjection;
import com.mediaalterations.storageservice.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorageRepository extends JpaRepository<Storage, UUID> {

        @Query("select s.path from Storage s where id=:id")
        Optional<String> getPathFromId(UUID id);

        @Modifying
        @Query("""
                        DELETE FROM Storage s
                        WHERE s.id IN :ids
                        AND s.userId = :userId
                        """)
        void deleteByIdsAndUserId(
                        @Param("ids") List<UUID> ids,
                        @Param("userId") String userId);

        @Query("""
                        SELECT new com.mediaalterations.storageservice.dto.StorageDeleteProjection(
                               s.id,
                               s.path,
                               s.fileName
                        )
                        FROM Storage s
                        WHERE s.id IN :ids
                        AND s.userId = :userId
                        """)
        List<StorageDeleteProjection> findForDeletion(
                        @Param("ids") List<UUID> ids,
                        @Param("userId") String userId);

        List<Storage> findByUserIdAndMediaTypeIn(String string, List<String> of);

}
