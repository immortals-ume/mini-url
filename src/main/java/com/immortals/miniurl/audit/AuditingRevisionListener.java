package com.immortals.miniurl.audit;

import com.immortals.miniurl.model.domain.AuditingRevisionEntity;
import com.immortals.miniurl.model.enums.UserTypes;
import com.immortals.miniurl.service.exception.UrlShorteningException;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;


public class AuditingRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        AuditingRevisionEntity auditingRevisionEntity = (AuditingRevisionEntity) revisionEntity;

        String currentUser = UserTypes.SYSTEM.name();
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                currentUser = auth.getName();
            }
        } catch (Exception e) {
            throw new UrlShorteningException(e.getMessage(),e);
        }

        auditingRevisionEntity.setUsername(currentUser);
    }
}
